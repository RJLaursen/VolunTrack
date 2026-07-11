package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Controller for the Admin Registrations view
//Displays all user registrations including project details
public class AdminRegistrationsController {

    //FXML elements
    @FXML private TableView<AdminRegRow> regsTable;
    @FXML private TableColumn<AdminRegRow, String> ridCol;
    @FXML private TableColumn<AdminRegRow, String> userCol;
    @FXML private TableColumn<AdminRegRow, String> projectCol;
    @FXML private TableColumn<AdminRegRow, String> locationCol;
    @FXML private TableColumn<AdminRegRow, String> dayCol;
    @FXML private TableColumn<AdminRegRow, String> slotsCol;
    @FXML private TableColumn<AdminRegRow, String> hoursCol;
    @FXML private TableColumn<AdminRegRow, String> contribCol;
    @FXML private TableColumn<AdminRegRow, String> timeCol;

    private ObservableList<AdminRegRow> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Bind table columns to properties from AdminRegRow
        ridCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().regId));
        userCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().username));
        projectCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().projectTitle));
        locationCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().location));
        dayCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().day));
        slotsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().slots)));
        hoursCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().hours)));
        contribCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", d.getValue().contribution)));
        
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    timeCol.setCellValueFactory(d -> {
        try {
            //Treat database timestamp as local time
            LocalDateTime localDateTime = LocalDateTime.parse(d.getValue().confirmedAt.replace(" ", "T"));
            String formatted = localDateTime.format(fmt);
            return new javafx.beans.property.SimpleStringProperty(formatted);
        } catch (Exception e) {
            return new javafx.beans.property.SimpleStringProperty(d.getValue().confirmedAt);
        }
    });
        //Load all registrations when the screen opens
        loadAllRegistrations();
    }

    //Loads all user registrations from database and displays them in table
    private void loadAllRegistrations() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT r.id AS rid, u.username, p.title, p.location, p.day, r.slots, r.hours, r.contribution, r.confirmed_at " +
                    "FROM registrations r " +
                    "JOIN users u ON r.user_id = u.id " +
                    "JOIN projects p ON r.project_id = p.id " +
                    "ORDER BY r.confirmed_at DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            //Clear current list and repopulate from query results
            rows.clear();
            while (rs.next()) {
                String regId = String.format("%04d", rs.getInt("rid"));
                String username = rs.getString("username");
                String title = rs.getString("title");
                String loc = rs.getString("location");
                String day = rs.getString("day");
                int slots = rs.getInt("slots");
                int hours = rs.getInt("hours");
                double contrib = rs.getDouble("contribution");
                String confirmed = rs.getString("confirmed_at");

                //Add new record to observable list
                rows.add(new AdminRegRow(regId, username, title, loc, day, slots, hours, contrib, confirmed));
            }
            regsTable.setItems(rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Inner class to represent each row in the registration table
    private static class AdminRegRow {
        String regId, username, projectTitle, location, day, confirmedAt;
        int slots, hours;
        double contribution;

        //Constructor to populate all fields for a row
        AdminRegRow(String regId, String username, String projectTitle, String location, String day, int slots, int hours, double contribution, String confirmedAt) {
            this.regId = regId;
            this.username = username;
            this.projectTitle = projectTitle;
            this.location = location;
            this.day = day;
            this.slots = slots;
            this.hours = hours;
            this.contribution = contribution;
            this.confirmedAt = confirmedAt;
        }
    }
}
