package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

//Handles registration-related database actions
public class RegistrationDAO {

    //Adds a new registration record, preventing duplicates
    public static String addRegistration(int userId, int projectId, int slots, int hours, double contribution) {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();

            //Check if user already registered for this project
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM registrations WHERE user_id = ? AND project_id = ?"
            );
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, projectId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return "⚠ You are already registered for this project.";
            }

            //Validate hours and slots (1–3)
            if (slots < 1 || slots > 3 || hours < 1 || hours > 3) {
                return "⚠ Hours and slots must each be between 1 and 3.";
            }

            //Check available slots
            PreparedStatement capacityStmt = conn.prepareStatement(
                "SELECT total_slots, registered_slots FROM projects WHERE id = ?"
            );
            capacityStmt.setInt(1, projectId);
            ResultSet capRS = capacityStmt.executeQuery();

            if (capRS.next()) {
                int total = capRS.getInt("total_slots");
                int registered = capRS.getInt("registered_slots");
                int available = total - registered;

                if (slots > available) {
                    return "⚠ Not enough available slots (" + available + " left).";
                }
            }

            String confirmedAt = java.time.ZonedDateTime.now(java.time.ZoneId.of("Australia/Melbourne"))
            .toLocalDateTime().toString();

            //Insert registration
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO registrations (user_id, project_id, slots, hours, contribution, confirmed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
            );
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, projectId);
            insertStmt.setInt(3, slots);
            insertStmt.setInt(4, hours);
            insertStmt.setDouble(5, contribution);
            insertStmt.setString(6, confirmedAt);
            insertStmt.executeUpdate();

            //Update registered slots count
            PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE projects SET registered_slots = registered_slots + ? WHERE id = ?"
            );
            updateStmt.setInt(1, slots);
            updateStmt.setInt(2, projectId);
            updateStmt.executeUpdate();

            return "✅ Registration successful!";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error registering project.";
        }
    }
}
