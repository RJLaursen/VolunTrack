package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import model.Project;
import model.ProjectDAO;
import util.Session;

import java.util.List;

//Controller for the Admin Dashboard screen
//Allows admin to view, add, edit, delete, enable/disable projects and view registrations
public class AdminDashboardController {

    //FXML elements
    @FXML private ListView<String> titlesList;
    @FXML private TableView<Project> projectsTable;

    @FXML private TableColumn<Project, String> idCol;
    @FXML private TableColumn<Project, String> titleCol;
    @FXML private TableColumn<Project, String> locationCol;
    @FXML private TableColumn<Project, String> dayCol;
    @FXML private TableColumn<Project, String> hourlyCol;
    @FXML private TableColumn<Project, String> slotsCol;
    @FXML private TableColumn<Project, String> enabledCol;

    @FXML private Label messageLabel;

    private ObservableList<Project> projects = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Simple redirect safety: Ensure only admin can see this screen (Session check)
        //We don't want an oopsie here
        if (Session.getCurrentUser() == null || !Session.getCurrentUser().isAdmin()) {
            //Fallback to login
            switchToLogin();
            return;
        }

        //Bind table columns to Project model properties
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getId())));
        titleCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTitle()));
        locationCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getLocation()));
        dayCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDay()));
        hourlyCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getHourlyValue())));
        slotsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRegisteredSlots() + "/" + d.getValue().getTotalSlots()));
        enabledCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().isEnabled() ? "Yes" : "No"));

        //Load all project titles to populate the list view
        loadTitles();
    }

    //Loads all unique project titles from database
    private void loadTitles() {
        try {
            List<String> titles = ProjectDAO.getDistinctTitles();
            titlesList.setItems(FXCollections.observableArrayList(titles));

            //When an item is selected, load projects with that title
            titlesList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) loadProjectsForTitle(newV);
            });

            //If non-empty, select first title to show table
            if (!titles.isEmpty()) {
                titlesList.getSelectionModel().select(0);
                loadProjectsForTitle(titles.get(0));
            } else {
                projects.clear();
                projectsTable.setItems(projects);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error loading titles", "red");
        }
    }

    //Loads all project variations for a selected title
    private void loadProjectsForTitle(String title) {
        try {
            List<Project> list = ProjectDAO.getProjectsByTitle(title);
            projects.setAll(list);
            projectsTable.setItems(projects);
            showMessage("Loaded projects for: " + title, "green");
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error loading projects", "red");
        }
    }

    //Handle Add Project button
    @FXML
    private void onAddProject() {
        ProjectFormDialog dialog = new ProjectFormDialog(null);
        dialog.showAndWait().ifPresent(data -> {
            try {
                boolean ok = ProjectDAO.addProject(data.title, data.location, data.day, data.hourly, data.totalSlots);
                if (!ok) {
                    showMessage("Duplicate project exists (title/location/day).", "red");
                } else {
                    showMessage("Project added successfully.", "green");
                    loadTitles();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("Error adding project.", "red");
            }
        });
    }

    //Handle Edit Project button
    @FXML
    private void onEditSelected() {
        Project sel = projectsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showMessage("Select a project to edit.", "red");
            return;
        }

        //Open edit dialog with selected project data
        ProjectFormDialog dialog = new ProjectFormDialog(sel);
        dialog.showAndWait().ifPresent(data -> {
            try {
                boolean ok = ProjectDAO.updateProject(sel.getId(), data.title, data.location, data.day, data.hourly, data.totalSlots);
                if (!ok) {
                    showMessage("Duplicate project exists after modification.", "red");
                } else {
                    showMessage("Project updated.", "green");
                    loadTitles();
                    titlesList.getSelectionModel().select(data.title);
                    loadProjectsForTitle(data.title);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("Error updating project.", "red");
            }
        });
    }

    //Handle Delete Project button
    @FXML
    private void onDeleteSelected() {
        Project sel = projectsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showMessage("Select a project to delete.", "red");
            return;
        }

        //Ask for confirmation before deleting
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete project: " + sel.getTitle() + " (" + sel.getDay() + ")?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirm delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    ProjectDAO.deleteProject(sel.getId());
                    showMessage("Project deleted.", "green");
                    loadTitles();
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage("Error deleting project.", "red");
                }
            }
        });
    }

    //Handle Enable/Disable button
    @FXML
    private void onToggleEnabled() {
        Project sel = projectsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showMessage("Select a project to enable/disable.", "red");
            return;
        }
        try {
            boolean newState = !sel.isEnabled();
            ProjectDAO.setEnabled(sel.getId(), newState);
            showMessage((newState ? "Enabled" : "Disabled") + " project: " + sel.getTitle(), "green");
            loadTitles();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error toggling enabled.", "red");
        }
    }

    //Handle View All Registrations button
    @FXML
    private void onViewAllRegistrations() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/AdminRegistrations.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("All Registrations");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error opening registrations view.", "red");
        }
    }

    @FXML
    private void onRefresh() { loadTitles(); }

    @FXML
    private void onLogout() { Session.setCurrentUser(null); switchToLogin(); }

    //Display feedback message (with colors)
    private void showMessage(String text, String color) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    //Switch scene back to Login view
    private void switchToLogin() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) projectsTable.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("VolunTrack - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //FormData inner class for Add/Edit form results
    private static class FormData {
        final String title;
        final String location;
        final String day;
        final double hourly;
        final int totalSlots;
        FormData(String t, String l, String d, double h, int s) { title = t; location = l; day = d; hourly = h; totalSlots = s; }
    }

    //Custom dialog for creating and editing projects
    private static class ProjectFormDialog extends Dialog<FormData> {
        ProjectFormDialog(Project existing) {
            setTitle(existing == null ? "Add Project" : "Edit Project");
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            //Input feilds
            TextField titleField = new TextField();
            TextField locationField = new TextField();
            TextField dayField = new TextField();
            TextField hourlyField = new TextField();
            TextField slotsField = new TextField();

            if (existing != null) {
                titleField.setText(existing.getTitle());
                locationField.setText(existing.getLocation());
                dayField.setText(existing.getDay());
                hourlyField.setText(String.valueOf(existing.getHourlyValue()));
                slotsField.setText(String.valueOf(existing.getTotalSlots()));
            }

            //Gridpane to arrange form
            GridPane grid = new GridPane();
            grid.setVgap(8);
            grid.setHgap(8);
            grid.addRow(0, new Label("Title:"), titleField);
            grid.addRow(1, new Label("Location:"), locationField);
            grid.addRow(2, new Label("Day (Mon/Tue/...):"), dayField);
            grid.addRow(3, new Label("Hourly value (1..100):"), hourlyField);
            grid.addRow(4, new Label("Total slots (1..100):"), slotsField);
            getDialogPane().setContent(grid);

            final Button okBtn = (Button) getDialogPane().lookupButton(ButtonType.OK);
            okBtn.setDisable(false);

            setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    String t = titleField.getText().trim();
                    String l = locationField.getText().trim();
                    String d = dayField.getText().trim();
                    String hv = hourlyField.getText().trim();
                    String sl = slotsField.getText().trim();

                    //Validate basic text fields
                    if (t.isEmpty() || t.length() > 30) { showAlert("Title must be 1..30 characters"); return null; }
                    if (l.isEmpty() || l.length() > 30) { showAlert("Location must be 1..30 characters"); return null; }
                    
                    //Validate day input
                    String dn = d.trim().toLowerCase();
                    if (dn.length() < 3) { showAlert("Day must be Mon/Tue/.../Sun"); return null; }
                    String[] allowed = {"mon","tue","wed","thu","fri","sat","sun","monday","tuesday","wednesday","thursday","friday","saturday","sunday"};
                    boolean ok = false;
                    for (String a : allowed) if (a.equals(dn)) { ok = true; break; }
                    if (!ok) { showAlert("Day must be one of Mon,Tue,Wed,Thu,Fri,Sat,Sun"); return null; }

                    //Validate numeric input
                    double hourly;
                    int totalSlots;
                    try { hourly = Double.parseDouble(hv); } catch (Exception ex) { showAlert("Hourly value must be a number"); return null; }
                    try { totalSlots = Integer.parseInt(sl); } catch (Exception ex) { showAlert("Total slots must be an integer"); return null; }

                    //Ensure numeric values within range
                    if (hourly < 1 || hourly > 100) { showAlert("Hourly value must be 1..100"); return null; }
                    if (totalSlots < 1 || totalSlots > 100) { showAlert("Total slots must be 1..100"); return null; }

                    //Normalize day name for consistent storage
                    String dayNormalized = dn.length() > 3 ? dn.substring(0,3) : dn;
                    return new FormData(t, l, dayNormalized, hourly, totalSlots);
                }
                return null;
            });
        }

        //Show alert pop up for invalid input
        private void showAlert(String text) {
            Alert a = new Alert(Alert.AlertType.ERROR, text, ButtonType.OK);
            a.setHeaderText("Validation error");
            a.showAndWait();
        }
    }
}
