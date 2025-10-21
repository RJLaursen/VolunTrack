package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

//Utility class to load projects from a CSV file into the database
//Only runs if the "projects" table is empty (avoids duplicates)
public class CSVLoader {

    public static void loadProjectsIfEmpty(Connection conn, String csvFilePath) {
        try {
            //Check if projects table already has entries
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) AS count FROM projects");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Projects already exist. Skipping CSV import.");
                return;
            }

            //Check file existence
            File f = new File(csvFilePath);
            if (!f.exists()) {
                System.out.println("CSV file not found: " + csvFilePath);
                return;
            }

            //Read the CSV
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line;
                boolean skipHeader = true;

                while ((line = reader.readLine()) != null) {
                    if (skipHeader) { skipHeader = false; continue; }

                    String[] parts = line.split(",");
                    boolean isEnabled = true;

                if (parts.length >= 6) {
                    String title = parts[0].trim();
                    String location = parts[1].trim();
                    String day = parts[2].trim();
                    double hourlyValue = Double.parseDouble(parts[3].trim());
                    int registeredSlots = Integer.parseInt(parts[4].trim());
                    int totalSlots = Integer.parseInt(parts[5].trim());

                    if (parts.length == 7) {
                        isEnabled = Boolean.parseBoolean(parts[6].trim());
                    }

                PreparedStatement pstmt = conn.prepareStatement("""
                    INSERT INTO projects (title, location, day, hourly_value, registered_slots, total_slots, is_enabled)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """);
                    pstmt.setString(1, title);
                    pstmt.setString(2, location);
                    pstmt.setString(3, day);
                    pstmt.setDouble(4, hourlyValue);
                    pstmt.setInt(5, registeredSlots);
                    pstmt.setInt(6, totalSlots);
                    pstmt.setBoolean(7, isEnabled);
                    pstmt.executeUpdate();
                    }
                }
            }
        System.out.println("Projects loaded successfully from CSV.");

        } catch (Exception e) {
            System.err.println("Failed to load projects from CSV.");
            e.printStackTrace();
        }
    }
}
