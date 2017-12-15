package MinicoinServer;

import java.util.List;

public class Server {

    private static List<User> userList;

    public Server() {

    }

    public static void main(String[] args) {
        System.out.println("This is a Server.");

        ServerForAdmin serverForAdmin = new ServerForAdmin(userList);
        serverForAdmin.start();

    }
}
