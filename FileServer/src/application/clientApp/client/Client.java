package application.clientApp.client;

import application.HTTPRequest;
import application.FileSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private final Socket socket;
    private final FileSystem fileHandler;

    private boolean isExit = false;

    public Client(Socket socket, FileSystem fileHandler) {
        this.socket = socket;
        this.fileHandler = fileHandler;
    }

    public void run() {

        try (DataInputStream input = new DataInputStream(this.socket.getInputStream());
             DataOutputStream output = new DataOutputStream(this.socket.getOutputStream());
             Scanner scanner = new Scanner(System.in)) {

            while (!this.isExit) {

                System.out.print("Enter action (1 - get a file, 2 - save a file, 3 - delete a file): ");

                String clientRequest = scanner.nextLine().trim();

                HTTPRequest request = HTTPRequest.getRequest(clientRequest);

                switch (request) {

                    case GET:
                        output.writeUTF(clientRequest);
                        this.fileHandler.getFile(input, output);
                        break;
                        
                        case PUT:
                            output.writeUTF(clientRequest);
                            this.fileHandler.saveFile(input, output);
                            break;

                        case DELETE:
                            output.writeUTF(clientRequest);
                            this.fileHandler.deleteFile(input, output);
                            break;

                        case EXIT:
                            this.isExit = true;
                            output.writeUTF(clientRequest);
                            closeConnection();
                            System.out.println("The request was sent");
                            break;

                        case INVALID_REQUEST:
                            System.out.println("Invalid request!");
                            break;
                    }
                }

        } catch (IOException e) {
            System.err.println("[CLIENT] Error connecting to server: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Error closing socket connection: " + e.getMessage());
        }
    }
}
