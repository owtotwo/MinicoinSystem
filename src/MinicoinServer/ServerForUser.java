package MinicoinServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ServerForUser extends Thread {

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private Storage storage;

    private String username;

    private static final Set<String> INSTRUCTIONS = new HashSet<String>(Arrays.asList(
            "login", "checkBalance", "transferMinicoin", "changePassword", "exit"
    ));

    public ServerForUser(Socket socket, Storage storage) {
        this.socket = socket;
        this.storage = storage;
        try {
            this.dataInputStream = new DataInputStream(this.socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("A new Sub-Server for a User is running in thread " + Thread.currentThread());
        try {
            this.mainLoop();
            System.out.println("Over.");
        }
        catch (IOException e) {
            e.printStackTrace();
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
                String username = this.dataInputStream.readUTF();
                if (!this.isUserExisted(username)) {
                    System.out.println("This user does not exist.");
                    this.dataOutputStream.writeUTF("notExisted");
                    break;
                } else {
                    this.dataOutputStream.writeUTF("existed");
                    this.username = username;
                }

                String password = this.dataInputStream.readUTF();
                if (this.checkPassword(username, password)) {
                    this.dataOutputStream.writeUTF("success");
                } else {
                    this.dataOutputStream.writeUTF("wrongPassword");
                    break;
                }
            }
            break;
            case "checkBalance": {
                dataOutputStream.writeUTF(storage.getUserBalance(this.username).toString());
            }
            break;
            case "transferMinicoin": {
                String distUsername = this.dataInputStream.readUTF();
                boolean isExisted = isUserExisted(distUsername);
                if (isExisted) {
                    this.dataOutputStream.writeUTF("existed");
                } else {
                    System.out.println("the user [" + distUsername + "] is not existed");
                    this.dataOutputStream.writeUTF("notExisted");
                    break;
                }
                String amountString = this.dataInputStream.readUTF();
                Double amount = Double.parseDouble(amountString);
                Double balanceBefore = storage.getUserBalance(this.username);
                if (balanceBefore < amount) {
                    System.out.println("Its Minicoin is not enough to transfer");
                    this.dataOutputStream.writeUTF("notEnough");
                } else {
                    storage.reduceUserBalance(this.username, amount);
                    storage.addUserBalance(distUsername, amount);
                    Double balanceNow = storage.getUserBalance(this.username);
                    Double reduceAmount = balanceBefore - balanceNow;
                    System.out.println("Success to transfer Minicoin " + reduceAmount.toString() +
                            " from " + this.username + " to " + distUsername);
                    this.dataOutputStream.writeUTF("success");
                }
            }
            break;
            case "changePassword": {
                String oldPassword = this.dataInputStream.readUTF();
                if (this.checkPassword(this.username, oldPassword)) {
                    this.dataOutputStream.writeUTF("match");
                } else {
                    this.dataOutputStream.writeUTF("notMatch");
                    System.out.println("The old password does not match");
                    break;
                }

                String newPassword = this.dataInputStream.readUTF();
                storage.changeUserPassword(this.username, newPassword);
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

    public boolean checkPassword(String username, String password) { return storage.getUserPassword(username).equals(password); }

    private boolean isUserExisted(String username) {
        return !storage.isUsernameAvailable(username);
    }
}
