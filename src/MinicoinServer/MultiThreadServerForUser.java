package MinicoinServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadServerForUser extends Thread {
    private static MultiThreadServerForUser instance = null;

    private ServerSocket serverSocket;

    private Storage storage;

    private MultiThreadServerForUser(Storage storage) {
        this.storage = storage;
        try {
            this.serverSocket = new ServerSocket(10010);
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    // Singleton
    public static synchronized MultiThreadServerForUser getInstance(Storage storage) {
        if (instance == null) {
            instance = new MultiThreadServerForUser(storage);
        }
        return instance;
    }

    @Override
    public void run() {
        System.out.println("Server for User is running.");
        while (true) {
            try {
                System.out.println("Listening...");
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("Success to Connect: " + clientSocket.getRemoteSocketAddress());
                ServerForUser newServer = new ServerForUser(clientSocket, storage);
                newServer.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
