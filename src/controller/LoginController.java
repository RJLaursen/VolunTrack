package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import model.DatabaseManager;
import model.User;
import model.UserDirectAccessObject;

//Controller for the Login screen
//Handles user authentication and navigation to other screens

public class LoginController {
    //Input fields from the Login.fxml
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    //Data Access Object for interacting with the User table in DB
    private UserDirectAccessObject userDAO;

    //Called automatically when the Login screen loads
    //Initializes the User DAO with a database connection
    @FXML
    public void initialize() {
        try {
            userDAO = new UserDirectAccessObject(DatabaseManager.getInstance().getConnection());
        } catch (Exception e) {
            messageLabel.setText("Database error!");
            e.printStackTrace();
        }
    }

    //Handles the Login button when you click it
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (userDAO.loginUser(username, password)) {
            messageLabel.setText("Login successful!");

            //Store the logged in user in the session for later use
            User loggedUser = userDAO.getUserByUsername(username);
            util.Session.setCurrentUser(loggedUser);
            
            openDashboard();

        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }

    //Store the logged in user in the session for later use
    @FXML
    private void goToSignup() {
        switchScene("/view/Signup.fxml", "VolunTrack - Signup");
    }

    //Opens the Dashboard screen after successful login
    private void openDashboard() {
        switchScene("/view/Dashboard.fxml", "VolunTrack - Dashboard");
    }

    //Utility method to switch between FXML scenes
    private void switchScene(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
