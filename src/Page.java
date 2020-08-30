import java.util.ArrayList;
import java.util.Arrays;


public class Page {
    PageHeader pageHeader;
    ArrayList<String> records = new ArrayList<>();

    public Page() {
        this.pageHeader = new PageHeader(0, 0, 0, false);
    }

    public Page(String str) {
        String[] parts = str.split(Controller.RECORD_DELIMETER);
        this.pageHeader = new PageHeader(parts[0]);
        this.records = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length)));
    }

    public int addFile(String fileName) {
        int address = this.getFreeAddress();
        records.add(fileName + Controller.FIELD_DELIMETER + address);
        return address;
    }

    public void addFile(String fileName, int pageID) {
        records.add(fileName + Controller.FIELD_DELIMETER + pageID);
    }

    public int removeFile(String fileName) {
        int address = 0;
        for (int i = 0; i < records.size(); i++) {
            String[] fields = records.get(i).split(Controller.FIELD_DELIMETER);
            if (fields[0].equalsIgnoreCase(fileName)) {
                address = Integer.valueOf(fields[1]);
                records.remove(i);
                break;
            }
        }
        return address;
    }

    public int getAddress(String fileName) {
        for (String record : records) {
            String[] fields = record.split(Controller.FIELD_DELIMETER);
            if (fields[0].equalsIgnoreCase(fileName)) {
                return Integer.valueOf(fields[1]);
            }
        }
        return 0;
    }

    public int getFreeAddress() {
        int freeAddress = this.getAddress(Controller.FREE_SPACE);
        this.setFreeAddress(freeAddress + 1);
        return freeAddress;
    }

    public void setFreeAddress(int address) {
        for (int i = 0; i < records.size(); i++) {
            String[] fields = records.get(i).split(Controller.FIELD_DELIMETER);
            if (fields[0].equalsIgnoreCase(Controller.FREE_SPACE)) {
                records.set(i, fields[0] + Controller.FIELD_DELIMETER + address);
            }
        }
    }

    @Override
    public String toString() {
        String str = pageHeader.toString();
        if (!records.isEmpty()) {
            str += Controller.RECORD_DELIMETER;
            str += String.join(Controller.RECORD_DELIMETER, records);
        }
        return str;
    }

}

class PageHeader {

    int pageID;
    boolean isEmpty;
    int pointer;
    int size;

    public PageHeader(int pageID,  int pointer, int size, boolean isEmpty) {

    }


    public PageHeader(String str) {
        String[] fields = str.split(Controller.FIELD_DELIMETER);
        this.pageID = Integer.valueOf(fields[0]);
        this.size = Integer.valueOf(fields[2]);
        this.isEmpty = "1".equals(fields[3]);
        if (fields[1].isEmpty()) {
            this.pointer = 0;
        } else {
            this.pointer = Integer.valueOf(fields[1]);
        }
    }

    @Override
    public String toString() {
        String[] fields = {Integer.toString(pageID), pointer == 0 ? "" : Integer.toString(pointer), Integer.toString(size), isEmpty == false ? "0" : "1"};
        return String.join(Controller.FIELD_DELIMETER, fields);
    }

}
