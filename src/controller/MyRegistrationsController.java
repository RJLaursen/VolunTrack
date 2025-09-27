package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.DatabaseManager;
import model.Registration;
import util.Session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.FileWriter;
import java.io.IOException;

//Controller for the "My Registrations" screen
//Displays all projects the logged in user has registered for

public class MyRegistrationsController {
    //Table and columns for showing user registrations
    @FXML private TableView<Registration> regTable;
    @FXML private TableColumn<Registration, String> projectCol;
    @FXML private TableColumn<Registration, String> dayCol;
    @FXML private TableColumn<Registration, String> slotsCol;
    @FXML private TableColumn<Registration, String> hoursCol;
    @FXML private TableColumn<Registration, String> contributionCol;
    @FXML private TableColumn<Registration, String> dateCol;

    private ObservableList<Registration> regList = FXCollections.observableArrayList();

    //Runs when the FXML is first loaded
    //Sets up table columns and loads data from database

    @FXML
    public void initialize() {
        //Bind columns to Registration fields (some stuff are placeholders for testing purposes, fix soon)
        projectCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Project #" + data.getValue().getProjectId()));
        dayCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Day TBD"));
        slotsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getSlots())));
        hoursCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getHours())));
        contributionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("$" + data.getValue().getContribution()));
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getConfirmedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        loadRegistrations();
    }

    //Load all registrations for the currently logged in user
    //Queries the database and populates the Table View

    private void loadRegistrations() {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            Statement stmt = conn.createStatement();

            //Fetch registrations only for the logged in user
            ResultSet rs = stmt.executeQuery("SELECT * FROM registrations WHERE user_id=" + Session.getCurrentUser().getId());

            regList.clear();
            while (rs.next()) {
                Registration r = new Registration(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("project_id"),
                    rs.getInt("slots"),
                    rs.getInt("hours"),
                    rs.getDouble("contribution"),
                    LocalDateTime.parse(rs.getString("confirmed_at").replace(" ", "T"))
                );
                regList.add(r);
            }
            regTable.setItems(regList);

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Return to the Dashboard screen
    @FXML
    private void goBack() {
        switchScene("/view/Dashboard.fxml", "VolunTrack - Dashboard");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) regTable.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToCSV() {
        if (regList.isEmpty()) {
            messageAlert("No registrations to export.");
            return;
        }

    String username = Session.getCurrentUser().getUsername();
    String fileName = "registrations_" + username + ".csv";

    try (FileWriter writer = new FileWriter(fileName)) {
        //Write header row
        writer.append("ProjectID,Slots,Hours,Contribution,ConfirmedAt\n");

        //Write the registrations
        for (Registration r : regList) {
            writer.append(r.getProjectId() + ",")
                  .append(r.getSlots() + ",")
                  .append(r.getHours() + ",")
                  .append(r.getContribution() + ",")
                  .append(r.getConfirmedAt().toString())
                  .append("\n");
        }

            writer.flush();
            messageAlert("Exported to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            messageAlert("Failed to export file.");
        }
    }

    //Simple popup alert for messages.
    private void messageAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
