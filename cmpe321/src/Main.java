import java.util.*;
import java.io.*;

public class Main {
    static File dictionary;
    static Scanner console = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome!");
        File db = new File(FileManager.DB_FILE);
        if (db.exists()) {
            System.out.println("You have an old data file");
            System.out.println("Enter 'o' to overwrite");
            System.out.println("Enter 'l' to load the old file");
            String choice = console.next();
            switch (choice) {
                case "o":
                    init();
                    break;
                case "l":
                    dictionary = db;
                    System.out.println("Old database file is used");
                    break;
            }
        }
        else{
            init();
        }
        while (true) {
            System.out.println("Enter 't' for type operations");
            System.out.println("Enter 'r' for record operations");
            System.out.println("Enter 'q' to quit the storage manager");
            String category = console.next();
            switch (category) {
                case "t":
                    typeOp();
                    break;
                case "r":
                    recordOp();
                    break;
                case "q":
                    System.out.println("Quitting...");
                    System.exit(0);
                    break;
            }
        }
    }


    public static void init() {
        dictionary = new File(FileManager.DB_FILE);
        try ( PrintWriter out = new PrintWriter(dictionary)){
            // First page always is Disc Directory file
            Page discDirectory = new Page();
            discDirectory.addFile(FileManager.FREE_SPACE, 2);
            discDirectory.addFile(FileManager.SYS_CAT_FILE, 1);
            out.print(discDirectory);
            int totalPageCount = FileManager.PAGE_PER_FILE;
            // Add empty page with ID
            for(int i = 1; i < totalPageCount; i++) {
                out.print(FileManager.PAGE_DELIMETER);
                out.print(i); // Page ID
                out.print(FileManager.FIELD_DELIMETER);
                out.print(""); // Pointer to next page
                out.print(FileManager.FIELD_DELIMETER);
                out.print("0"); // Number of records
                out.print(FileManager.FIELD_DELIMETER);
                if (i == 1) {
                    out.print("0"); // Sys is full

                } else {
                    out.print("1"); // Empty page
                }
            }
        } catch (IOException e) {
            System.out.println("exception");
        }
        System.out.println(FileManager.DB_FILE + " is created.");
    }


    public static void typeOp() {
        System.out.println("Enter 'c' to create a new type");
        System.out.println("Enter 'd' to delete a type");
        System.out.println("Enter 'l' to list all types");
        String operation = console.next();
        switch (operation) {
            case "c":
                Type.create(dictionary);
                break;
            case "d":
                Type.delete(dictionary);
                break;
            case "l":
                Type.list(dictionary);
                break;
        }
    }

    public static void recordOp() throws IOException {
        System.out.println("Enter 'c' to create a new record");
        System.out.println("Enter 'd' to delete a record");
        System.out.println("Enter 'r' to retrieve a record");
        System.out.println("Enter 'l' to list all records of a type");
        String operation = console.next();
        switch (operation) {
            case "c":
                Record.create(dictionary);
                break;
            case "d":
                Record.delete(dictionary);
                break;
            case "r":
                Record.retrieve(dictionary);
                break;
            case "l":
                Record.list(dictionary);
                break;
        }

    }
}