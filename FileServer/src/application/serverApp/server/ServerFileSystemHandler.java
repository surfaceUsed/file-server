package application.serverApp.server;

import application.FileSystem;
import application.serverApp.server.util.ServerFileLoader;

import java.io.*;
import java.util.InputMismatchException;

class ServerFileSystemHandler implements FileSystem {

    private final static FileSystem FILE_HANDLER = new ServerFileSystemHandler();
    private final static String USER_DIRECTORY = ServerFileLoader.getDir(ServerFileLoader.USER_DATA);
    private final static String INVALID_INPUT = "Invalid input";
    private final static int REQUEST_SUCCESS = 200;
    private final static int REQUEST_FAILED = 404;
    private final static int ERROR_SAVING_FILE = -1;
    private final static String FILE_BY_NAME_INPUT = "1";
    private final static String FILE_BY_ID_INPUT = "2";

    private final Object readLock = new Object();

    private ServerFileSystemHandler() {}

    static FileSystem getFileHandler() {
        return FILE_HANDLER;
    }

    @Override
    public void getFile(DataInputStream input, DataOutputStream output) {

        try {

            String idOrName = input.readUTF();

            if (idOrName.equals(INVALID_INPUT)) {
                System.err.println("[SERVER] Invalid option input from client.");
                output.writeInt(REQUEST_FAILED);

            } else {

                String fileID = input.readUTF();

                fileID = getFileByIdOrName(idOrName, fileID);

                if (!fileID.equals(INVALID_INPUT)) {

                    synchronized (readLock) {

                        File file = findFile(fileID);

                        if (file != null) {
                            output.writeInt(REQUEST_SUCCESS);
                            byte[] byteFile = readFileFromServer(file);
                            int fileLength = byteFile.length;
                            output.writeInt(fileLength);
                            output.write(byteFile);
                            System.out.println("[SERVER] File \"" + fileID + "\" sent successfully!");
                        } else {
                            System.err.println("[SERVER] Could not find file on server.");
                            output.writeInt(REQUEST_FAILED);
                        }
                    }

                } else {
                    System.err.println("[SERVER] Invalid file identification from client.");
                    output.writeInt(REQUEST_FAILED);
                }
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Error GET-request: " + e.getMessage());
        }
    }

    @Override
    public void saveFile(DataInputStream input, DataOutputStream output) {

        try {

            String fileName = input.readUTF();

            if (fileName.equals(INVALID_INPUT)) {
                System.err.println("[SERVER] File not found in client directory.");
                output.writeInt(REQUEST_FAILED);

            } else {

                int fileLength = input.readInt();
                byte[] byteFile = new byte[fileLength];
                input.readFully(byteFile, 0, fileLength);

                int id;
                synchronized (readLock) {
                    File file = prepareFileForDirectory(fileName);
                    id = (file.createNewFile()) ? saveFileOnServer(file, byteFile) : ERROR_SAVING_FILE;
                }

                if (id > ERROR_SAVING_FILE) {
                    output.writeInt(REQUEST_SUCCESS);
                    output.writeInt(id);
                    System.out.println("[SERVER] File \"" + fileName + "\" saved successfully on server");
                } else {
                    System.err.println("[SERVER] Could not save file to server.");
                    output.writeInt(REQUEST_FAILED);
                }
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Error PUT-request: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(DataInputStream input, DataOutputStream output) {

        try {

            String idOrName = input.readUTF();

            if (idOrName.equals(INVALID_INPUT)) {
                output.writeInt(REQUEST_FAILED);

            } else {

                String fileID = input.readUTF();

                fileID = getFileByIdOrName(idOrName, fileID);

                if (!fileID.equals(INVALID_INPUT)) {

                    synchronized (readLock) {

                        File file = findFile(fileID);

                        if (file != null) {

                            if (file.delete()) {
                                output.writeInt(REQUEST_SUCCESS);
                                ServerFileLoader.removeFile(ServerFileLoader.getKey(fileID));
                                System.out.println("[SERVER] File \"" + fileID + "\" deleted successfully from server.");
                            }

                        } else {
                            System.err.println("[SERVER] File not found.");
                            output.writeInt(REQUEST_FAILED);
                        }
                    }

                } else {
                    System.err.println("[SERVER] Invalid file identification from client.");
                    output.writeInt(REQUEST_FAILED);
                }
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Error DELETE-request: " + e.getMessage());
        }
    }

    private File prepareFileForDirectory(String fileName) {
        return new File(USER_DIRECTORY + File.separator + fileName);
    }

    private int saveFileOnServer(File file, byte[] byteFile) {

        int fileID = -1;

        try (FileOutputStream out = new FileOutputStream(file)) {

            String fileName = file.getName();
            out.write(byteFile, 0, byteFile.length);
            ServerFileLoader.addFile(fileName);
            fileID = ServerFileLoader.getKey(fileName);

        } catch (IOException e) {
            System.err.println("[SERVER] Error saving file \"" + file.getName() + "\" on server: " + e.getMessage());
        }

        return fileID;
    }

    private File findFile(String fileName) {
        File file = new File(USER_DIRECTORY + File.separator + fileName);
        return (file.exists()) ? file : null;
    }

    private byte[] readFileFromServer(File file) {

        byte[] byteFile = new byte[1024];

        try (FileInputStream input = new FileInputStream(file);
             ByteArrayOutputStream outArr = new ByteArrayOutputStream()) {

            int bytesRead;

            while ((bytesRead = input.read(byteFile)) != -1) {
                outArr.write(byteFile, 0, bytesRead);
            }

            byteFile = outArr.toByteArray();

        } catch (IOException e) {
            System.err.println("[SERVER] Error reading file \"" + file.getName() + "\":" + e.getMessage());
        }

        return byteFile;
    }

    private String getFileByIdOrName(String idOrName, String fileID) {

        switch (idOrName) {

            case FILE_BY_NAME_INPUT:
                return fileID;

            case FILE_BY_ID_INPUT:
                try {
                    return (ServerFileLoader.getValue(Integer.parseInt(fileID)) != null) ?
                            ServerFileLoader.getValue(Integer.parseInt(fileID)) : INVALID_INPUT;
                } catch (InputMismatchException e) {
                    System.err.println("[SERVER] Error parsing file id: " + e.getMessage());
                }

            default:
                return INVALID_INPUT;
        }
    }
}
