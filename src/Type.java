
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
        String[] parts = typeStr.split(Controller.FIELD_DELIMETER);
        this.name = parts[0];
        this.isEmpty = "1".equals(parts[1]);
        this.num_of_fields = Integer.valueOf(parts[2]);
        this.fields = new String[this.num_of_fields];
        for (int i = 3; i < parts.length; i++) {
            this.fields[i-3] = parts[i];
        }
    }

    public static void create(File file) {
        try {
            System.out.println("Enter type name");
            String type_name = console.next();


            System.out.println("Enter the number of fields up to " + Controller.NUM_OF_FIELDS);
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
            Type type = new Type(type_name, nOfFields, fields);

            String[] pages = Controller.read_pages(file);

            Page disc_page = new Page(pages[0]);
            int system_address = disc_page.getAddress(Controller.DICTIONARY_FILE);
            int save_address = disc_page.addFile(type_name + ".txt");
            pages[0] = disc_page.toString();
            Controller.write_pages(file, pages);
            Page record_page = new Page(pages[save_address]);
            record_page.pageHeader.isEmpty = false;
            pages[save_address] = record_page.toString();
            Controller.write_pages(file, pages);
            Page system_page = new Page(pages[system_address]);


            while(system_page.pageHeader.pointer != 0) {
                system_address = system_page.pageHeader.pointer;
                system_page = new Page(pages[system_address]);
            }

            if (system_page.pageHeader.size == Controller.TYPE_PER_PAGE) {
                int freeAddress = disc_page.getFreeAddress();
                pages[0] = disc_page.toString();
                Controller.write_pages(file, pages);

                system_page.pageHeader.pointer = freeAddress;
                pages[system_address] = system_page.toString();
                Controller.write_pages(file, pages);


                system_page = new Page(pages[freeAddress]);
                system_page.pageHeader.isEmpty = false;
                system_page.pageHeader.size++;
                system_page.records.add(type.toString());
                pages[freeAddress] = system_page.toString();
                Controller.write_pages(file, pages);
            }
            else {
                system_page.records.add(type.toString());
                system_page.pageHeader.size++;
                pages[system_address] = system_page.toString();
                Controller.write_pages(file, pages);
            }
            System.out.println();
            System.out.println("Type \"" + type_name + "\" is added to database successfully.");
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

            String[] pages = Controller.read_pages(file);
            Page disc_page = new Page(pages[0]);
            int sysAddress = disc_page.getAddress(Controller.DICTIONARY_FILE);
            int removeAddress = disc_page.removeFile(type.name + ".txt");
            pages[0] = disc_page.toString();
            Controller.write_pages(file, pages);

            Page record_page = new Page(pages[removeAddress]);
            record_page.pageHeader.isEmpty = true;
            record_page.pageHeader.size = 0;
            int next = record_page.pageHeader.pointer;
            record_page.pageHeader.pointer = 0;
            record_page.records.clear();
            pages[removeAddress] = record_page.toString();
            Controller.write_pages(file, pages);

            while(next != 0) {
                removeAddress = next;
                record_page = new Page(pages[removeAddress]);
                record_page.pageHeader.isEmpty = true;
                record_page.pageHeader.size = 0;
                next = record_page.pageHeader.pointer;
                record_page.pageHeader.pointer = 0;
                record_page.records.clear();
                pages[removeAddress] = record_page.toString();
                Controller.write_pages(file, pages);
            }

            Page sysPage = new Page(pages[sysAddress]);
            int position = -1;
            while(position == -1) {
                for (int i = 0; i < sysPage.records.size(); i++) {
                    if (sysPage.records.get(i).equalsIgnoreCase(type.toString())) {
                        position = i;
                        break;
                    }
                }
                if (sysPage.pageHeader.pointer == 0) {
                    break;
                }
                sysAddress = sysPage.pageHeader.pointer;
                sysPage = new Page(pages[sysAddress]);
            }
            if (position == -1) {
                System.out.println("Couldn't find type");
            } else {
                type.isEmpty = true;
                sysPage.records.set(position, type.toString());
                sysPage.pageHeader.size--;
                pages[sysAddress] = sysPage.toString();
                Controller.write_pages(file, pages);
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
            String[] pages = Controller.read_pages(file);
            Page disc_page = new Page(pages[0]);

            Page dictionary_page = new Page(pages[disc_page.getAddress(Controller.DICTIONARY_FILE)]);
            for ( String str :  dictionary_page.records) {
                Type type = new Type(str);
                if (!type.isEmpty) {
                    types.add(type);
                }
            }
            while (dictionary_page.pageHeader.pointer != 0) {
                dictionary_page = new Page(pages[dictionary_page.pageHeader.pointer]);
                for ( String str :  dictionary_page.records) {
                    Type type = new Type(str);
                    if (!type.isEmpty) {
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
        return String.join(Controller.FIELD_DELIMETER, parts);
    }
}
