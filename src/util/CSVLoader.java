package util;

import model.DatabaseManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

//Utility class to load projects from a CSV file into the database
//This will only runs if the "projects" table is empty (to avoid duplicates and stuff)

public class CSVLoader {
    public static void loadProjectsIfEmpty(String csvFilePath) {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();

            //Check if projects already exist
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) AS count FROM projects");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Projects already loaded. Skipping CSV import.");
                return;
            }

            //Read CSV and insert projects
            BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
            String line;

            //Skip header row
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String title = parts[0].trim();
                    String location = parts[1].trim();
                    String day = parts[2].trim();
                    double hourlyValue = Double.parseDouble(parts[3].trim());
                    int registeredSlots = Integer.parseInt(parts[4].trim());
                    int totalSlots = Integer.parseInt(parts[5].trim());

                    //Insert into database
                    PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO projects (title, location, day, hourly_value, total_slots, registered_slots, is_enabled) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 1)"
                    );
                    pstmt.setString(1, title);
                    pstmt.setString(2, location);
                    pstmt.setString(3, day);
                    pstmt.setDouble(4, hourlyValue);
                    pstmt.setInt(5, totalSlots);
                    pstmt.setInt(6, registeredSlots);
                    pstmt.executeUpdate();
                }
            }
            reader.close();
            System.out.println("Projects loaded from CSV into database."); //Success!

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
