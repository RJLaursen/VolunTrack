package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import model.DatabaseManager;
import model.UserDirectAccessObject;
import util.Session;

//Controller for the Update Password screen and allow users to change their password if need be
public class UpdatePasswordController {
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private UserDirectAccessObject userDAO;

    @FXML
    public void initialize() {
        try {
            userDAO = new UserDirectAccessObject(DatabaseManager.getInstance().getConnection());
        } catch (Exception e) {
            messageLabel.setText("Database error!");
            e.printStackTrace();
        }
    }

    //Handle the Update button click.
    @FXML
    private void handleUpdate() {
        String oldPass = oldPasswordField.getText().trim();
        String newPass = newPasswordField.getText().trim();
        String confirmPass = confirmPasswordField.getText().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        //Verify old password
        String username = Session.getCurrentUser().getUsername();
        if (!userDAO.loginUser(username, oldPass)) {
            messageLabel.setText("Old password is incorrect.");
            return;
        }

        //Validate new password
        if (!UserDirectAccessObject.isValidPassword(newPass)) {
            messageLabel.setText("Password must be at least 6 characters.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        //Update password
        boolean success = userDAO.updatePassword(Session.getCurrentUser().getId(), newPass);
        if (success) {
            messageLabel.setText("Password updated successfully!");
        } else {
            messageLabel.setText("Password update failed.");
        }
    }

    //Go back to the Dashboard
    @FXML
    private void goBack() {
        switchScene("/view/Dashboard.fxml", "VolunTrack - Dashboard");
    }

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
