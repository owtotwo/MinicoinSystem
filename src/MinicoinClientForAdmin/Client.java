package MinicoinClientForAdmin;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Client {
    private static Socket socket;
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;

    public static void main(String[] args) {
        System.out.println("This is a Client for Admin.");
        try {
            System.out.println("Connecting...");
            Client.socket = new Socket("localhost", 10086);
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
            Integer choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1: {
                    Client.dataOutputStream.writeUTF("login");

                    System.out.println("Please input your admin password: ");
                    String password = scanner.nextLine();
                    try {
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        byte[] cipher = md5.digest(password.getBytes("UTF-8"));
                        String encryptedPassword = (new HexBinaryAdapter()).marshal(cipher);
                        Client.dataOutputStream.writeUTF(encryptedPassword);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    String result = Client.dataInputStream.readUTF();
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
            System.out.println("1. create user\n" +
                    "2. check user\n" +
                    "3. distribute minicoin\n" +
                    "4. take back minicoin\n" +
                    "5. change admin password\n" +
                    "6. logout");
            Scanner scanner = new Scanner(System.in);
            Integer choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1: {
                    Client.dataOutputStream.writeUTF("createUser");

                    System.out.println("Please input new username: ");
                    String username = scanner.nextLine();
                    Client.dataOutputStream.writeUTF(username);

                    String usernameInfo = Client.dataInputStream.readUTF();
                    if (!usernameInfo.equals("available")) {
                        System.out.println("This username has been used.");
                        continue;
                    }

                    String password;
                    while (true) {
                        System.out.println("Please input new password: ");
                        password = scanner.nextLine();

                        System.out.println("Please input new password again: ");
                        String passwordAgain = scanner.nextLine();

                        if (password.equals(passwordAgain)) {
                            break;
                        }
                        System.out.println("It is different between the passwords inputted two times.");
                    }

                    try {
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        byte[] cipher = md5.digest(password.getBytes("UTF-8"));
                        String encryptedPassword = (new HexBinaryAdapter()).marshal(cipher);
                        Client.dataOutputStream.writeUTF(encryptedPassword);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    String result = Client.dataInputStream.readUTF();
                    if (result.equals("success")) {
                        System.out.println("Success to Create a new User!");
                    } else {
                        System.out.println("Fail to Create a new User!");
                    }
                }
                break;
                case 2: {
                    Client.dataOutputStream.writeUTF("checkUser");

                    System.out.println("Please input the username: ");
                    String username = scanner.nextLine();
                    Client.dataOutputStream.writeUTF(username);

                    String usernameInfo = Client.dataInputStream.readUTF();
                    if (!usernameInfo.equals("existed")) {
                        System.out.println("This user does not exist.");
                        continue;
                    }

                    String balance = Client.dataInputStream.readUTF();
                    System.out.println("username: [" + username + "]");
                    System.out.println("balance: [" + balance + "]");
                }
                break;
                case 3: {
                    Client.dataOutputStream.writeUTF("distributeMinicoin");

                    System.out.println("Please input the username: ");
                    String username = scanner.nextLine();
                    Client.dataOutputStream.writeUTF(username);

                    String usernameInfo = Client.dataInputStream.readUTF();
                    if (!usernameInfo.equals("existed")) {
                        System.out.println("This user does not exist.");
                        continue;
                    }

                    System.out.println("Please input the amount you want to distribute: ");
                    String amountString = scanner.nextLine();
                    Client.dataOutputStream.writeUTF(amountString);

                    String result = Client.dataInputStream.readUTF();
                    if (result.equals("success")) {
                        System.out.println("Success to distribute Minicoin " + amountString);
                    }
                    else {
                        System.out.println("Fail to distribute Minicoin " + amountString);
                    }
                }
                break;
                case 4: {
                    Client.dataOutputStream.writeUTF("takeBackMinicoin");

                    System.out.println("Please input the username: ");
                    String username = scanner.nextLine();
                    Client.dataOutputStream.writeUTF(username);

                    String usernameInfo = Client.dataInputStream.readUTF();
                    if (!usernameInfo.equals("existed")) {
                        System.out.println("This user does not exist.");
                        continue;
                    }

                    System.out.println("Please input the amount you want to take back: ");
                    String amountString = scanner.nextLine();
                    Client.dataOutputStream.writeUTF(amountString);

                    String result = Client.dataInputStream.readUTF();
                    if (result.equals("success")) {
                        System.out.println("Success to take back Minicoin " + amountString);
                    }
                    else if (result.equals("notEnough")) {
                        System.out.println("Its Minicoin is not enough to take back");
                    }
                    else {
                        System.out.println("Fail to take back " + amountString + " Minicoin");
                    }
                }
                break;
                case 5: {
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
                case 6: {
                    System.out.println("Success to Logout");
                    return;
                }
            }
        }
    }
}
