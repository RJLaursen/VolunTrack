package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

//Handles registration-related database actions
public class RegistrationDAO {

    //Adds a new registration record, preventing duplicates
    public static boolean addRegistration(int userId, int projectId, int slots, int hours, double contribution) {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();

            // Check if user already registered for this project
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM registrations WHERE user_id = ? AND project_id = ?"
            );
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, projectId);

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                //User already registered, stop here
                return false;
            }

            //Insert new registration
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO registrations (user_id, project_id, slots, hours, contribution, confirmed_at) " +
                "VALUES (?, ?, ?, ?, ?, datetime('now'))"
            );
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, projectId);
            insertStmt.setInt(3, slots);
            insertStmt.setInt(4, hours);
            insertStmt.setDouble(5, contribution);
            insertStmt.executeUpdate();

            //Update the registered slots in the projects table
            PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE projects SET registered_slots = registered_slots + 1 WHERE id = ?"
            );
            updateStmt.setInt(1, projectId);
            updateStmt.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
