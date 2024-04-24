package application.serverApp.server;

import application.FileSystem;
import application.serverApp.server.util.ServerFileLoader;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final static FileSystem FILE_HANDLER = ServerFileSystemHandler.getFileHandler();

    private static volatile boolean isExit = false;

    private final ServerSocket serverSocket;
    private final ExecutorService executor;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.executor = Executors.newFixedThreadPool(4);
    }

    public void run() {

        try {

            System.out.println("Server is running..");

            while (!isExit) {

                System.out.println("Waiting for client..");
                Thread session = new Thread(new ServerSession(this.serverSocket.accept(), this, FILE_HANDLER));
                this.executor.submit(session);
                System.out.println("Client connected!");
                Thread.sleep(1000);
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Socket connection closed: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected synchronized void stopServer() {
        if (!isExit) {
            isExit = true;
            ServerFileLoader.serialize(ServerFileLoader.FILE_NAME_MAPPER_OBJECT);
            closeConnection();
            System.out.println("Server is closed.");
            System.exit(0);
        }
    }

    private void closeConnection() {

        this.executor.shutdown();
        try {

            if (this.serverSocket != null) {
                this.serverSocket.close();
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Error closing server socket connection: " + e.getMessage());
        }
    }
}
