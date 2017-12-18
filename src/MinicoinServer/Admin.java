package MinicoinServer;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Admin {
    private String password;

    public Admin() {
        this.password = "admin";
        this.encryptPassword();
    }

    public Admin(String password) { this.password = password; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private void encryptPassword() {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] cipher = md5.digest(this.password.getBytes("UTF-8"));
            this.password = (new HexBinaryAdapter()).marshal(cipher);;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
