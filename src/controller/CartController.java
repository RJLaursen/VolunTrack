package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import model.Project;
import model.DatabaseManager;
import util.CartManager;
import util.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.Random;

//Controller for the Cart screen
//Allows users to confirm project registrations with a simulated 6-digit code
public class CartController {

    @FXML private TableView<Project> cartTable;
    @FXML private TableColumn<Project, String> titleCol;
    @FXML private TableColumn<Project, String> dayCol;
    @FXML private TableColumn<Project, String> slotsCol;
    @FXML private TableColumn<Project, String> hoursCol;
    @FXML private TableColumn<Project, String> valueCol;

    @FXML private Label codeLabel;
    @FXML private TextField codeField;
    @FXML private Label messageLabel;

    private ObservableList<Project> cartItems = FXCollections.observableArrayList();
    private String generatedCode = null;

    @FXML
    public void initialize() {
        titleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        dayCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDay()));
        slotsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getAvailableSlots())));
        hoursCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("2")); //Default hours
        valueCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("$" + data.getValue().getHourlyValue()));

        cartItems.setAll(CartManager.getCartItems());
        cartTable.setItems(cartItems);
    }

    //Remove a selected projects from the cart
    @FXML
    private void removeSelected() {
        Project selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            CartManager.removeFromCart(selected);
            cartItems.remove(selected);
            showMessage("Removed " + selected.getTitle() + " from cart.", "orange");
        } else {
            showMessage("Select a project to remove.", "red");
        }
    }

    //Generates confirmation code (doesn't send to email, more like a "simulated experience")
    @FXML
    private void generateCode() {
        generatedCode = String.valueOf(100000 + new Random().nextInt(900000));
        codeLabel.setText("Your confirmation code is: " + generatedCode + " (simulated)");
        showMessage("A confirmation code has been generated!", "green");
    }

    //Confirm using generated code (the one above ^^^)
    @FXML
    private void confirmRegistration() {
        if (generatedCode == null) {
            showMessage("Generate a confirmation code first.", "red");
            return;
        }

        String userInput = codeField.getText().trim();
        if (!userInput.equals(generatedCode)) {
            showMessage("Invalid confirmation code.", "red");
            return;
        }

        try {
            int userId = Session.getCurrentUser().getId();

            for (Project p : CartManager.getCartItems()) {
                //Validate project day (Must be current or future)
                if (!isValidDay(p.getDay())) {
                    showMessage("Cannot register for past project: " + p.getTitle(), "red");
                    continue;
                }

                //Use DAO to handle registration and slot update
                boolean success = model.RegistrationDAO.addRegistration(
                    userId,
                    p.getId(),
                    1, //Default slots
                    2, //Default hours
                    p.getHourlyValue() * 2
                );

                if (success) {
                    showMessage("✅ " + p.getTitle() + " confirmed successfully!", "green");
                } else {
                    showMessage("⚠ Already registered for " + p.getTitle(), "orange");
                }
            }

            //Clear cart and refresh table
            CartManager.clearCart();
            cartItems.clear();

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error confirming registration.", "red");
        }
    }


    //Validates project dates (had to fix due to error occuring)
    private boolean isValidDay(String projectDay) {
    try {
        //Normalize input like "Mon" or "monday"
        String normalized = projectDay.trim().toLowerCase();

        //Convert abbreviations to full names
        switch (normalized) {
            case "mon": normalized = "monday"; break;
            case "tue": case "tues": normalized = "tuesday"; break;
            case "wed": normalized = "wednesday"; break;
            case "thu": case "thur": case "thurs": normalized = "thursday"; break;
            case "fri": normalized = "friday"; break;
            case "sat": normalized = "saturday"; break;
            case "sun": normalized = "sunday"; break;
        }

            //Convert to DayOfWeek enum
            DayOfWeek projectDayOfWeek = DayOfWeek.valueOf(normalized.toUpperCase());
            DayOfWeek today = LocalDate.now().getDayOfWeek();

            //Compare using numeric values
            int projectValue = projectDayOfWeek.getValue();
            int todayValue = today.getValue();

            //The project must be today or after today
            return projectValue >= todayValue;
        } catch (Exception e) {
            System.out.println("Invalid day format: " + projectDay);
            return false;
        }
    }



    //Back to dashboard
    @FXML
    private void goBack() {
        switchScene("/view/Dashboard.fxml", "VolunTrack - Dashboard");
    }

    private void showMessage(String text, String color) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) cartTable.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
