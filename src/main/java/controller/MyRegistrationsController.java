package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.DatabaseManager;
import model.Registration;
import util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;

//Controller for the "My Registrations" screen
//Displays all projects the logged in user has registered for with full details
public class MyRegistrationsController {

    //FXML elements
    @FXML private TableView<Registration> regTable;
    @FXML private TableColumn<Registration, String> idCol;
    @FXML private TableColumn<Registration, String> projectCol;
    @FXML private TableColumn<Registration, String> locationCol;
    @FXML private TableColumn<Registration, String> dayCol;
    @FXML private TableColumn<Registration, String> slotsCol;
    @FXML private TableColumn<Registration, String> hoursCol;
    @FXML private TableColumn<Registration, String> contributionCol;
    @FXML private TableColumn<Registration, String> dateCol;

    private ObservableList<Registration> regList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Bind table columns
        idCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.format("%04d", data.getValue().getId())));

        projectCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getProjectTitle()));

        locationCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getProjectLocation()));

        dayCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getProjectDay()));

        slotsCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getSlots())));

        hoursCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getHours())));

        contributionCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty("$" + String.format("%.2f", data.getValue().getContribution())));

        dateCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue()
                    .getConfirmedAt()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))));

        loadRegistrations();
    }

    //Load all registrations for the logged in user, sorted by most recent first
    private void loadRegistrations() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                SELECT r.id, r.user_id, r.project_id, r.slots, r.hours, r.contribution, r.confirmed_at,
                       p.title, p.location, p.day
                FROM registrations r
                JOIN projects p ON r.project_id = p.id
                WHERE r.user_id = ?
                ORDER BY r.confirmed_at DESC
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Session.getCurrentUser().getId());
            ResultSet rs = stmt.executeQuery();

            regList.clear();
            while (rs.next()) {
                String raw = rs.getString("confirmed_at");
                LocalDateTime confirmedLocal = LocalDateTime.parse(raw.replace(" ", "T"));

                Registration r = new Registration(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("project_id"),
                    rs.getInt("slots"),
                    rs.getInt("hours"),
                    rs.getDouble("contribution"),
                    confirmedLocal
                );

                //Set project details inside the Registration model
                r.setProjectTitle(rs.getString("title"));
                r.setProjectLocation(rs.getString("location"));
                r.setProjectDay(rs.getString("day"));

                regList.add(r);
            }

            regTable.setItems(regList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Cancel an existing registration
    @FXML
    private void cancelRegistration() {
        Registration selected = regTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageAlert("Please select a registration to cancel.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Cancellation");
        confirm.setHeaderText("Cancel this registration?");
        confirm.setContentText("Project: " + selected.getProjectTitle() +
                               "\nContribution: $" + selected.getContribution());
        if (confirm.showAndWait().get() != ButtonType.OK) return;

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            //Delete registration record
            PreparedStatement del = conn.prepareStatement("DELETE FROM registrations WHERE id = ?");
            del.setInt(1, selected.getId());
            int rows = del.executeUpdate();

            if (rows > 0) {
                //Reduce registered_slots in projects table
                PreparedStatement update = conn.prepareStatement(
                    "UPDATE projects SET registered_slots = registered_slots - ? WHERE id = ?");
                update.setInt(1, selected.getSlots());
                update.setInt(2, selected.getProjectId());
                update.executeUpdate();

                messageAlert("Registration canceled successfully.");
                loadRegistrations();
            } else {
                messageAlert("Failed to cancel registration.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageAlert("Database error while canceling.");
        }
    }

    //Export registration history to CSV
    @FXML
    private void exportToCSV() {
        if (regList.isEmpty()) {
            messageAlert("No registrations to export.");
            return;
        }

        String username = Session.getCurrentUser().getUsername();
        String fileName = "registrations_" + username + ".csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.append("RegID,Project,Location,Day,Slots,Hours,Contribution,ConfirmedAt\n");

            for (Registration r : regList) {
                writer.append(String.format("%04d", r.getId())).append(",")
                      .append(r.getProjectTitle()).append(",")
                      .append(r.getProjectLocation()).append(",")
                      .append(r.getProjectDay()).append(",")
                      .append(String.valueOf(r.getSlots())).append(",")
                      .append(String.valueOf(r.getHours())).append(",")
                      .append(String.valueOf(r.getContribution())).append(",")
                      .append(r.getConfirmedAt().toString())
                      .append("\n");
            }

            messageAlert("Exported to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            messageAlert("Failed to export file.");
        }
    }

    //Return to dashboard
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

    //Display message popup
    private void messageAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
