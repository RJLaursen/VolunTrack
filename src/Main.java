import model.DatabaseManager;
import util.CSVLoader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

//Main entry point for testing the database setup, CSV loading, and other stuff
//Will be replaced by the JavaFX GUI later down the line (for now this will do)

public class Main {
    public static void main(String[] args) {
        try {
            //// Connect to DB
            DatabaseManager db = DatabaseManager.getInstance();

            //Load projects from CSV
            CSVLoader.loadProjectsIfEmpty("projects.csv");

            //Print all projects to confirm DB insert worked
            Connection conn = db.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM projects");

            System.out.println("---- Projects in Database ----");
            while (rs.next()) {
                System.out.println(
                    rs.getInt("id") + " | " +
                    rs.getString("title") + " | " +
                    rs.getString("location") + " | " +
                    rs.getString("day") + " | " +
                    "Slots: " + rs.getInt("registered_slots") +
                    "/" + rs.getInt("total_slots")
                );
            }
            System.out.println("-------------------------------");

            rs.close();
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
