package MinicoinClientForUser;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static Socket socket;
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;

    public static void main(String[] args) {
        System.out.println("This is a Client for User.");
        try {
            System.out.println("Connecting...");
            Client.socket = new Socket("localhost", 10010);
            Client.dataInputStream = new DataInputStream(Client.socket.getInputStream());
            Client.dataOutputStream = new DataOutputStream(Client.socket.getOutputStream());
            System.out.println("Connect to the server successfully!");
            Client.mailLoop();
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private static void mailLoop() throws IOException {
        while (true) {
            System.out.println("1. login\n2. exit");

            Scanner scanner = new Scanner(System.in);

            Integer choice;
            List<Integer> validChoices = new ArrayList<Integer>(Arrays.asList(1, 2));
            while (true) {
                System.out.println("Please input your choice:");
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                    if (validChoices.contains(choice)) {
                        break;
                    } else {
                        System.err.println("Not a valid choice number");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Not a valid number");
                }
            }

            switch (choice) {
                case 1: {
                    Client.dataOutputStream.writeUTF("login");

                    System.out.println("Please input your username: ");
                    String username = scanner.nextLine();
                    Client.dataOutputStream.writeUTF(username);

                    String result = Client.dataInputStream.readUTF();

                    if (!result.equals("existed")) {
                        System.out.println("This user does not exist.");
                        break;
                    }

                    System.out.println("Please input your password: ");
                    String password = scanner.nextLine();
                    try {
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        byte[] cipher = md5.digest(password.getBytes("UTF-8"));
                        String encryptedPassword = (new HexBinaryAdapter()).marshal(cipher);
                        Client.dataOutputStream.writeUTF(encryptedPassword);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    result = Client.dataInputStream.readUTF();
                    if (result.equals("success")) {
                        System.out.println("Success to Login!");
                        Client.menu();
                    } else if (result.equals("wrongPassword")) {
                        System.out.println("The password is wrong!");
                    } else {
                        System.out.println("Fail to Login!");
                    }
                }
                break;
                case 2: {
                    Client.dataOutputStream.writeUTF("exit");
                    System.out.println("GoodBye~");
                    return;
                }
            }
        }
    }

    private static void menu() throws IOException {
        while (true) {
            System.out.println(
                    "1. check balance\n" +
                    "2. transfer minicoin\n" +
                    "3. change password\n" +
                    "4. logout");

            Scanner scanner = new Scanner(System.in);

            Integer choice;
            List<Integer> validChoices = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5));
            while (true) {
                System.out.println("Please input your choice:");
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                    if (validChoices.contains(choice)) {
                        break;
                    } else {
                        System.err.println("Not a valid choice number");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Not a valid number");
                }
            }

            switch (choice) {
                case 1: {
                    Client.dataOutputStream.writeUTF("checkBalance");
                    String balanceString = Client.dataInputStream.readUTF();
                    Double balance = Double.parseDouble(balanceString);
                    System.out.println("balance: [" + balance.toString() + "]");
                }
                break;
                case 2: {
                    Client.dataOutputStream.writeUTF("transferMinicoin");

                    System.out.println("Please input the remittee username: ");
                    String username = scanner.nextLine();
                    Client.dataOutputStream.writeUTF(username);

                    String usernameInfo = Client.dataInputStream.readUTF();
                    if (!usernameInfo.equals("existed")) {
                        System.out.println("This user does not exist.");
                        continue;
                    }

                    System.out.println("Please input the amount you want to transfer: ");
                    String amountString = scanner.nextLine();
                    Double amount;
                    try {
                        amount = Double.parseDouble(amountString);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number, fail to transfer");
                        break;
                    }
                    amountString = amount.toString();
                    Client.dataOutputStream.writeUTF(amountString);

                    String result = Client.dataInputStream.readUTF();
                    if (result.equals("success")) {
                        System.out.println("Success to transfer Minicoin " + amountString);
                    }
                    else if (result.equals("notEnough")) {
                        System.out.println("Its Minicoin is not enough to transfer");
                    }
                    else {
                        System.out.println("Fail to transfer " + amountString + " Minicoin");
                    }
                }
                break;
                case 3: {
                    Client.dataOutputStream.writeUTF("changePassword");

                    System.out.println("Please input your old admin password: ");
                    String oldPassword = scanner.nextLine();
                    try {
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        byte[] cipher = md5.digest(oldPassword.getBytes("UTF-8"));
                        String encryptedPassword = (new HexBinaryAdapter()).marshal(cipher);
                        Client.dataOutputStream.writeUTF(encryptedPassword);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    String result = Client.dataInputStream.readUTF();
                    if (!result.equals("match")) {
                        System.out.println("The old admin password does not match.");
                        break;
                    }

                    String newPassword;
                    while (true) {
                        System.out.println("Please input your new admin password: ");
                        newPassword = scanner.nextLine();

                        System.out.println("Please input your new admin password again: ");
                        String newPasswordAgain = scanner.nextLine();

                        if (newPassword.equals(newPasswordAgain)) {
                            break;
                        }
                        System.out.println("It is different between the passwords inputted two times.");
                    }

                    try {
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        byte[] cipher = md5.digest(newPassword.getBytes("UTF-8"));
                        String encryptedPassword = (new HexBinaryAdapter()).marshal(cipher);
                        Client.dataOutputStream.writeUTF(encryptedPassword);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    result = Client.dataInputStream.readUTF();
                    if (result.equals("success")) {
                        System.out.println("Success to change the admin password");
                    } else {
                        System.out.println("Fail to change the admin password");
                    }
                }
                break;
                case 4: {
                    System.out.println("Success to Logout");
                    return;
                }
            }
        }
    }
}
