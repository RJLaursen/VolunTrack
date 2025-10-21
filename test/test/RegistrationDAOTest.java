package test;

import model.DatabaseManager;
import model.ProjectDAO;
import model.RegistrationDAO;
import model.UserDirectAccessObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

//JUnit Test (creating users, projects, and registrations, with checks in place)
public class RegistrationDAOTest {

    @BeforeAll
    static void setup() throws Exception {
        DatabaseManager db = DatabaseManager.getInstance();
        db.switchToTestMode();
    }

    private int createTestUserAndGetId() throws Exception {
        Connection conn = DatabaseManager.getInstance().getConnection();
        UserDirectAccessObject userDAO = new UserDirectAccessObject(conn);

        String uname = "regUser_" + System.currentTimeMillis();
        String email = uname + "@example.com";

        boolean created = userDAO.registerUser("Reg Test", uname, email, "Password1!", false);
        assertTrue(created, "Test user should be created");

        PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username = ? ORDER BY id DESC LIMIT 1");
        ps.setString(1, uname);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("id");
        throw new IllegalStateException("Test user ID not found");
    }

    private int createTestProjectAndGetId() throws Exception {
        String uniqueTitle = "RegProj_" + System.currentTimeMillis();
        boolean added = ProjectDAO.addProject(uniqueTitle, "Melbourne", "Tue", 20.0, 5);
        assertTrue(added, "Test project should be created");

        Connection conn = DatabaseManager.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM projects WHERE title = ? ORDER BY id DESC LIMIT 1");
        ps.setString(1, uniqueTitle);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("id");
        throw new IllegalStateException("Test project ID not found");
    }

    @Test
    public void testAddRegistration_Success() throws Exception {
        int userId = createTestUserAndGetId();
        int projectId = createTestProjectAndGetId();

        String result = RegistrationDAO.addRegistration(userId, projectId, 1, 2, 40.0);
        assertTrue(result.startsWith("✅"), "Should confirm successful registration");
    }

    @Test
    public void testAddRegistration_Duplicate() throws Exception {
        int userId = createTestUserAndGetId();
        int projectId = createTestProjectAndGetId();

        RegistrationDAO.addRegistration(userId, projectId, 1, 2, 40.0);

        String result = RegistrationDAO.addRegistration(userId, projectId, 1, 2, 40.0);
        assertTrue(result.contains("⚠"), "Duplicate registration should be prevented");
    }

    @Test
    public void testAddRegistration_InvalidHours() throws Exception {
        int userId = createTestUserAndGetId();
        int projectId = createTestProjectAndGetId();

        String result = RegistrationDAO.addRegistration(userId, projectId, 1, 5, 40.0);
        assertTrue(result.contains("⚠"), "Invalid hours should trigger warning");
    }
}
