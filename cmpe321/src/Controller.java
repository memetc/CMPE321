import java.io.*;

public class Controller {
    static final String DB_FILE = "datafile.txt";
    static final String SYS_CAT_FILE = "dictionary.txt";
    static final String FREE_SPACE = "free";
    static final String PAGE_DELIMETER = "_";
    static final String FIELD_DELIMETER = "-";
    static final String RECORD_DELIMETER = ",";
    static final int NUM_OF_FIELDS = 5;
    static final int DATABASE_SIZE = 80000;
    static final int PAGE_SIZE = 1600;
    static final int PAGE_PER_FILE = DATABASE_SIZE / PAGE_SIZE;
    static final int TYPE_PER_PAGE = 10;
    static final int RECORD_PER_PAGE = 30;


    public static String[] read_pages(File file) throws  IOException{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        if ((line = reader.readLine()) != null) {
            return line.split(PAGE_DELIMETER);
        } else {
            throw new IOException();
        }
    }

    public static void write_pages(File file, String[] pages) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter( file );
        String data = String.join(PAGE_DELIMETER, pages);
        writer.print(data);
        writer.close();
    }
}
