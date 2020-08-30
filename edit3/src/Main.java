import java.util.*;
import java.io.*;

public class Main {
    static File dictionary;
    static Scanner console = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome!");
        File database_file = new File("datafile.txt");
        if (database_file.exists()) {
            System.out.println("You have an old data file");
            System.out.println("Enter 'o' to overwrite");
            System.out.println("Enter 'l' to load the old file");
            String choice = console.next();
            switch (choice) {
                case "o":
                    init();
                    break;
                case "l":
                    dictionary = database_file;
                    System.out.println("Loading...");
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
            String choice = console.next();
            switch (choice) {
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
        dictionary = new File("datafile.txt");
        try ( PrintWriter printWriter = new PrintWriter(dictionary)){
            Page disc_page = new Page();
            disc_page.add_file("free.txt", (short)2);
            disc_page.add_file("dictionary.txt", (short)1);
            printWriter.print(disc_page);
            short number_of_pages = Controller.PAGE_PER_FILE;
            for(short i = 1; i < number_of_pages; i++) {
                printWriter.print("_");
                printWriter.print(i);
                printWriter.print("-");
                printWriter.print("");
                printWriter.print("-");
                printWriter.print("0");
                printWriter.print("-");
                if (i == 1) {
                    printWriter.print("0");

                } else {
                    printWriter.print("1");
                }
            }
        } catch (IOException e) {
            System.out.println("exception");
        }
        System.out.println("Data file is created.");
    }


    public static void typeOp() {
        System.out.println("Enter 'c' to create a new type");
        System.out.println("Enter 'd' to delete a type");
        System.out.println("Enter 'l' to list all types");
        String choice = console.next();
        switch (choice) {
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
        String choice = console.next();
        switch (choice) {
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