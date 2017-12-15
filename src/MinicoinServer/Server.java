package MinicoinServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {

    private static List<User> userList = new ArrayList<User>();

    public Server() {

    }

    public static void main(String[] args) {
        System.out.println("This is a Server.");

        User u1 = new User("abc", "123");
        Server.userList = new ArrayList<User>(Arrays.asList(u1));
        ServerForAdmin serverForAdmin = new ServerForAdmin(userList);
        serverForAdmin.start();

    }
}
