package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

//DatabaseManager for connecting to the SQLite database
//Currently using Singleton Pattern (might be subject for change since this is the first commit)

public class DatabaseManager {
    private static DatabaseManager instance; //Singleton instance
    private Connection connection;
    private final String URL = "jdbc:sqlite:voluntrack.db"; //Database file

    //Private constructor to make sure Singleton connects to SQLite database and creates neccesary tables (if needed)

    private DatabaseManager() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC"); //Load driver explicitly

            //Establish connection and creates "voluntrack.db file" if it doesn't exist yet
            connection = DriverManager.getConnection(URL);

            //Ensure required tables exist
            createTablesIfNotExists();

        //Exceptions in case something goes wrong
        } catch (ClassNotFoundException ex) {
            throw new SQLException("SQLite JDBC Driver not found.", ex);
        } catch (SQLException ex) {
            throw new SQLException("Failed to connect to database.", ex);
        }
    }

    //Public method to get the singleton instance
    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager();
        } else if (instance.getConnection().isClosed()) {
            instance = new DatabaseManager();
        }
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
}
