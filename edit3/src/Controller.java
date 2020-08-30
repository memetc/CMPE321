import java.io.*;

public class Controller {
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
            return line.split("_");
        } else {
            throw new IOException();
        }
    }

    public static void write_pages(File file, String[] table) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(file);
        String str = String.join("_", table);
        writer.print(str);
        writer.close();
    }
}
