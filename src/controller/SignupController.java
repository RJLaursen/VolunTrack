package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import model.DatabaseManager;
import model.User;
import model.UserDirectAccessObject;

//Controller for the Signup screen
//Handles new user registration and navigation back to login

public class SignupController {
    //Input fields from the Signup.fxml
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    //DAO for database access (users table)
    private UserDirectAccessObject userDAO;

    //Called automatically when the Signup screen loads
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

    //Handles the Signup button
    @FXML
    private void handleSignup() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        boolean success = userDAO.registerUser(fullName, username, email, password, false);
        if (success) {
            messageLabel.setText("Account created! Please login.");
        } else {
            messageLabel.setText("Signup failed. Username/email may already exist.");
        }
    }

    //Handles the "Back to Login" button click
    @FXML
    private void goToLogin() {
        switchScene("/view/Login.fxml", "VolunTrack - Login");
    }

    //Utility method to switch between FXML screens
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
