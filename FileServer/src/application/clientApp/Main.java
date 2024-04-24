package application.clientApp;

import application.clientApp.client.Client;
import application.clientApp.client.ClientFileSystemHandler;

import java.io.IOException;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {

        try (Socket socket = new Socket("localhost", 5555)) {

            new Client(socket, new ClientFileSystemHandler(args[0])).run();

        } catch (IOException e) {
            System.err.println("[CLIENT] Connection to server shut down: " + e.getMessage());
        }
    }
}
