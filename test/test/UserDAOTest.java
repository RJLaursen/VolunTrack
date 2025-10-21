package test;

import model.DatabaseManager;
import model.UserDirectAccessObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

//JUnit Test (user creation and login system)
public class UserDAOTest {

    private static UserDirectAccessObject userDAO;

    @BeforeAll
    static void setup() throws Exception {
        DatabaseManager db = DatabaseManager.getInstance();
        db.switchToTestMode();

        Connection conn = db.getConnection();
        userDAO = new UserDirectAccessObject(conn);
    }

    @Test
    public void testRegisterUser_Success() {
        String uniqueUser = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUser + "@test.com";
        boolean result = userDAO.registerUser("Test User", uniqueUser, uniqueEmail, "Password@1", false);
        assertTrue(result, "User should be created successfully");
    }

    @Test
    void testRegisterUser_Duplicate() {
        userDAO.registerUser("Dup User", "dupuser", "dup@example.com", "Password123!", false);
        boolean secondTry = userDAO.registerUser("Dup User", "dupuser", "dup@example.com", "Password123!", false);
        assertFalse(secondTry, "Duplicate username/email should not be allowed");
    }

    @Test
    void testLogin_Success() {
        String user = "loginuser_" + System.currentTimeMillis();
        userDAO.registerUser("Login User", user, user + "@example.com", "Password123!", false);
        assertTrue(userDAO.loginUser(user, "Password123!"), "User should log in successfully");
    }

    @Test
    void testLogin_Fail() {
        assertFalse(userDAO.loginUser("nonexistent", "wrongpass"), "Invalid login should fail");
    }
}
