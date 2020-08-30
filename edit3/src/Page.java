import java.util.ArrayList;
import java.util.Arrays;

public class Page {
    short ID;
    boolean empty;
    short next;
    short size;
    ArrayList<String> record_list = new ArrayList<>();

    public Page() {
        this.ID = 0;
        this.empty = false;
        this.next = 0;
        this.size = 0;

    }

    public Page(String str) {
        String[] sections = str.split(",");
        String[] header_sections = sections[0].split("-");
        this.ID = Short.valueOf(header_sections[0]);
        this.size = Short.valueOf(header_sections[2]);
        this.empty = "1".equals(header_sections[3]);
        if (header_sections[1].isEmpty()) {
            this.next = 0;
        } else {
            this.next = Short.valueOf(header_sections[1]);
        }
        this.record_list = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(sections, 1, sections.length)));
    }

    public short add_file(String fileName) {
        short address = this.get_free_address();
        record_list.add(fileName + "-" + address);
        return address;
    }

    public void add_file(String fileName, short ID) {
        record_list.add(fileName + "-" + ID);
    }

    public short delete_file(String fileName){
        short address = 0;
        for (int i = 0; i < record_list.size(); i++) {
            String[] fields = record_list.get(i).split("-");
            if (fields[0].equalsIgnoreCase(fileName)) {
                address = Short.valueOf(fields[1]);
                record_list.remove(i);
                break;
            }
        }
        return address;
    }

    public short find_address(String str_file) {
        for (String i : record_list) {
            String[] fields = i.split("-");
            if (fields[0].equalsIgnoreCase(str_file)) {
                return Short.valueOf(fields[1]);
            }
        }
        return 0;
    }

    public short get_free_address() {
        short address = this.find_address("free.txt");
        this.set_free_address((short)(address + 1));
        return address;
    }

    public void set_free_address(short address) {
        for (int i = 0; i < record_list.size(); i++) {
            String[] fields = record_list.get(i).split("-");
            if (fields[0].equalsIgnoreCase("free.txt")) {
                record_list.set(i, fields[0] + "-" + address);
            }
        }
    }

    public String construct_header() {
        String[] fields = {Short.toString(this.ID), next == 0 ? "" : Short.toString(this.next), Short.toString(this.size),this.empty == false ? "0" : "1"};
        return String.join("-", fields);
    }
    @Override
    public String toString() {
        String str = this.construct_header();
        if (!record_list.isEmpty()){
            str += ",";
            str += String.join(",", record_list);
        }
        return str;
    }

}

