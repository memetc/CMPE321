import java.io.*;

public class FileManager {
    static final String DB_FILE = "datafile.txt";
    static final String SYS_CAT_FILE = "dictionary.txt";
    static final String FREE_SPACE = "free";
    static final String PAGE_DELIMETER = "_"; //
    static final String FIELD_DELIMETER = "-";
    static final int NUM_OF_FIELDS = 5;
    static final int DATABASE_SIZE = 80000;
    static final int PAGE_SIZE = 1600;
    static final int PAGE_PER_FILE = DATABASE_SIZE / PAGE_SIZE;
    static final int TYPE_PER_PAGE = 10;
    static final int RECORD_PER_PAGE = 30;


    public static String[] getPages(File file) throws  IOException{
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        if ((line = br.readLine()) != null) {
            return line.split(PAGE_DELIMETER);
        } else {
            throw new IOException();
        }
    }

    public static void writePages(File file, String[] pages) throws FileNotFoundException {
        PrintWriter out = new PrintWriter( file );
        String data = String.join(PAGE_DELIMETER, pages);
        out.print(data);
        out.close();

    }
}
