package model;

import util.PasswordUtility;
import java.sql.*;

//Data Access Object for managing user accounts in the database
public class UserDirectAccessObject {
    private Connection conn;

    public UserDirectAccessObject(Connection conn) {
        this.conn = conn;
    }

    //Registers a new user with hashed password
    public boolean registerUser(String fullName, String username, String email, String plainPassword, boolean isAdmin) {
        try {
            //Validates the username/email
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username=? OR email=?");
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; //User already exists
            }

            //Hash password
            String hashed = PasswordUtility.hashPassword(plainPassword);

            //Insert user
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (full_name, username, email, password_hash, is_admin) VALUES (?, ?, ?, ?, ?)");
            stmt.setString(1, fullName);
            stmt.setString(2, username);
            stmt.setString(3, email);
            stmt.setString(4, hashed);
            stmt.setBoolean(5, isAdmin);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Attempts login with username and plain password
    public boolean loginUser(String username, String plainPassword) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT password_hash FROM users WHERE username=?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String inputHash = PasswordUtility.hashPassword(plainPassword);
                return storedHash.equals(inputHash);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User getUserByUsername(String username) {
    try {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username=?");
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new User(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getBoolean("is_admin")
            );
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

}
