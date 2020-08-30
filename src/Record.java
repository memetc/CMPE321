import java.util.*;
import java.io.*;

public class Record {
    static short end_ID = 0;
    static Scanner console = new Scanner(System.in);

    short ID;
    boolean isEmpty;
    String[] fields;
    Type type;

    public Record(Type type, String[] fields) {
        end_ID++;
        this.ID = end_ID;
        this.isEmpty = false;
        this.type = type;
        this.fields = fields.clone();
    }

    public Record(Type type, String str_type) {
        String[] parts = str_type.split(Controller.FIELD_DELIMETER);
        this.ID = Short.valueOf(parts[0]);
        this.isEmpty = "1".equals(parts[1]);
        this.type = type;
        this.fields = new String[type.num_of_fields];
        for (int i = 2; i < parts.length; i++) {
            this.fields[i-2] = parts[i];
        }
    }



    public static void create(File file) {
        ArrayList<String> type_names = new ArrayList<String>();
        try {
            ArrayList<Type> types = Type.getTypeList(file);

            for (int i = 0; i < types.size(); i++) {
                System.out.println("\t\t[" + types.get(i).name + "] " );
                System.out.println("\t\tFields: " + String.join(" / ", types.get(i).fields));
                type_names.add(types.get(i).name);
            }
            if(types.size() == 0){
                System.out.println("None type exist, create a type first!");
                return;
            }

            System.out.println("Enter the name of the type that you wish to create a new record");

            String target_type = console.next();
            int index = type_names.indexOf(target_type);
            while(index == -1){
                System.out.println("Type does not exist, enter again");
                target_type = console.next();
                index = type_names.indexOf(target_type);
            }
            Type type = types.get(index);


            System.out.println("You are creating record for type " + type.name);
            String[] fields = new String[type.num_of_fields];
            for (int i = 0; i < type.num_of_fields; i++) {
                System.out.println("Enter data for " + type.fields[i]);
                fields[i] = console.next();
            }
            Record record = new Record(type, fields);

            String[] pages = Controller.read_pages(file);
            Page disc_page = new Page(pages[0]);
            int address = disc_page.getAddress(type.name + ".txt");

            Page rPage = new Page(pages[address]);
            while(rPage.pageHeader.pointer != 0) {
                address = rPage.pageHeader.pointer;
                rPage = new Page(pages[address]);
            }

            if (rPage.pageHeader.size == Controller.RECORD_PER_PAGE) {
                int freeAddress = disc_page.getFreeAddress();
                pages[0] = disc_page.toString();
                Controller.write_pages(file, pages);

                rPage.pageHeader.pointer = freeAddress;
                pages[address] = rPage.toString();
                Controller.write_pages(file, pages);


                rPage = new Page(pages[freeAddress]);
                rPage.pageHeader.isEmpty = false;
                rPage.pageHeader.size++;
                rPage.records.add(record.toString());
                pages[freeAddress] = rPage.toString();
                Controller.write_pages(file, pages);
            } else {
                rPage.records.add(record.toString());
                rPage.pageHeader.size++;
                pages[address] = rPage.toString();
                Controller.write_pages(file, pages);
            }
            System.out.println("Record is added successfully.");
        } catch (IOException e) {
            System.exit(0);
        }
    }

    public static void delete(File file) throws IOException {
        ArrayList<String> type_names = new ArrayList<String>();

        ArrayList<Type> types = Type.getTypeList(file);
        for (int i = 0; i < types.size(); i++) {
            System.out.println("\t\t[" + types.get(i).name + "]");
            System.out.println("\t\t Number of Fields: " + types.get(i).num_of_fields);
            System.out.println("\t\t Fields: " + String.join(" / ", types.get(i).fields));
            type_names.add(types.get(i).name);
        }


        String target_type = console.next();
        int index = type_names.indexOf(target_type);
        while(index == -1){
            System.out.println("Type does not exist, enter again");
            target_type = console.next();
            index = type_names.indexOf(target_type);
        }
        Type type = types.get(index);

        System.out.println("Records for type" + type.name);

        ArrayList<Record> records = getRecords(file, type);
        ArrayList<String> record_keys =  new ArrayList<String>();
        for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            record_keys.add(records.get(i).fields[0]);
            ArrayList<String> parts = new ArrayList<>();
            for (int j = 0; j < record.type.num_of_fields; j++) {
                parts.add(record.type.fields[j] + ": " + record.fields[j]);
            }
            System.out.println("\t[" + (i+1) + "] => " + String.join(" / ", parts));
        }
        if(records.size() == 0){
            System.out.println("Record does not exist");
            return;
        }
        else
            System.out.println("Select the record that you wish to delete with primary key(value of the first field)");
        String key_target_record = console.next();
        index = record_keys.indexOf(key_target_record);
        Record record = records.get(index);
        System.out.println("Deleting: " + record.toString());
        String[] pages = Controller.read_pages(file);
        Page disc_page = new Page(pages[0]);
        int address = disc_page.getAddress(type.name + ".txt");
        Page record_page = new Page(pages[address]);

        int position = -1;
        while(position == -1) {
            for (int i = 0; i < record_page.records.size(); i++) {
                if (record_page.records.get(i).equalsIgnoreCase(record.toString())) {
                    position = i;
                    break;
                }
            }
            if (record_page.pageHeader.pointer == 0) {
                break;
            }
            address = record_page.pageHeader.pointer;
            record_page = new Page(pages[address]);
        }
        if (position == -1) {
            System.out.println("Couldn't find record");
        } else {
            record.isEmpty = true;
            record_page.records.set(position, record.toString());
            record_page.pageHeader.size--;
            pages[address] = record_page.toString();
            Controller.write_pages(file, pages);
            System.out.println();
            System.out.println("Record is deleted from database successfully.");
        }


    }

    public static void retrieve(File file) {
        ArrayList<String> type_names = new ArrayList<String>();
        ArrayList<Type> types = Type.getTypeList(file);
        for (int i = 0; i < types.size(); i++) {
            System.out.println("\t[" + (i+1) + "] " + types.get(i).name);
            System.out.println("\t\t Number of Fields: " + types.get(i).num_of_fields);
            System.out.println("\t\t Fields: " + String.join(" / ", types.get(i).fields));
            type_names.add(types.get(i).name);
        }
        if(types.size() == 0){
            System.out.println("Record does not exist");
            return;
        }

        System.out.println("Enter the name of the type that you want to retrieve the record of");
        String target_type = console.next();
        int index = type_names.indexOf(target_type);
        Type type = types.get(index);

        System.out.println("Primary key for type " + type.name + " is " + type.fields[0]);
        System.out.println("Enter " + type.fields[0] + " for retrieve record");
        String pk = console.next();

        ArrayList<Record> records = getRecords(file, type);
        Record found = null;
        for (Record record : records) {
            if (pk.equalsIgnoreCase(record.fields[0])){
                found = record;
                break;
            }
        }
        if (found != null) {
            System.out.println("The record is found");
            System.out.println("\t[" + found.ID + "] Type " + found.type.name);
            ArrayList<String> parts = new ArrayList<>();
            for (int j = 0; j < found.type.num_of_fields; j++) {
                parts.add(found.type.fields[j] + ": " + found.fields[j]);
            }
            System.out.println("\t\t" + String.join(" / ", parts));
        } else {
            System.out.println("The record that has given primary key couldn't found");
        }
    }

    public static void list(File file) {
        ArrayList<String> type_names = new ArrayList<String>();
        System.out.println();
        ArrayList<Type> types = Type.getTypeList(file);
        for (int i = 0; i < types.size(); i++) {
            System.out.println("\t\t " + types.get(i).name);
            System.out.println("\t\t Number of Fields: " + types.get(i).num_of_fields);
            System.out.println("\t\t Fields: " + String.join(" / ", types.get(i).fields));
            type_names.add(types.get(i).name);
        }
        if(types.size() == 0){
            System.out.println("Type does not exist");
            return;
        }


        System.out.println("Enter the name of the type that you want to list the records of");
        String target_type = console.next();
        int index = type_names.indexOf(target_type);
        Type type = types.get(index);

        System.out.println("Records for type " + type.name);
        ArrayList<Record> records = Record.getRecords(file, type);
        for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            ArrayList<String> parts = new ArrayList<>();
            for (int j = 0; j < record.type.num_of_fields; j++) {
                parts.add(record.type.fields[j] + ": " + record.fields[j]);
            }
            System.out.println("\t\t" + String.join(" / ", parts));




        }
    }

    public static ArrayList<Record> getRecords(File file, Type type) {
        ArrayList<Record> records = new ArrayList<>();
        try {
            String[] pages = Controller.read_pages(file);
            Page discDir = new Page(pages[0]);
            int address = discDir.getAddress(type.name + ".txt");
            Page rPage = new Page(pages[address]);
            for ( String str :  rPage.records) {
                Record record = new Record(type, str);
                if (!record.isEmpty) {
                    records.add(record);
                }
            }

            while (rPage.pageHeader.pointer != 0) {
                rPage = new Page(pages[rPage.pageHeader.pointer]);
                for ( String str :  rPage.records) {
                    Record record = new Record(type, str);
                    if (!record.isEmpty) {
                        records.add(record);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Couldn't read line from text");
            System.exit(0);
        }
        return records;
    }


    @Override
    public String toString() {
        ArrayList<String> parts = new ArrayList<>();
        parts.add(Integer.toString(ID));
        if (isEmpty)
            parts.add("1");
        else
            parts.add("0");
        parts.addAll(Arrays.asList(fields));
        return String.join(Controller.FIELD_DELIMETER, parts);
    }
}
