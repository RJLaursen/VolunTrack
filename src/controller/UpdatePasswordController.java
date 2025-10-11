package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import model.DatabaseManager;
import model.UserDirectAccessObject;
import util.Session;

//Controller for the Update Password screen
//Allows users to securely change their password with visual feedback and visibility toggles
public class UpdatePasswordController {

    //Password fields
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    //Unmasked password fields
    @FXML private TextField visibleOldPasswordField;
    @FXML private TextField visibleNewPasswordField;
    @FXML private TextField visibleConfirmPasswordField;

    //"Look at PW" toggle buttons
    @FXML private Button toggleOldPasswordBtn;
    @FXML private Button toggleNewPasswordBtn;
    @FXML private Button toggleConfirmPasswordBtn;

    @FXML private Label messageLabel;

    private UserDirectAccessObject userDAO;

    @FXML
    public void initialize() {
        try {
            userDAO = new UserDirectAccessObject(DatabaseManager.getInstance().getConnection());
        } catch (Exception e) {
            showMessage("⚠ Database error!", "red");
            e.printStackTrace();
        }

        //Keep visible/hidden password fields connected
        visibleOldPasswordField.textProperty().bindBidirectional(oldPasswordField.textProperty());
        visibleNewPasswordField.textProperty().bindBidirectional(newPasswordField.textProperty());
        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    //Handle password update logic
    @FXML
    private void handleUpdate() {
        String oldPass = oldPasswordField.getText().trim();
        String newPass = newPasswordField.getText().trim();
        String confirmPass = confirmPasswordField.getText().trim();

        //Validate input completeness
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showMessage("⚠ Please fill in all fields.", "red");
            return;
        }

        //Verify old password
        String username = Session.getCurrentUser().getUsername();
        if (!userDAO.loginUser(username, oldPass)) {
            showMessage("⚠ Old password is incorrect.", "red");
            return;
        }

        //Validate password strength (custom rule: at least 8 chars, uppercase, number, special)
        if (!isStrongPassword(newPass)) {
            showMessage("⚠ Password must have 8+ chars, uppercase, number, and special character.", "red");
            return;
        }

        //Confirm new password match
        if (!newPass.equals(confirmPass)) {
            showMessage("⚠ Passwords do not match.", "red");
            return;
        }

        //Perform update
        boolean success = userDAO.updatePassword(Session.getCurrentUser().getId(), newPass);
        if (success) {
            showMessage("✅ Password updated successfully!", "green");
        } else {
            showMessage("⚠ Password update failed. Try again.", "red");
        }
    }

    //Password strength validator
    private boolean isStrongPassword(String password) {
        return password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&  //At least one uppercase
               password.matches(".*[0-9].*") &&  //At least one number
               password.matches(".*[!@#$%^&*(),.?\":{}|<>].*"); //At least one special char
    }

    //Toggle visibility for Old Password
    @FXML private void toggleOldPasswordVisibility() { toggleVisibility(oldPasswordField, visibleOldPasswordField); }

    //Toggle visibility for New Password
    @FXML private void toggleNewPasswordVisibility() { toggleVisibility(newPasswordField, visibleNewPasswordField); }

    //Toggle visibility for Confirm Password
    @FXML private void toggleConfirmPasswordVisibility() { toggleVisibility(confirmPasswordField, visibleConfirmPasswordField); }

    //Reusable visibility toggle
    private void toggleVisibility(PasswordField hidden, TextField visible) {
        boolean showing = visible.isVisible();
        visible.setVisible(!showing);
        visible.setManaged(!showing);
        hidden.setVisible(showing);
        hidden.setManaged(showing);
    }

    //Return to Dashboard
    @FXML
    private void goBack() {
        switchScene("/view/Dashboard.fxml", "VolunTrack - Dashboard");
    }

    //Color coded feedback display
    private void showMessage(String text, String color) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    //Utility: switch between scenes
    private void switchScene(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) oldPasswordField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
