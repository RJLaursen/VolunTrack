package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.DatabaseManager;
import model.UserDirectAccessObject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

//Controller for the Signup screen
//Handles new user registration, password validation, show/hide functionality, and navigation
public class SignupController {

    //Input fields
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    //Visible text fields (for showing password)
    @FXML private TextField visiblePasswordField;
    @FXML private TextField visibleConfirmPasswordField;

    //Buttons to toggle visibility
    @FXML private Button togglePasswordBtn;
    @FXML private Button toggleConfirmPasswordBtn;

    @FXML private Label messageLabel;

    private UserDirectAccessObject userDAO;

    @FXML
    public void initialize() {
        try {
            userDAO = new UserDirectAccessObject(DatabaseManager.getInstance().getConnection());
        } catch (Exception e) {
            messageLabel.setText("Database error!");
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }

        //Keep visible fields connected with hidden ones
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    //Toggle visibility for password field
    @FXML
    private void togglePasswordVisibility() {
        boolean visible = visiblePasswordField.isVisible();
        visiblePasswordField.setVisible(!visible);
        visiblePasswordField.setManaged(!visible);
        passwordField.setVisible(visible);
        passwordField.setManaged(visible);
    }

    //Toggle visibility for confirm password field
    @FXML
    private void toggleConfirmPasswordVisibility() {
        boolean visible = visibleConfirmPasswordField.isVisible();
        visibleConfirmPasswordField.setVisible(!visible);
        visibleConfirmPasswordField.setManaged(!visible);
        confirmPasswordField.setVisible(visible);
        confirmPasswordField.setManaged(visible);
    }

    //Handles signup logic with password validation and color coded feedback
    @FXML
    private void handleSignup() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        //Validation
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showMessage("⚠ Please fill in all fields.", "red");
            return;
        }

        if (!email.matches("^[\\w.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showMessage("⚠ Please enter a valid email address.", "red");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("⚠ Passwords do not match.", "red");
            return;
        }

        if (!UserDirectAccessObject.isValidPassword(password)) {
            showMessage("⚠ Password must include uppercase, number, and special character.", "red");
            return;
        }

        //Attempt registration
        boolean success = userDAO.registerUser(fullName, username, email, password, false);
        if (success) {
            showMessage("✅ Account created! Please login.", "green");
        } else {
            showMessage("⚠ Signup failed. Username/email may already exist.", "red");
        }
    }

    //Helper method for feedback messages
    private void showMessage(String text, String color) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    //Navigate back to Login screen
    @FXML
    private void goToLogin() {
        switchScene("/view/Login.fxml", "VolunTrack - Login");
    }

    //Helper to change scenes
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
