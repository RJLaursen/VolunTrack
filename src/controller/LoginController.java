package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import model.DatabaseManager;
import model.User;
import model.UserDirectAccessObject;
import util.Session;

//Controller for the Login screen
//Handles authentication, password visibility toggle, and navigation to Signup or Dashboard
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    //Visible password field for toggle
    @FXML private TextField visiblePasswordField;
    @FXML private Button togglePasswordBtn;

    @FXML private Label messageLabel;

    private UserDirectAccessObject userDAO;

    @FXML
    public void initialize() {
        try {
            userDAO = new UserDirectAccessObject(DatabaseManager.getInstance().getConnection());
        } catch (Exception e) {
            showMessage("Database error!", "red");
            e.printStackTrace();
        }

        //Keep both password fields connected
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    //Handles user login validation
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("⚠ Please enter both username and password.", "red");
            return;
        }

        if (userDAO.loginUser(username, password)) {
            showMessage("✅ Login successful!", "green");

            User loggedUser = userDAO.getUserByUsername(username);
            Session.setCurrentUser(loggedUser);

            //Short delay for user feedback before loading dashboard (just cause it looks cool)
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(0.6));
            pause.setOnFinished(e -> openDashboard());
            pause.play();
        } else {
            showMessage("⚠ Invalid username or password.", "red");
        }
    }

    //Toggle password visibility (👁 button) [The eyeball]
    @FXML
    private void togglePasswordVisibility() {
        boolean visible = visiblePasswordField.isVisible();
        visiblePasswordField.setVisible(!visible);
        visiblePasswordField.setManaged(!visible);
        passwordField.setVisible(visible);
        passwordField.setManaged(visible);
    }

    //Navigate to Signup screen
    @FXML
    private void goToSignup() {
        switchScene("/view/Signup.fxml", "VolunTrack - Signup");
    }

    //Open Dashboard after login success
    private void openDashboard() {
        switchScene("/view/Dashboard.fxml", "VolunTrack - Dashboard");
    }

    //Helper for switching screens
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

    //Display messages with color styling
    private void showMessage(String text, String color) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }
}
