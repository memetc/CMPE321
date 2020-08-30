
import java.util.*;
import java.io.*;


public class Type {
    static Scanner console = new Scanner(System.in);
    String name;
    boolean empty;
    short num_of_fields;
    String[] field_list;

    public Type(String str_name, short num_of_fields, String[] field_list) {
        this.name = str_name;
        this.empty = false;
        this.num_of_fields = num_of_fields;
        this.field_list = field_list.clone();
    }

    public Type(String str) {
        String[] fields = str.split("-");

        this.name = fields[0];
        this.empty = "1".equals(fields[1]);
        this.num_of_fields = Short.valueOf(fields[2]);
        this.field_list = new String[this.num_of_fields];
        for (int i = 3; i < fields.length; i++) {
            this.field_list[i-3] = fields[i];
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
            short num_fields = console.nextShort();
            String[] fields = new String[num_fields];
            for (int i = 0; i < num_fields; i++) {
                System.out.println("Enter name for field " + (i+1));
                fields[i] = console.next();
            }
            Type type = new Type(type_name, num_fields, fields);
            String[] table = Controller.read_pages(file);
            Page disc_page = new Page(table[0]);
            short system_address = disc_page.find_address("dictionary.txt");
            short save_address = disc_page.add_file(type_name + ".txt");
            table[0] = disc_page.toString();
            Controller.write_pages(file, table);
            Page record_page = new Page(table[save_address]);
            record_page.empty = false;
            table[save_address] = record_page.toString();
            Controller.write_pages(file, table);
            Page system_page = new Page(table[system_address]);
            while(system_page.next != 0) {
                system_address = system_page.next;
                system_page = new Page(table[system_address]);
            }
            if (system_page.size == Controller.TYPE_PER_PAGE) {
                short empty_space = disc_page.get_free_address();
                table[0] = disc_page.toString();
                Controller.write_pages(file, table);
                system_page.next = empty_space;
                table[system_address] = system_page.toString();
                Controller.write_pages(file, table);
                system_page = new Page(table[empty_space]);
                system_page.empty = false;
                system_page.size++;
                system_page.record_list.add(type.toString());
                table[empty_space] = system_page.toString();
                Controller.write_pages(file, table);
            }
            else {
                system_page.record_list.add(type.toString());
                system_page.size++;
                table[system_address] = system_page.toString();
                Controller.write_pages(file, table);
            }
            System.out.println();
            System.out.println("The database is updated");
        } catch (IOException e) {
            System.out.println("Exception");
            System.exit(0);
        }
    }

    public static void delete(File file) {
        try {
            ArrayList<Type> types = get_types(file);
            ArrayList<String> type_names = new ArrayList<String>();
            System.out.println();
            System.out.println(types.size() + " types are found");
            for (int i = 0; i < types.size(); i++) {
                System.out.println("\t\t[" + types.get(i).name + "]");
                System.out.println("\t\t Number of Fields: " + types.get(i).num_of_fields);
                System.out.println("\t\t Fields: " + String.join(" / ", types.get(i).field_list));
                type_names.add(types.get(i).name);
            }

            System.out.println("Enter the name of the type that you want to delete");
            String target_type = console.next();
            short index = (short)type_names.indexOf(target_type);
            Type type = types.get(index);
            String[] table = Controller.read_pages(file);
            Page disc_page = new Page(table[0]);
            short dictionary_address = disc_page.find_address("dictionary.txt");
            short target_address = disc_page.delete_file(type.name + ".txt");
            table[0] = disc_page.toString();
            Controller.write_pages(file, table);
            Page record_page = new Page(table[target_address]);
            record_page.empty = true;
            record_page.size = 0;
            short next = record_page.next;
            record_page.next = 0;
            record_page.record_list.clear();
            table[target_address] = record_page.toString();
            Controller.write_pages(file, table);

            while(next != 0) {
                target_address = next;
                record_page = new Page(table[target_address]);
                record_page.empty = true;
                record_page.size = 0;
                next = record_page.next;
                record_page.next = 0;
                record_page.record_list.clear();
                table[target_address] = record_page.toString();
                Controller.write_pages(file, table);
            }

            Page dictionary_page = new Page(table[dictionary_address]);
            int position = -1;
            while(position == -1) {
                for (int i = 0; i < dictionary_page.record_list.size(); i++) {
                    if (dictionary_page.record_list.get(i).equalsIgnoreCase(type.toString())) {
                        position = i;
                        break;
                    }
                }
                if (dictionary_page.next == 0) {
                    break;
                }
                dictionary_address = dictionary_page.next;
                dictionary_page = new Page(table[dictionary_address]);
            }
            if (position == -1) {
                System.out.println("Couldn't find type");
            } else {
                type.empty = true;
                dictionary_page.record_list.set(position, type.toString());
                dictionary_page.size--;
                table[dictionary_address] = dictionary_page.toString();
                Controller.write_pages(file, table);
                System.out.println();
                System.out.println("Type \"" + type.name + "\" is deleted from database successfully.");
            }
        } catch (IOException e) {
            System.out.println("Couldn't read line from text");
            System.exit(0);
        }
    }
    public static ArrayList<Type> get_types(File file) {
        ArrayList<Type> types = new ArrayList<>();
        try {
            String[] pages = Controller.read_pages(file);
            Page disc_page = new Page(pages[0]);

            Page dictionary_page = new Page(pages[disc_page.find_address("dictionary.txt")]);
            for ( String str :  dictionary_page.record_list) {
                Type type = new Type(str);
                if (!type.empty) {
                    types.add(type);
                }
            }
            while (dictionary_page.next != 0) {
                dictionary_page = new Page(pages[dictionary_page.next]);
                for ( String str :  dictionary_page.record_list) {
                    Type type = new Type(str);
                    if (!type.empty) {
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

    public static void list(File file) {
        ArrayList<Type> types = get_types(file);
        for (int i = 0; i < types.size(); i++) {
            System.out.println("\t\t [" + types.get(i).name + "]");
            System.out.println("\t\t Number of Fields: " + types.get(i).num_of_fields);
            System.out.println("\t\t Fields: " + String.join(" / ", types.get(i).field_list));
        }
    }



    @Override
    public String toString() {
        ArrayList<String> str_result = new ArrayList<String>();
        str_result.add(name);
        String temp = empty ? "1": "0";
        str_result.add(temp);
        str_result.add(Integer.toString(num_of_fields));
        str_result.addAll(Arrays.asList(field_list));
        return String.join("-", str_result);
    }
}
