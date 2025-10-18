package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//Handles all database operations for the projects table
//Includes CRUD actions, duplication checks, and project filtering
public class ProjectDAO {

    //Returns all projects from database ordered by title and day
    public static List<Project> getAllProjects() throws SQLException {
        List<Project> list = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().getConnection();
        String sql = "SELECT * FROM projects ORDER BY title, day";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Project p = new Project(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("location"),
                        rs.getString("day"),
                        rs.getDouble("hourly_value"),
                        rs.getInt("total_slots"),
                        rs.getInt("registered_slots"),
                        rs.getBoolean("is_enabled")
                );
                list.add(p);
            }
        }
        return list;
    }

    //Returns distinct project titles for grouping in admin view
    public static List<String> getDistinctTitles() throws SQLException {
        List<String> titles = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().getConnection();
        String sql = "SELECT DISTINCT title FROM projects ORDER BY title";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                titles.add(rs.getString("title"));
            }
        }
        return titles;
    }

    //Returns all projects that share the same title
    public static List<Project> getProjectsByTitle(String title) throws SQLException {
        List<Project> list = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().getConnection();
        String sql = "SELECT * FROM projects WHERE title = ? ORDER BY day";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Project p = new Project(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("location"),
                            rs.getString("day"),
                            rs.getDouble("hourly_value"),
                            rs.getInt("total_slots"),
                            rs.getInt("registered_slots"),
                            rs.getBoolean("is_enabled")
                    );
                    list.add(p);
                }
            }
        }
        return list;
    }

    //Adds a new project if no duplicate exists (same title, location, and day)
    public static boolean addProject(String title, String location, String day, double hourlyValue, int totalSlots) throws SQLException {
        if (isDuplicate(title, location, day)) return false;
        String sql = "INSERT INTO projects (title, location, day, hourly_value, total_slots, registered_slots, is_enabled) VALUES (?, ?, ?, ?, ?, 0, 1)";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, location);
            ps.setString(3, day);
            ps.setDouble(4, hourlyValue);
            ps.setInt(5, totalSlots);
            ps.executeUpdate();
            return true;
        }
    }

    //Updates an existing project with new data while checking for duplicates
    public static boolean updateProject(int id, String title, String location, String day, double hourlyValue, int totalSlots) throws SQLException {
        if (isDuplicateExcludingId(id, title, location, day)) return false;
        String sql = "UPDATE projects SET title = ?, location = ?, day = ?, hourly_value = ?, total_slots = ? WHERE id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, location);
            ps.setString(3, day);
            ps.setDouble(4, hourlyValue);
            ps.setInt(5, totalSlots);
            ps.setInt(6, id);
            ps.executeUpdate();
            return true;
        }
    }

    //Deletes a project permanently from database by ID
    public static boolean deleteProject(int id) throws SQLException {
        String sql = "DELETE FROM projects WHERE id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        }
    }

    //Toggles project enabled/disabled state
    public static boolean setEnabled(int id, boolean enabled) throws SQLException {
        String sql = "UPDATE projects SET is_enabled = ? WHERE id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enabled ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
            return true;
        }
    }

    //Checks if a project already exists with same title, location, and day
    public static boolean isDuplicate(String title, String location, String day) throws SQLException {
        String sql = "SELECT COUNT(*) FROM projects WHERE title = ? AND location = ? AND day = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, location);
            ps.setString(3, day);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    //Checks for duplicate projects excluding a specific project ID (used for edit operations)
    public static boolean isDuplicateExcludingId(int id, String title, String location, String day) throws SQLException {
        String sql = "SELECT COUNT(*) FROM projects WHERE title = ? AND location = ? AND day = ? AND id <> ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, location);
            ps.setString(3, day);
            ps.setInt(4, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}

