package MinicoinServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerForAdmin extends Thread {
    private static ServerForAdmin instance = null;

    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private Storage storage;

    private static final Set<String> INSTRUCTIONS = new HashSet<String>(Arrays.asList(
            "login", "createUser", "checkUser", "distributeMinicoin", "takeBackMinicoin", "changePassword", "exit"
    ));

    private ServerForAdmin(Storage storage) {
        this.storage = storage;
        try {
            this.serverSocket = new ServerSocket(10086);
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    // Singleton
    public static synchronized ServerForAdmin getInstance(Storage storage) {
        if (instance == null) {
            instance = new ServerForAdmin(storage);
        }
        return instance;
    }

    @Override
    public void run() {
        System.out.println("Server for Admin is running.");
        while (true) {
            try {
                System.out.println("Listening...");
                this.socket = this.serverSocket.accept();
                this.dataInputStream = new DataInputStream(this.socket.getInputStream());
                this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
                System.out.println("Success to Connect: " + this.socket.getRemoteSocketAddress());
                this.mainLoop();
                System.out.println("Over.");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void mainLoop() throws IOException {
        boolean isContinue = true;
        while (isContinue) {
            String instruction = this.getInstruction();
            if (!INSTRUCTIONS.contains(instruction)) {
                System.err.println("Unknown Instruction [" + instruction + "]");
                break;
            }
            isContinue = this.solveInstruction(instruction);
        }
    }

    private String getInstruction() throws IOException{
        return this.dataInputStream.readUTF();
    }

    private boolean solveInstruction(String instruction) throws IOException {
        System.out.println("Solve instruction [" + instruction + "]");
        switch (instruction) {
            case "login": {
                String password = this.dataInputStream.readUTF();
                if (this.checkAdminPassword(password)) {
                    this.dataOutputStream.writeUTF("success");
                } else {
                    this.dataOutputStream.writeUTF("wrongPassword");
                }
            }
            break;
            case "createUser": {
                String username = this.dataInputStream.readUTF();
                boolean isAvailable = isUsernameAvailable(username);
                if (isAvailable) {
                    System.out.println("the username [" + username + "] is available");
                    this.dataOutputStream.writeUTF("available");
                } else {
                    System.out.println("the username [" + username + "] is unavailable");
                    this.dataOutputStream.writeUTF("unavailable");
                    break;
                }
                String encryptedPassword = this.dataInputStream.readUTF();
                storage.addUser(username, encryptedPassword);
                this.dataOutputStream.writeUTF("success");

                System.out.println("Now there are " + storage.getUserListSize() + " users in system");

            }
            break;
            case "checkUser": {
                String username = this.dataInputStream.readUTF();
                boolean isExisted = isUserExisted(username);
                if (isExisted) {
                    this.dataOutputStream.writeUTF("existed");
                } else {
                    System.out.println("the user [" + username + "] is not existed");
                    this.dataOutputStream.writeUTF("notExisted");
                    break;
                }
                Double balance = storage.getUserBalance(username);
                this.dataOutputStream.writeUTF(balance.toString());
                System.out.println("Success to check the user " + username + "'s information");
            }
            break;
            case "distributeMinicoin": {
                String username = this.dataInputStream.readUTF();
                boolean isExisted = isUserExisted(username);
                if (isExisted) {
                    this.dataOutputStream.writeUTF("existed");
                } else {
                    System.out.println("the user [" + username + "] is not existed");
                    this.dataOutputStream.writeUTF("notExisted");
                    break;
                }
                String amountString = this.dataInputStream.readUTF();
                Double amount = Double.parseDouble(amountString);
                storage.addUserBalance(username, amount);
                System.out.println("Success to distribute Minicoin " + amount.toString());
                this.dataOutputStream.writeUTF("success");
            }
            break;
            case "takeBackMinicoin": {
                String username = this.dataInputStream.readUTF();
                boolean isExisted = isUserExisted(username);
                if (isExisted) {
                    this.dataOutputStream.writeUTF("existed");
                } else {
                    System.out.println("the user [" + username + "] is not existed");
                    this.dataOutputStream.writeUTF("notExisted");
                    break;
                }
                String amountString = this.dataInputStream.readUTF();
                Double amount = Double.parseDouble(amountString);
                Double balanceBefore = storage.getUserBalance(username);
                if (balanceBefore < amount) {
                    System.out.println("Its Minicoin is not enough to take back");
                    this.dataOutputStream.writeUTF("notEnough");
                } else {
                    storage.reduceUserBalance(username, amount);
                    Double balanceNow = storage.getUserBalance(username);
                    Double reduceAmount = balanceBefore - balanceNow;
                    System.out.println("Success to take back Minicoin " + reduceAmount.toString());
                    this.dataOutputStream.writeUTF("success");
                }
            }
            break;
            case "changePassword": {
                String oldPassword = this.dataInputStream.readUTF();
                if (this.checkAdminPassword(oldPassword)) {
                    this.dataOutputStream.writeUTF("match");
                } else {
                    this.dataOutputStream.writeUTF("notMatch");
                    System.out.println("The old password does not match");
                    break;
                }

                String newPassword = this.dataInputStream.readUTF();
                storage.changeAdminPassword(newPassword);
                this.dataOutputStream.writeUTF("success");
                System.out.println("Success to change admin password");
            }
            break;
            case "exit":
            default: {
                return false;
            }
        }
        return true;
    }

    private boolean isUsernameAvailable(String username) { return storage.isUsernameAvailable(username); }

    private boolean isUserExisted(String username) {
        return !isUsernameAvailable(username);
    }

    private boolean checkAdminPassword(String password) {
        return storage.getAdminPassword().equals(password);
    }
}
