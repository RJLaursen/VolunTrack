package model;

import java.sql.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import model.UserDirectAccessObject;

//DatabaseManager for connecting to the SQLite database
//Currently using Singleton Pattern (might be subject for change since this is the first commit)
public class DatabaseManager {
    private static DatabaseManager instance; //Singleton instance
    private Connection connection;
    private final String URL = "jdbc:sqlite:voluntrack.db"; //Database file
    private static boolean setupDone = false;

    //Private constructor to make sure Singleton connects to SQLite database and creates neccesary tables (if needed)
    private DatabaseManager() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC"); //Load driver explicitly
            //Establish connection and creates "voluntrack.db file" if it doesn't exist yet
            connection = DriverManager.getConnection(URL);
            //Ensure required tables exist
            createTablesIfNotExists();
            createDefaultAdminIfMissing();
            loadProjectsFromCSVIfNeeded(connection);

        //Exceptions in case something goes wrong
        } catch (ClassNotFoundException ex) {
            throw new SQLException("SQLite JDBC Driver not found.", ex);
        } catch (SQLException ex) {
            throw new SQLException("Failed to connect to database.", ex);
        }
    }

    //Public method to get the singleton instance
    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null) instance = new DatabaseManager();
        else if (instance.getConnection().isClosed()) instance = new DatabaseManager();
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    //Creates users, projects, and registrations tables if they don't exist yet
    private void createTablesIfNotExists() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            //Users table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    is_admin BOOLEAN DEFAULT 0
                );
            """);
            //Projects table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    location TEXT NOT NULL,
                    day TEXT NOT NULL,
                    hourly_value REAL NOT NULL,
                    total_slots INTEGER NOT NULL,
                    registered_slots INTEGER NOT NULL DEFAULT 0,
                    is_enabled BOOLEAN DEFAULT 1
                );
            """);
            //Registrations table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS registrations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    project_id INTEGER NOT NULL,
                    slots INTEGER NOT NULL,
                    hours INTEGER NOT NULL,
                    contribution REAL NOT NULL,
                    confirmed_at TEXT NOT NULL,
                    FOREIGN KEY(user_id) REFERENCES users(id),
                    FOREIGN KEY(project_id) REFERENCES projects(id)
                );
            """);
        }
    }

    //Creates the admin user if they dont exist yet (integrated the original AdminSetup.java code into this)
    //Preferably this would be done in a more professional manner, but it works
    private void createDefaultAdminIfMissing() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE username = 'admin'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                if (!setupDone) System.out.println("⚙ Creating default admin user...");
                UserDirectAccessObject userDAO = new UserDirectAccessObject(connection);
                boolean adminCreated = userDAO.registerUser(
                        "System Administrator",
                        "admin",
                        "admin@voluntrack.com",
                        "Admin654!@",
                        true
                );
                if (adminCreated && !setupDone)
                    System.out.println("Default admin created (admin)");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Failed while creating default admin.", e);
        }
    }

    //Loads projects from CSV (had to create this cause the program was having issues populating the admin/user dashboard)
    private void loadProjectsFromCSVIfNeeded(Connection conn) {
        try {
            //Try common possible CSV locations (cover all possible bases)
            List<String> possiblePaths = Arrays.asList(
                "projects.csv",
                "./projects.csv",
                "src/projects.csv",
                "resources/projects.csv",
                System.getProperty("user.dir") + "/projects.csv"
            );

            File csvFile = null;
            for (String path : possiblePaths) {
                File candidate = new File(path);
                if (candidate.exists()) {
                    csvFile = candidate;
                    break;
                }
            }

            if (csvFile != null) {
                System.out.println("Found CSV file: " + csvFile.getAbsolutePath());
                util.CSVLoader.loadProjectsIfEmpty(conn, csvFile.getAbsolutePath());

                //Verification log, show how many projects were loaded (to verify nothing went wrong)
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM projects")) {
                    if (rs.next()) {
                        System.out.println("Good news! " + rs.getInt("count") + " project(s) detected in database.");
                    }
                }

            } else {
                System.out.println("Could not find projects.csv in any known location.");
            }

        } catch (Exception e) {
            System.err.println("Failed to auto-load projects from CSV.");
            e.printStackTrace();
        }
    }

    //Switch to alternative database for JUnit testing
    public void switchToTestMode() throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
            connection = DriverManager.getConnection("jdbc:sqlite:test_voluntrack.db");
            createTablesIfNotExists();
            createDefaultAdminIfMissing();
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Failed to switch to test DB.", e);
        }
    }
}
