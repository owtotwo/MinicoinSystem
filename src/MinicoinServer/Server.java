package MinicoinServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {

    private static List<User> userList = new ArrayList<User>();
    private static Storage storage;

    public Server() {

    }

    public static void main(String[] args) {
        System.out.println("This is a Server.");

        storage = new Storage();
        ServerForAdmin serverForAdmin = ServerForAdmin.getInstance(storage);
        serverForAdmin.start();

    }
}
