import java.util.*;
import java.io.*;

public class Record {
    static short end_ID = 0;
    static Scanner console = new Scanner(System.in);

    short ID;
    boolean empty;
    String[] field_list;
    Type type;

    public Record(Type type, String[] field_list) {
        end_ID++;
        this.ID = end_ID;
        this.empty = false;
        this.type = type;
        this.field_list = field_list.clone();
    }

    public Record(Type type, String str_type) {
        System.out.println(str_type);
        String[] str_result = str_type.split("-");
        this.ID = Short.valueOf(str_result[0]);
        this.empty = "1".equals(str_result[1]);
        this.type = type;
        this.field_list = new String[type.num_of_fields];
        for (int i = 2; i < str_result.length; i++) {
            this.field_list[i-2] = str_result[i];
        }
    }

    public static void create(File file) {
        ArrayList<String> type_names = new ArrayList<String>();
        try {
            ArrayList<Type> type_list = Type.get_types(file);

            for (int i = 0; i < type_list.size(); i++) {
                System.out.println("\t\t[" + type_list.get(i).name + "] "+ String.join(" / ", type_list.get(i).field_list));
                type_names.add(type_list.get(i).name);
            }
            if(type_list.size() == 0){
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
            Type type = type_list.get(index);
            String[] fields = new String[type.num_of_fields];
            for (int i = 0; i < type.num_of_fields; i++) {
                System.out.print(type.field_list[i] + ":");
                fields[i] = console.next();
            }
            Record record = new Record(type, fields);
            String[] table = Controller.read_pages(file);
            Page disc_page = new Page(table[0]);
            int address = disc_page.find_address(type.name + ".txt");
            Page record_page = new Page(table[address]);
            while(record_page.next != 0) {
                address = record_page.next;
                record_page = new Page(table[address]);
            }

            if (record_page.size == Controller.RECORD_PER_PAGE) {
                short free_address = disc_page.get_free_address();
                table[0] = disc_page.toString();
                Controller.write_pages(file, table);

                record_page.next = free_address;
                table[address] = record_page.toString();
                Controller.write_pages(file, table);


                record_page = new Page(table[free_address]);
                record_page.empty = false;
                record_page.size++;
                record_page.record_list.add(record.toString());
                table[free_address] = record_page.toString();
                Controller.write_pages(file, table);
            } else {
                record_page.record_list.add(record.toString());
                record_page.size++;
                table[address] = record_page.toString();
                Controller.write_pages(file, table);
            }
            System.out.println("Database is updated");
        } catch (IOException e) {
            System.out.println("Exception");
            System.exit(0);
        }
    }

    public static void delete(File file) throws IOException {
        ArrayList<String> type_names = new ArrayList<String>();

        ArrayList<Type> types = Type.get_types(file);
        for (int i = 0; i < types.size(); i++) {
            System.out.println("\t\t[" + types.get(i).name + "]");
            System.out.println("\t\t Number of Fields: " + types.get(i).num_of_fields);
            System.out.println("\t\t Fields: " + String.join(" / ", types.get(i).field_list));
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

        ArrayList<Record> records = get_records(file, type);
        ArrayList<String> record_keys =  new ArrayList<String>();
        for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            record_keys.add(records.get(i).field_list[0]);
            ArrayList<String> parts = new ArrayList<>();
            for (int j = 0; j < record.type.num_of_fields; j++) {
                parts.add(record.type.field_list[j] + ": " + record.field_list[j]);
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
        int address = disc_page.find_address(type.name + ".txt");
        Page record_page = new Page(pages[address]);

        int position = -1;
        while(position == -1) {
            for (int i = 0; i < record_page.record_list.size(); i++) {
                if (record_page.record_list.get(i).equalsIgnoreCase(record.toString())) {
                    position = i;
                    break;
                }
            }
            if (record_page.next == 0) {
                break;
            }
            address = record_page.next;
            record_page = new Page(pages[address]);
        }
        if (position == -1) {
            System.out.println("Couldn't find record");
        } else {
            record.empty = true;
            record_page.record_list.set(position, record.toString());
            record_page.size--;
            pages[address] = record_page.toString();
            Controller.write_pages(file, pages);
            System.out.println();
            System.out.println("Record is deleted from database successfully.");
        }


    }

    public static void retrieve(File file) {
        ArrayList<String> type_names = new ArrayList<String>();
        ArrayList<Type> types = Type.get_types(file);
        for (int i = 0; i < types.size(); i++) {
            System.out.println("\t[" + (i+1) + "] " + types.get(i).name);
            System.out.println("\t\t Fields: " + String.join(" / ", types.get(i).field_list));
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

        System.out.println("Primary key for type " + type.name + " is " + type.field_list[0]);
        System.out.println("Enter " + type.field_list[0] + " for retrieve record");
        String pk = console.next();

        ArrayList<Record> records = get_records(file, type);
        Record found = null;
        for (Record record : records) {
            if (pk.equalsIgnoreCase(record.field_list[0])){
                found = record;
                break;
            }
        }
        if (found != null) {
            System.out.println("The record is found");
            System.out.println("\t\t[" + found.ID + "] Type " + found.type.name);
            ArrayList<String> parts = new ArrayList<>();
            for (int j = 0; j < found.type.num_of_fields; j++) {
                parts.add(found.type.field_list[j] + ": " + found.field_list[j]);
            }
            System.out.println("\t\t" + String.join(" / ", parts));
        } else {
            System.out.println("The record that has given primary key couldn't found");
        }
    }

    public static void list(File file) {
        ArrayList<String> type_names = new ArrayList<String>();
        System.out.println();
        ArrayList<Type> types = Type.get_types(file);
        for (int i = 0; i < types.size(); i++) {
            System.out.println("\t\t " + types.get(i).name);
            System.out.println("\t\t Fields: " + String.join(" / ", types.get(i).field_list));
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
        ArrayList<Record> records = Record.get_records(file, type);
        for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            ArrayList<String> parts = new ArrayList<>();
            for (int j = 0; j < record.type.num_of_fields; j++) {
                parts.add(record.type.field_list[j] + ": " + record.field_list[j]);
            }
            System.out.println("\t\t" + String.join(" / ", parts));
        }
    }

    public static ArrayList<Record> get_records(File file, Type type) {
        ArrayList<Record> record_list = new ArrayList<>();
        try {
            String[] table = Controller.read_pages(file);
            Page disc_page = new Page(table[0]);
            int address = disc_page.find_address(type.name + ".txt");
            Page record_page = new Page(table[address]);
            for ( String i :  record_page.record_list) {
                Record record = new Record(type, i);
                if (!record.empty) {
                    record_list.add(record);
                }
            }

            while (record_page.next != 0) {
                record_page = new Page(table[record_page.next]);
                for ( String str :  record_page.record_list) {
                    Record record = new Record(type, str);
                    if (!record.empty) {
                        record_list.add(record);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("exception");
            System.exit(0);
        }
        return record_list;
    }


    @Override
    public String toString() {
        ArrayList<String> record_str = new ArrayList<>();
        record_str.add(Integer.toString(ID));
        String temp = empty ? "1" : "0";
        record_str.add(temp);
        record_str.addAll(Arrays.asList(field_list));
        return String.join("-", record_str);
    }
}
