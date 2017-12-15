package MinicoinServer;

public class User {
    private String username;
    private String password;
    private Double balance = 0.0;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public synchronized String  getPassword() {
        return password;
    }

    public synchronized void setPassword(String password) {
        this.password = password;
    }

    public synchronized Double getBalance() {
        return balance;
    }

    public synchronized void setBalance(Double balance) {
        this.balance = balance;
    }

    public synchronized void reduceBalance(Double amount) {
        this.balance -= amount;
        Math.max(0.0, this.balance);
    }

    public synchronized void addBalance(Double amount) {
        this.balance += amount;
    }
}
