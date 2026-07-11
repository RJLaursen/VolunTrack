package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import model.Project;
import util.CartManager;
import util.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.Random;

//Controller for the Cart screen
//Allows users to confirm project registrations with a simulated 6-digit code
public class CartController {

    //FXML elements
    @FXML private TableView<Project> cartTable;
    @FXML private TableColumn<Project, String> titleCol;
    @FXML private TableColumn<Project, String> dayCol;
    @FXML private TableColumn<Project, String> slotsCol;
    @FXML private TableColumn<Project, String> hoursCol;
    @FXML private TableColumn<Project, String> valueCol;
    @FXML private TableColumn<Project, String> totalCol;

    @FXML private Label codeLabel;
    @FXML private TextField codeField;
    @FXML private Label messageLabel;

    @FXML private Spinner<Integer> slotsSpinner;
    @FXML private Spinner<Integer> hoursSpinner;

    @FXML private Label totalCartLabel;

    private ObservableList<Project> cartItems = FXCollections.observableArrayList();
    private String generatedCode = null;

    @FXML
    public void initialize() {
        titleCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTitle()));
        dayCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDay()));
        valueCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("$" + d.getValue().getHourlyValue()));

        //Make Slots editable
        slotsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getSelectedSlots())));
        slotsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        slotsCol.setOnEditCommit(e -> {
            try {
                int val = Integer.parseInt(e.getNewValue());
                if (val < 1 || val > 3) throw new NumberFormatException();
                e.getRowValue().setSelectedSlots(val);
                updateTotals();
            } catch (NumberFormatException ex) {
                showMessage("⚠ Slots must be between 1–3.", "red");
                cartTable.refresh();
            }
        });

        //Make Hours editable
        hoursCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getSelectedHours())));
        hoursCol.setCellFactory(TextFieldTableCell.forTableColumn());
        hoursCol.setOnEditCommit(e -> {
            try {
                int val = Integer.parseInt(e.getNewValue());
                if (val < 1 || val > 3) throw new NumberFormatException();
                e.getRowValue().setSelectedHours(val);
                updateTotals();
            } catch (NumberFormatException ex) {
                showMessage("⚠ Hours must be between 1–3.", "red");
                cartTable.refresh();
            }
        });

        //Live total contribution per row (hourly × hours × slots, same as requirements basically)
        totalCol.setCellValueFactory(d -> {
            double total = d.getValue().getHourlyValue() * d.getValue().getSelectedHours() * d.getValue().getSelectedSlots();
            return new javafx.beans.property.SimpleStringProperty("$" + String.format("%.2f", total));
        });

        cartTable.setEditable(true);
        cartItems.setAll(CartManager.getCartItems());
        cartTable.setItems(cartItems);

        //Initial total display
        updateTotals();
    }

    //Recalculates and displays total contribution for the entire cart
    private void updateTotals() {
        double grandTotal = 0;
        for (Project p : cartItems) {
            grandTotal += p.getHourlyValue() * p.getSelectedHours() * p.getSelectedSlots();
        }
        totalCartLabel.setText("Total Contribution: $" + String.format("%.2f", grandTotal));
        cartTable.refresh();
    }

    //Remove a selected projects from the cart
    @FXML
    private void removeSelected() {
        Project selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            CartManager.removeFromCart(selected);
            cartItems.remove(selected);
            updateTotals();
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

    //Confirm using generated code ^^^ (with hours/slots support and validation)
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
            boolean anySuccess = false;

            for (Project p : CartManager.getCartItems()) {
                //Validate project day
                if (!isValidDay(p.getDay())) {
                    showMessage("Cannot register for past project: " + p.getTitle(), "red");
                    continue;
                }

                int slots = p.getSelectedSlots();
                int hours = p.getSelectedHours();
                double contribution = p.getHourlyValue() * hours * slots;

                //Perform registration
                String resultMsg = model.RegistrationDAO.addRegistration(
                    userId,
                    p.getId(),
                    slots,
                    hours,
                    contribution
                );

                if (resultMsg.startsWith("✅")) {
                    anySuccess = true;
                    showMessage(p.getTitle() + ": " + resultMsg, "green");
                } else if (resultMsg.startsWith("⚠")) {
                    showMessage(p.getTitle() + ": " + resultMsg, "orange");
                } else {
                    showMessage(p.getTitle() + ": " + resultMsg, "red");
                }

                //Give SQLite time to finalize each insert (Quick pause)
                Thread.sleep(100);
            }

            if (anySuccess) {
                //Clear cart after successful registrations
                CartManager.clearCart();
                cartItems.clear();
                updateTotals();
                showMessage("✅ All valid registrations confirmed!", "green");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error confirming registration.", "red");
        }
    }

    //Validates project dates (had to fix due to error occuring)
    private boolean isValidDay(String projectDay) {
        try {
            //Normalize input like "Mon" or "monday" (mostly for consistency with the original csv file)
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

            DayOfWeek projectDayOfWeek = DayOfWeek.valueOf(normalized.toUpperCase());
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            return projectDayOfWeek.getValue() >= today.getValue();
        } catch (Exception e) {
            System.out.println("Invalid day format: " + projectDay);
            return false;
        }
    }

    //Update the selected project in cart with new slots and hours
    @FXML
    private void updateSelected() {
        Project selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select a project to update.", "red");
            return;
        }

        int newSlots = slotsSpinner.getValue();
        int newHours = hoursSpinner.getValue();

        //Validation
        if (newSlots < 1 || newSlots > 3) {
            showMessage("Slots must be between 1 and 3.", "red");
            return;
        }
        if (newHours < 1 || newHours > 3) {
            showMessage("Hours must be between 1 and 3.", "red");
            return;
        }

        //Update user selection (not DB fields)
        selected.setSelectedSlots(newSlots);
        selected.setSelectedHours(newHours);

        //Sync with CartManager to ensure persistence
        CartManager.updateItem(selected);

        updateTotals();
        cartTable.refresh();
        showMessage("Updated " + selected.getTitle() + " with " + newSlots + " slot(s), " + newHours + " hour(s).", "green");
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
