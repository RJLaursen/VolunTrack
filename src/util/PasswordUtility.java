package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//Utility class for password hashing and validation
//Utilizes SHA-256 hashing algorithm for secure storage (opted to use SHA-256 because I used this before on other projects)
public class PasswordUtility {
    
    //Hash a plain text password using SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b)); //Convert to hex
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: SHA-256 not available.", e);
        }
    }
}
