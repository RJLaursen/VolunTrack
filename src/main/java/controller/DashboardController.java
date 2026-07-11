package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.DatabaseManager;
import model.Project;
import util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

//Controller for the Dashboard screen
//Displays projects and allows user registration for available ones
public class DashboardController {

    //FXML elements
    @FXML private TableView<Project> projectTable;
    @FXML private TableColumn<Project, String> titleCol;
    @FXML private TableColumn<Project, String> locationCol;
    @FXML private TableColumn<Project, String> dayCol;
    @FXML private TableColumn<Project, String> slotsCol;
    @FXML private TableColumn<Project, String> valueCol;

    @FXML private Label messageLabel;
    @FXML private Label welcomeLabel;

    private ObservableList<Project> projectList = FXCollections.observableArrayList();

    //Initialize the Dashboard screen and populate project data
    @FXML
    public void initialize() {
        if (Session.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome, " + Session.getCurrentUser().getUsername() + "!");
        }

        //Bind table columns to Project model properties
        titleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        locationCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLocation()));
        dayCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDay()));
        slotsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRegisteredSlots() + "/" + data.getValue().getTotalSlots()
        ));
        valueCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                "$" + data.getValue().getHourlyValue()
        ));

        //Load projects from database
        loadProjectsFromDB();
    }

    //Loads all available projects from the database
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

    //Handles registration when user clicks the "Register" button
    @FXML
    private void handleRegister() {
        Project selected = projectTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messageLabel.setText("⚠ Please select a project.");
            messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            return;
        }

        if (selected.getRegisteredSlots() >= selected.getTotalSlots()) {
            messageLabel.setText("⚠ This project is full!");
            messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            return;
        }

        try (java.sql.Connection conn = model.DatabaseManager.getInstance().getConnection()) {
            //Check if user already registered for this project
            java.sql.PreparedStatement check = conn.prepareStatement(
                "SELECT COUNT(*) FROM registrations WHERE user_id = ? AND project_id = ?"
            );
            check.setInt(1, util.Session.getCurrentUser().getId());
            check.setInt(2, selected.getId());
            java.sql.ResultSet rs = check.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                messageLabel.setText("⚠ You’re already registered for " + selected.getTitle() + "!");
                messageLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("⚠ Error checking registration status.");
            messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            return;
        }

        //Check if already in cart
        if (util.CartManager.getCartItems().contains(selected)) {
            messageLabel.setText("⚠ Project is already in your cart!");
            messageLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            return;
        }

        //Add project to in memory cart only (no DB write yet)
        util.CartManager.addToCart(selected);
        messageLabel.setText("✅ " + selected.getTitle() + " added to cart!");
        messageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    }

    //Refresh project list in Dashboard
    @FXML
    private void refreshProjects() {
        loadProjectsFromDB();
        messageLabel.setText("✅ Project list refreshed!");
    }

    //Switch to the My Registrations screen
    @FXML
    private void goToMyRegistrations() {
        switchScene("/view/MyRegistrations.fxml", "VolunTrack - My Registrations");
    }

    //Navigate to Update Password screen
    @FXML
    private void goToUpdatePassword() {
        switchScene("/view/UpdatePassword.fxml", "VolunTrack - Update Password");
    }

    //Logout and return to the Login screen
    @FXML
    private void handleLogout() {
        //Ask for confirmation before logging out
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Logout");
        confirm.setHeaderText("Are you sure you want to log out?");
        confirm.setContentText("Your current session will end and you’ll return to the login screen.");

        DialogPane pane = confirm.getDialogPane();
        pane.setStyle("-fx-font-size: 13px;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                //Clear session data
                util.Session.setCurrentUser(null);

                //Return to login screen
                try {
                    javafx.stage.Stage stage = (javafx.stage.Stage) projectTable.getScene().getWindow();
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/Login.fxml"));
                    javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                    stage.setScene(scene);
                    stage.setTitle("VolunTrack - Login");

                    //Fade out transition (because it looks cooler hehehe)
                    javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), scene.getRoot());
                    ft.setFromValue(0);
                    ft.setToValue(1);
                    ft.play();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //Navigate to Cart Screen
    @FXML
    private void goToCart() {
        switchScene("/view/Cart.fxml", "VolunTrack - Cart");
    }

    //Generic method for switching between scenes
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
}
