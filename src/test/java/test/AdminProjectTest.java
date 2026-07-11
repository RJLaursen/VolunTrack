package test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import model.DatabaseManager;
import model.ProjectDAO;

import static org.junit.jupiter.api.Assertions.*;

//JUnit Test (adding, duplication check, and deleting projects)
public class AdminProjectTest {

    @BeforeAll
    static void setup() throws Exception {
        DatabaseManager db = DatabaseManager.getInstance();
        db.switchToTestMode();
    }

    @Test
    public void testAddProject_Success() throws Exception {
        String uniqueTitle = "JUnit Project " + System.currentTimeMillis();
        boolean result = ProjectDAO.addProject(uniqueTitle, "TestLocation", "Mon", 10.0, 5);
        assertTrue(result, "Project should be added successfully");
    }

    @Test
    void testAddProject_Duplicate() throws Exception {
        ProjectDAO.addProject("DuplicateProject", "Sydney", "Tue", 30.0, 5);
        boolean duplicate = ProjectDAO.addProject("DuplicateProject", "Sydney", "Tue", 30.0, 5);
        assertFalse(duplicate, "Duplicate project should not be added");
    }

    @Test
    void testDeleteProject() throws Exception {
        ProjectDAO.addProject("TempProject", "Brisbane", "Wed", 20.0, 4);
        boolean deleted = ProjectDAO.deleteProject(1);
        assertTrue(deleted, "Project should be deleted successfully");
    }
}
