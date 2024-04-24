package application.clientApp.client;

import application.FileSystem;

import java.io.*;
import java.util.Scanner;

public class ClientFileSystemHandler implements FileSystem {

    private final static String FILE_BY_NAME_INPUT = "1";
    private final static String FILE_BY_ID_INPUT = "2";
    private final static String INVALID_FILE_IDENTIFICATION_VALUE = "Invalid input";

    private final static String FILE_NAME_REQUEST = "Enter file name: ";
    private final static String FILE_ID_REQUEST = "Enter id: ";

    private final static int VALID_NAME_IDENTIFICATION_INPUT = 1;
    private final static int VALID_ID_IDENTIFICATION_INPUT = 2;
    private final static int INVALID_IDENTIFICATION_INPUT = -1;

    private final static int REQUEST_SUCCESS = 200;
    private final static int REQUEST_FAILED = 404;

    private final Scanner scanner = new Scanner(System.in);
    private final String clientDirectory;

    private String fileIDValidation;

    public ClientFileSystemHandler(String clientDirectory) {
        this.clientDirectory = clientDirectory;
    }

    @Override
    public void getFile(DataInputStream input, DataOutputStream output) {

        try  {

            int fileIdentification = fileIdentificationInput();

            if (fileIdentification != INVALID_IDENTIFICATION_INPUT) {

                System.out.print(this.fileIDValidation);
                String fileID = this.scanner.nextLine().trim();

                output.writeUTF(String.valueOf(fileIdentification));
                output.writeUTF(fileID);

                System.out.println("The request was sent.");

                int response = input.readInt();

                if (response == REQUEST_SUCCESS) {

                    int fileLength = input.readInt();
                    byte[] byteFile = new byte[fileLength];
                    input.readFully(byteFile, 0, fileLength);

                    System.out.print("The file was downloaded! Specify a name for it: ");
                    String fileName = this.scanner.nextLine().trim();

                    File file = prepareFileForDirectory(fileName);

                    if (file != null && file.createNewFile()) {
                        saveFileToDirectory(file, byteFile);
                        System.out.println("File saved on the hard drive!");
                    } else {
                        System.out.println("File with name \"" + fileName + "\" already exists in directory.");
                    }

                } else {
                    System.out.println("The response says that this file is not found!");
                }

            } else {
                outputClientRequestCrashMessage(output, this.fileIDValidation);
                int response = input.readInt();
                System.out.println("[CLIENT] Could not retrieve file from server: " + response);
            }

        } catch (IOException e) {
            System.err.println("[CLIENT] Error returning file from server: " + e.getMessage());
        }
    }

    @Override
    public void saveFile(DataInputStream input, DataOutputStream output) {

        try {

            System.out.print("Enter name of the file: ");
            String fileName = this.scanner.nextLine().trim();
            File file = getFileFromDirectory(fileName);

            if (file != null) {

                System.out.print("Enter name of the file to be saved on server: ");
                String fileNameOnServer = this.scanner.nextLine().trim();

                if (fileNameOnServer.isEmpty()) {
                    fileNameOnServer = fileName;
                }

                byte[] byteFile = readFileFromDirectory(file);
                int fileLength = byteFile.length;

                output.writeUTF(fileNameOnServer);
                output.writeInt(fileLength);
                output.write(byteFile);

                System.out.println("The request was sent.");
                int response = input.readInt();

                if (response == REQUEST_SUCCESS) {

                    int id = input.readInt();

                    System.out.println("Response says that file is saved! ID = " + id);

                } else {
                    System.out.println("Response says file could not be saved on server.");
                }
            } else {
                outputClientRequestCrashMessage(output, INVALID_FILE_IDENTIFICATION_VALUE);
                int response = input.readInt();
                System.out.println("[CLIENT] The file does not exist in server directory: " + response);
            }

        } catch (IOException e) {
            System.err.println("[CLIENT] Error saving file on server: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(DataInputStream input, DataOutputStream output) {

        try {

            int fileIdentification = fileIdentificationInput();

            if (fileIdentification != INVALID_IDENTIFICATION_INPUT) {

                System.out.print(this.fileIDValidation);
                String fileID = this.scanner.nextLine().trim();

                output.writeUTF(String.valueOf(fileIdentification));
                output.writeUTF(fileID);

                int response = input.readInt();

                if (response == REQUEST_SUCCESS) {
                    System.out.println("The response says that this file was deleted successfully!");
                } else {
                    System.out.println("The response says that this file is not found!");
                }
            } else {
                outputClientRequestCrashMessage(output, this.fileIDValidation);
                int response = input.readInt();
                System.out.println("[CLIENT] Invalid file identification fileIdentification: " + response);
            }

        } catch (IOException e) {
            System.err.println("[CLIENT] Error deleting file from server: " + e.getMessage());
        }
    }

    private byte[] readFileFromDirectory(File file) {

        byte[] byteFile = new byte[1024];

        try (FileInputStream in = new FileInputStream(file);
             ByteArrayOutputStream outArr = new ByteArrayOutputStream()) {

            int bytesRead;
            while ((bytesRead = in.read(byteFile)) != -1) {
                outArr.write(byteFile, 0, bytesRead);
            }
            byteFile = outArr.toByteArray();

        } catch (IOException e) {
            System.err.println("[CLIENT] Error reading from file \"" + file.getName() + "\": " + e.getMessage());
        }

        return byteFile;
    }

    private void saveFileToDirectory(File file, byte[] byteFile) {

        try (FileOutputStream out = new FileOutputStream(file)) {

            out.write(byteFile, 0, byteFile.length);

        } catch (IOException e) {
            System.err.println("[CLIENT] Error saving file \"" + file.getName() + "\" to client directory: " + e.getMessage());
        }
    }

    private File prepareFileForDirectory(String fileName) {
        File file = new File(this.clientDirectory + File.separator + fileName);
        return (file.exists()) ? null : file;
    }

    private File getFileFromDirectory(String fileName) {
        File file = new File(this.clientDirectory + File.separator + fileName);
        return (file.exists()) ? file : null;
    }

    private int fileIdentificationInput() {

        System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
        String fileIdentification = this.scanner.nextLine().trim();

        switch (fileIdentification) {

            case FILE_BY_NAME_INPUT:
                this.fileIDValidation = FILE_NAME_REQUEST;
                return VALID_NAME_IDENTIFICATION_INPUT;

            case FILE_BY_ID_INPUT:
                this.fileIDValidation = FILE_ID_REQUEST;
                return VALID_ID_IDENTIFICATION_INPUT;

            default:
                this.fileIDValidation = INVALID_FILE_IDENTIFICATION_VALUE;
                return INVALID_IDENTIFICATION_INPUT;
        }
    }

    private void outputClientRequestCrashMessage(DataOutputStream output, String message) throws IOException {
        output.writeUTF(message);
    }
}
