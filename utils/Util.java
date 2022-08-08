package utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Util {

    /**
     * output to console (or specified stream)
     * call setupOut(desiredOutStream) before using pr/prln to use desiredOutStream,
     * otherwise System.out stream will be used
     */
    private static PrintStream out = null;
    public static void setOut(PrintStream o) {
        out = o;
    }

    private static PrintStream setupOut() {
        if (out == null) {
            out = System.out;
        }
        return out;
    }
    public static void pr(String s) {
        setupOut().print(s);
    }

    public static void prln(String s) {
        setupOut().println(s);
    }



    /**
     * get input from console in (or specified stream)
     * call setupScanner(desiredInStream) before using scanInt/scanString/scanLine to use desiredInStream,
     * otherwise System.in stream will be used
     */
    private static Scanner scanner = null;
    public static void setScanner(Scanner s) {
        scanner = s;
    }

    private static Scanner setupScanner() {
        if (scanner == null) {
            scanner = new Scanner(System.in);
        }
        return scanner;
    }

    public static int scanInt() {
        return setupScanner().nextInt();
    }

    public static String scanString() {
        return setupScanner().next();
    }

    public static String scanLine() {
        return setupScanner().nextLine();
    }

    /**
     * Serialize the given object to the file
     */
    public static void serializeToFile(Object obj, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }

    /**
     * Deserialize to an object from the file
     */
    public static Object deserialize(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }

    /**
     * output to logfile
     * call setLogitPrefix(STRING) before using logit to set a prefix
     */
    private static String logitPrefix = null;
    public static void logit(String message) {
        try (FileWriter fileWriter = new FileWriter("../../../loggy.txt", true)){
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(logitPrefix == null ? message : logitPrefix + message);
        } catch (Exception ex) {
        }
    }

    public static void setLogitPrefix(String prefix) {
        logitPrefix = prefix;
    }

    public static void prlnLogged(String message) {
        prln(message);
        logit(message);
    }

    /**
     * read a file into a string
     */
    public static String readFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

}
