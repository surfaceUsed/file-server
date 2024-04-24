package application.serverApp;

import application.serverApp.server.Server;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(5555)) {

            new Server(serverSocket).run();

        } catch (IOException e) {
            System.err.println("[SERVER] Error establishing server: " + e.getMessage());
        }
    }
}