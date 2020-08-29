
import java.util.*;
import java.io.*;


public class Type {
    static Scanner console = new Scanner(System.in);
    String name;
    boolean isEmpty;
    int num_of_fields;
    String[] fields;

    public Type(String typeName, int num_of_fields, String[] fields) {
        this.name = typeName;
        this.isEmpty = false;
        this.num_of_fields = num_of_fields;
        this.fields = fields.clone();
    }

    public Type(String typeStr) {
        String[] parts = typeStr.split(FileManager.FIELD_DELIMETER);
        this.name = parts[0];
        this.isEmpty = "1".equals(parts[1]);
        this.num_of_fields = Integer.valueOf(parts[2]);
        this.fields = new String[this.num_of_fields];
        for (int i = 3; i < parts.length; i++) {
            this.fields[i-3] = parts[i];
        }
    }

    public String getName(){
        return this.name;
    }

    public int getNOfFields(){
        return this.num_of_fields;
    }

    public String[] getFields(){
        return this.fields;
    }

    public boolean getIsEmpty(){
        return this.isEmpty;
    }
    public void setIsEmpty(boolean isEmpty){
        this.isEmpty = isEmpty;
    }

    public static void create(File file) {
        try {
            System.out.println("Enter type name");
            String typeName = console.next();


            System.out.println("Enter the number of fields up to " + FileManager.NUM_OF_FIELDS);
            while (!console.hasNextInt()) {
                System.out.println("That's not a number!");
                console.next();
            }
            int nOfFields = console.nextInt();

            String[] fields = new String[nOfFields];
            for (int i = 0; i < nOfFields; i++) {
                System.out.println("Enter name for field " + (i+1));
                fields[i] = console.next();
            }
            Type type = new Type(typeName, nOfFields, fields);

            String[] pages = FileManager.getPages(file);

            Page discDir = new Page(pages[0]);
            int sysAddress = discDir.getSysCatAddress();
            int saveAddress = discDir.addFile(typeName + ".txt");
            pages[0] = discDir.toString();
            FileManager.writePages(file, pages);

            Page rPage = new Page(pages[saveAddress]);
            rPage.pageHeader.isEmpty = false;
            pages[saveAddress] = rPage.toString();
            FileManager.writePages(file, pages);

            Page sysPage = new Page(pages[sysAddress]);
            while(sysPage.pageHeader.pointer != 0) {
                sysAddress = sysPage.pageHeader.pointer;
                sysPage = new Page(pages[sysAddress]);
            }

            if (sysPage.pageHeader.nOfRecords == FileManager.TYPE_PER_PAGE) {
                int freeAddress = discDir.getFreeAddress();
                pages[0] = discDir.toString();
                FileManager.writePages(file, pages);

                sysPage.pageHeader.pointer = freeAddress;
                pages[sysAddress] = sysPage.toString();
                FileManager.writePages(file, pages);


                sysPage = new Page(pages[freeAddress]);
                sysPage.pageHeader.isEmpty = false;
                sysPage.pageHeader.nOfRecords ++;
                sysPage.records.add(type.toString());
                pages[freeAddress] = sysPage.toString();
                FileManager.writePages(file, pages);
            } else {
                sysPage.records.add(type.toString());
                sysPage.pageHeader.nOfRecords ++;
                pages[sysAddress] = sysPage.toString();
                FileManager.writePages(file, pages);
            }
            System.out.println();
            System.out.println("Type \"" + typeName + "\" is added to database successfully.");
        } catch (IOException e) {
            System.out.println("Couldn't read line from text");
            System.exit(0);
        }
    }

    public static void delete(File file) {
        try {
            ArrayList<Type> types = getTypeList(file);
            System.out.println();
            System.out.println(types.size() + " types are found");
            for (int i = 0; i < types.size(); i++) {
                System.out.println("\t[" + (i+1) + "] " + types.get(i).name);
                System.out.println("\t\t Number of Fields: " + types.get(i).num_of_fields);
                System.out.println("\t\t Fields: " + String.join(" / ", types.get(i).fields));
            }

            System.out.println("Select the type that you wish to delete [1-" + types.size() + "]");
            int typeID = console.nextInt();
            Type type = types.get(typeID - 1);

            String[] pages = FileManager.getPages(file);
            Page discDir = new Page(pages[0]);
            int sysAddress = discDir.getSysCatAddress();
            int removeAddress = discDir.removeFile(type.name + ".txt");
            pages[0] = discDir.toString();
            FileManager.writePages(file, pages);

            Page rPage = new Page(pages[removeAddress]);
            rPage.pageHeader.isEmpty = true;
            rPage.pageHeader.nOfRecords = 0;
            int nextPointer = rPage.pageHeader.pointer;
            rPage.pageHeader.pointer = 0;
            rPage.records.clear();
            pages[removeAddress] = rPage.toString();
            FileManager.writePages(file, pages);

            while(nextPointer != 0) {
                removeAddress = nextPointer;
                rPage = new Page(pages[removeAddress]);
                rPage.pageHeader.isEmpty = true;
                rPage.pageHeader.nOfRecords = 0;
                nextPointer = rPage.pageHeader.pointer;
                rPage.pageHeader.pointer = 0;
                rPage.records.clear();
                pages[removeAddress] = rPage.toString();
                FileManager.writePages(file, pages);
            }

            Page sysPage = new Page(pages[sysAddress]);
            int loc = -1;
            while(loc == -1) {
                for (int i = 0; i < sysPage.records.size(); i++) {
                    if (sysPage.records.get(i).equalsIgnoreCase(type.toString())) {
                        loc = i;
                        break;
                    }
                }
                if (sysPage.pageHeader.pointer == 0) {
                    break;
                }
                sysAddress = sysPage.pageHeader.pointer;
                sysPage = new Page(pages[sysAddress]);
            }
            if (loc == -1) {
                System.out.println("Couldn't find type");
            } else {
                type.setIsEmpty(true);
                sysPage.records.set(loc, type.toString());
                sysPage.pageHeader.nOfRecords --;
                pages[sysAddress] = sysPage.toString();
                FileManager.writePages(file, pages);
                System.out.println();
                System.out.println("Type \"" + type.name + "\" is deleted from database successfully.");
            }
        } catch (IOException e) {
            System.out.println("Couldn't read line from text");
            System.exit(0);
        }
    }

    public static void list(File file) {
        ArrayList<Type> types = getTypeList(file);
        System.out.println();
        System.out.println(types.size() + " types are found");
        for (int i = 0; i < types.size(); i++) {
            System.out.println("\t[" + (i+1) + "] " + types.get(i).name);
            System.out.println("\t\t Number of Fields: " + types.get(i).num_of_fields);
            System.out.println("\t\t Fields: " + String.join(" / ", types.get(i).fields));
        }
    }

    public static ArrayList<Type> getTypeList(File file) {
        ArrayList<Type> types = new ArrayList<>();
        try {
            String[] pages = FileManager.getPages(file);
            Page discDir = new Page(pages[0]);
            int sysAddress = discDir.getSysCatAddress();

            Page sysPage = new Page(pages[sysAddress]);
            for ( String str :  sysPage.records) {
                Type type = new Type(str);
                if (!type.getIsEmpty()) {
                    types.add(type);
                }
            }
            while (sysPage.pageHeader.pointer != 0) {
                sysPage = new Page(pages[sysPage.pageHeader.pointer]);
                for ( String str :  sysPage.records) {
                    Type type = new Type(str);
                    if (!type.getIsEmpty()) {
                        types.add(type);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Couldn't read line from text");
            System.exit(0);
        }
        return types;
    }

    @Override
    public String toString() {
        ArrayList<String> parts = new ArrayList<>();
        parts.add(name);
        if (isEmpty)
            parts.add("1");
        else
            parts.add("0");
        parts.add(Integer.toString(num_of_fields));
        parts.addAll(Arrays.asList(fields));
        return String.join(FileManager.FIELD_DELIMETER, parts);
    }
}
