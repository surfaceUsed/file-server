package application.serverApp.server.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class ServerFileLoader implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    public final static String USER_DATA = "user.dir";
    public final static String SERVER_DATA = "server.dir";
    public final static String FILE_NAME_MAPPER_OBJECT = "data.ser";

    private final static Properties PROPERTIES = new Properties();

    private static Map<Integer, String> FILES_ID_MAPPER = new HashMap<>();
    // File id will always increment no matter the size of the hashMap.
    private static int fileID;

    private ServerFileLoader() {}

    static {
        loadProperties();

        deserialize();
    }

    // TODO: enter valid path to fileSystem.
    private static void loadProperties() {
        PROPERTIES.put(USER_DATA, "...src\\application\\serverApp\\server\\data\\");

        PROPERTIES.put(SERVER_DATA, "...src\\application\\serverApp\\server\\data\\maploader");
    }

    public static String getDir(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static int generateID() {
        fileID++;
        return fileID;
    }

    public static void addFile(String fileName) {
        FILES_ID_MAPPER.put(generateID(), fileName);
    }

    public static void removeFile(int fileID) {
        FILES_ID_MAPPER.remove(fileID);
    }

    public static int getKey(String value) {

        for (Integer i : FILES_ID_MAPPER.keySet()) {
            if (FILES_ID_MAPPER.get(i).equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public static String getValue(int key) {
        return FILES_ID_MAPPER.get(key);
    }

    public synchronized static void serialize(String fileName) {

        try (FileOutputStream out = new FileOutputStream(createFile(fileName));
             BufferedOutputStream buff = new BufferedOutputStream(out);
             ObjectOutputStream ob = new ObjectOutputStream(buff)) {

            ob.writeObject(FILES_ID_MAPPER);
            ob.writeInt(fileID);

        } catch (IOException e) {
            System.err.println("[SERVER] Error serializing file: " + e.getMessage());
        }
    }

    private synchronized static void deserialize() {

        try (FileInputStream in = new FileInputStream(createFile(FILE_NAME_MAPPER_OBJECT));
             BufferedInputStream buff = new BufferedInputStream(in);
             ObjectInputStream ob = new ObjectInputStream(buff)) {

            FILES_ID_MAPPER = (Map<Integer, String>) ob.readObject();
            fileID = ob.readInt();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[SERVER] Error, no object to deserialize: " + e.getMessage());
        }

    }

    private static File createFile(String fileName) {
        return new File(getDir(SERVER_DATA) + File.separator + fileName);
    }
}


























/*private synchronized static Map<Integer, String> deserialize() {

        Map<Integer, String> fileMapper = new HashMap<>();

        try (FileInputStream in = new FileInputStream(createFile(FILE_NAME_MAPPER_OBJECT));
             BufferedInputStream buff = new BufferedInputStream(in);
             ObjectInputStream ob = new ObjectInputStream(buff)) {

            fileMapper = (Map<Integer, String>) ob.readObject();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[SERVER] Error, no object to deserialize: " + e.getMessage());
        }

        return fileMapper;
    }

     */
