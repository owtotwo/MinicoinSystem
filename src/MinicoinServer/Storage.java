package MinicoinServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Storage {
    private static Storage instance = null;

    private static final Path userPath = Paths.get(System.getProperty("user.dir"));
    private static final Path dataFilePath = Paths.get(userPath.toString(), "Minicoin.db");

    private static final String dataFileHeadLabel = "Minicoin Database";
    private static final String dataFileAdminLabel = "Admin:";
    private static final String dataFileUserLabel = "User:";
    private static final String dataFileSeparator = "";

    private Admin admin = new Admin();
    private List<User> userList = new ArrayList<User>();

    private Storage() {
        try {
            load();
            save();
        } catch (StorageException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // Singleton
    public static synchronized Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    protected void finalize() {
        save();
    }

    public void load() throws StorageException {
        // read from data file

        // Form:
        // Minicoin Database
        //
        // Admin:
        // <admin password>
        //
        // User:
        // <user0: username>
        // <user0: password>
        // <user0: balance>
        //
        // <user1: username>
        // <user1: password>
        // <user1: balance>
        //
        // <user2: username>
        // ...
        try (Stream<String> stream = Files.lines(dataFilePath)) {
            String[] lines = stream.toArray(String[]::new);
            Integer userCount = 0;

            assertLine(lines, 0, dataFileHeadLabel);
            assertLine(lines, 1, dataFileSeparator);

            assertLine(lines, 2, dataFileAdminLabel);
            String adminPassword = lines[3];
            assertLine(lines, 4, dataFileSeparator);
            this.admin = new Admin(adminPassword);

            assertLine(lines, 5, dataFileUserLabel);
            for (Integer i = 0; lines.length > 6 + i * 4 + 3; i++) {
                String username = lines[6 + i * 4 + 0];
                String password = lines[6 + i * 4 + 1];
                String balance = lines[6 + i * 4 + 2];
                assertLine(lines, 6 + i * 4 + 3, dataFileSeparator);
                User user = new User(username, password, Double.parseDouble(balance));
                this.userList.add(user);
            }
        } catch (NoSuchFileException e) {
            System.err.println("Loading error: No such a database file: " + dataFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void assertLine(String[] lines, Integer index, String expectedLine) throws StorageException {
        if (!lines[index].equals(expectedLine)) {
            throw new StorageException("Form Error: line[" + Integer.toString(index + 1) + "], expect \"" + expectedLine + "\".");
        }
    }

    public void save() {
        // write to data file
        StringBuffer contentBuffer = new StringBuffer();
        Function<String, StringBuffer> appendLine = line -> contentBuffer.append(line).append(System.lineSeparator());

        appendLine.apply(dataFileHeadLabel);
        appendLine.apply(dataFileSeparator);

        appendLine.apply(dataFileAdminLabel);
        appendLine.apply(admin.getPassword());
        appendLine.apply(dataFileSeparator);

        appendLine.apply(dataFileUserLabel);
        for (Integer i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            appendLine.apply(user.getUsername());
            appendLine.apply(user.getPassword());
            appendLine.apply(user.getBalance().toString());
            appendLine.apply(dataFileSeparator);
        }

        try {
            Files.write(dataFilePath, contentBuffer.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void changeAdminPassword(String password) { admin.setPassword(password); save(); }

    public synchronized void changeUserPassword(String username, String password) { getUser(username).setPassword(password); save(); }

    public synchronized void addUserBalance(String username, Double amount) { getUser(username).addBalance(amount); save(); }

    public synchronized void reduceUserBalance(String username, Double amount) { getUser(username).reduceBalance(amount); save(); }

    public synchronized Double getUserBalance(String username) { return getUser(username).getBalance(); }

    public synchronized String getAdminPassword() { return admin.getPassword(); }

    public synchronized String getUserPassword(String username) { return getUser(username).getPassword(); }

    public synchronized void addUser(String username, String password) { userList.add(new User(username, password)); save(); }

    public synchronized Integer getUserListSize() { return userList.size(); }

    public synchronized boolean isUsernameAvailable(String username) {
        return userList.stream()
                .filter(user -> user.getUsername().equals(username))
                .count() == 0;
    }

    private synchronized User getUser(String username) {
        return userList.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .get();
    }
}
