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
            System.out.println("1. create user\n2. check user\n3. distribute minicoin\n4. take back minicoin\n5. logout");
            Scanner scanner = new Scanner(System.in);
            Integer choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1: {
                    Client.dataOutputStream.writeUTF("createUser");

                    System.out.println("Please input new username: ");
                    String username = scanner.nextLine();
                    Client.dataOutputStream.writeUTF(username);

                    String usernameInfo = Client.dataInputStream.readUTF();
                    if (!username.equals("available")) {
                        System.out.println("This username has been used.");
                        continue;
                    }

                    System.out.println("Please input new password: ");
                    String password = scanner.nextLine();

                    System.out.println("Please input new password again: ");
                    String passwordAgain = scanner.nextLine();

                    if (!password.equals(passwordAgain)) {
                        System.out.println("It is different between the passwords inputted two times.");
                        continue;
                    }

                    try {
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        byte[] cipher = md5.digest(password.getBytes("UTF-8"));
                        Client.dataOutputStream.writeUTF(cipher.toString());
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
                    if (username.equals("available")) {
                        System.out.println("This username does not exist.");
                        continue;
                    }

                    String balance = Client.dataInputStream.readUTF();
                    System.out.println("username: [" + username + "]");
                    System.out.println("balance: [" + balance + "]");
                }
                break;
                case 3: {
                    Client.dataOutputStream.writeUTF("exit");
                    System.out.println("GoodBye~");
                    return;
                }
            }
        }
    }
}
