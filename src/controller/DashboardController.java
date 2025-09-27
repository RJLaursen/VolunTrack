package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import model.DatabaseManager;
import model.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

//Controller for the Dashboard screen
//Displays projects in a Table View

public class DashboardController {
    @FXML private TableView<Project> projectTable;
    @FXML private TableColumn<Project, String> titleCol;
    @FXML private TableColumn<Project, String> locationCol;
    @FXML private TableColumn<Project, String> dayCol;
    @FXML private TableColumn<Project, String> slotsCol;
    @FXML private TableColumn<Project, String> valueCol;

    @FXML private Label messageLabel;
    @FXML private Label welcomeLabel;


    private ObservableList<Project> projectList = FXCollections.observableArrayList();

    //Populate the Projects table with data from the database
    //Runs when Dashboard is loaded
    @FXML
    public void initialize() {
        if (util.Session.getCurrentUser() != null) {
        welcomeLabel.setText("Welcome, " + util.Session.getCurrentUser().getUsername() + "!");
        }
        //Bind each column in the table to fields in the Project model

        titleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        locationCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLocation()));
        dayCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDay()));
        slotsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRegisteredSlots() + "/" + data.getValue().getTotalSlots()
        ));
        valueCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                "$" + data.getValue().getHourlyValue()
        ));

        //Load projects from the database
        loadProjectsFromDB();
    }

    private void loadProjectsFromDB() {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM projects WHERE is_enabled=1");

            projectList.clear();
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
                projectList.add(p);
            }
            projectTable.setItems(projectList);

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        switchScene("/view/LoginView.fxml", "VolunTrack - Login");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) projectTable.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Register the current user for the selected project
    //Inserts a new record into the "registrations" table
    @FXML
    private void handleRegister() {
        Project selected = projectTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
            messageLabel.setText("Please select a project.");
            return;
        }

            if (selected.getRegisteredSlots() >= selected.getTotalSlots()) {
            messageLabel.setText("This project is full!");
            return;
        }

        try {
        Connection conn = DatabaseManager.getInstance().getConnection();

            //Insert into registrations table
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO registrations (user_id, project_id, slots, hours, contribution, confirmed_at) " +
                "VALUES (?, ?, ?, ?, ?, datetime('now'))"
            );
            stmt.setInt(1, util.Session.getCurrentUser().getId());
            stmt.setInt(2, selected.getId());
            stmt.setInt(3, 1);  //Default 1 slot
            stmt.setInt(4, 2);  //Default 2 hours
            stmt.setDouble(5, selected.getHourlyValue() * 2); //Contribution = hourly x hours
            stmt.executeUpdate();

            //Update project’s registered slots
            PreparedStatement update = conn.prepareStatement(
                "UPDATE projects SET registered_slots = registered_slots + 1 WHERE id = ?"
            );
            update.setInt(1, selected.getId());
            update.executeUpdate();

            messageLabel.setText("Registered for project: " + selected.getTitle());
            loadProjectsFromDB(); //Refresh table
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error registering for project.");
        }
    }

    //Switch to the "My Registrations" screen (triggers when you click the "My Registrations" button)
    @FXML
    private void goToMyRegistrations() {
        switchScene("/view/MyRegistrations.fxml", "VolunTrack - My Registrations");
    }


    //Navigate to Update Password screen.
    @FXML
    private void goToUpdatePassword() {
        switchScene("/view/UpdatePassword.fxml", "VolunTrack - Update Password");
    }
}
