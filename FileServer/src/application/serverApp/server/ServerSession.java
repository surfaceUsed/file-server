package application.serverApp.server;

import application.HTTPRequest;
import application.FileSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerSession implements Runnable {

    private final Socket socket;
    private final Server server;
    private final FileSystem fileHandler;

    private boolean isExit = false;

    public ServerSession(Socket socket, Server server, FileSystem fileHandler) {
        this.socket = socket;
        this.server = server;
        this.fileHandler = fileHandler;
    }

    @Override
    public void run() {

        try (DataInputStream input = new DataInputStream(this.socket.getInputStream());
             DataOutputStream output = new DataOutputStream(this.socket.getOutputStream())) {

            while (!this.isExit) {

                handleRequest(input, output);

            }

        } catch (IOException e) {
            System.err.println("[SERVER] Error handling request from client: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void handleRequest(DataInputStream input, DataOutputStream output) throws IOException {

        String clientRequest = input.readUTF();

        HTTPRequest httpRequest = HTTPRequest.getRequest(clientRequest);

        switch (httpRequest) {

            case GET:
                this.fileHandler.getFile(input, output);
                break;

            case PUT:
                this.fileHandler.saveFile(input, output);
                break;

            case DELETE:
                this.fileHandler.deleteFile(input, output);
                break;

            case EXIT:
                this.isExit = true;
                this.server.stopServer();
                break;

            default:
                break;
        }
    }

    private void closeConnection() {
        this.isExit = true;
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Error closing socket connection: " + e.getMessage());
        }
    }
}
