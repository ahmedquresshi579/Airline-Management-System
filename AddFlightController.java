import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.collections.FXCollections;
import Session;
import AdminDashboardController;

public class AddFlightController {
    @FXML private TextField flightID;
    @FXML private TextField departureAirport;
    @FXML private TextField arrivalAirport;
    @FXML private TextField departureTime;
    @FXML private TextField arrivalTime;
    @FXML private DatePicker flightDate;
    @FXML private ComboBox<String> aircraftComboBox;
    @FXML private TextField gate;
    @FXML private TextField searchField;
    @FXML private Button settingsButton;
    @FXML private ComboBox<String> statusComboBox;

    @FXML
    public void initialize() {
        loadAircraftData();
        statusComboBox.setItems(FXCollections.observableArrayList("Scheduled", "Completed", "Delayed"));
        searchField.setOnAction(e -> handleSearch());
        settingsButton.setOnAction(e -> handleSettings());
    }

    private void loadAircraftData() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT aircraftID, model, regNo FROM Aircraft";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String displayText = String.format("%s (%s)", 
                        rs.getString("model"), 
                        rs.getString("regNo"));
                    aircraftComboBox.getItems().add(displayText);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle database error
        }
    }

    @FXML
    private void handleAddFlight() {
        if (!validateInputs()) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try {
                // First, add to Schedule table
                String scheduleSql = "INSERT INTO Schedule (adminID, aircraftID, dates, gate) VALUES (?, ?, ?, ?)";
                int scheduleId;
                try (PreparedStatement scheduleStmt = conn.prepareStatement(scheduleSql, Statement.RETURN_GENERATED_KEYS)) {
                    scheduleStmt.setInt(1, Session.adminUserID);
                    scheduleStmt.setInt(2, getAircraftId(aircraftComboBox.getValue()));
                    scheduleStmt.setDate(3, java.sql.Date.valueOf(flightDate.getValue()));
                    scheduleStmt.setString(4, gate.getText());
                    scheduleStmt.executeUpdate();
                    
                    try (ResultSet generatedKeys = scheduleStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            scheduleId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating schedule failed, no ID obtained.");
                        }
                    }
                }

                // Then, add to Flights table with manual flightID
                String flightSql = "INSERT INTO Flights (flightID, departureAirport, arrivalAirport, departureTime, arrivalTime, scheduleID, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement flightStmt = conn.prepareStatement(flightSql)) {
                    flightStmt.setInt(1, Integer.parseInt(flightID.getText()));
                    flightStmt.setString(2, departureAirport.getText());
                    flightStmt.setString(3, arrivalAirport.getText());
                    flightStmt.setTime(4, java.sql.Time.valueOf(LocalTime.parse(departureTime.getText(), DateTimeFormatter.ofPattern("HH:mm"))));
                    flightStmt.setTime(5, java.sql.Time.valueOf(LocalTime.parse(arrivalTime.getText(), DateTimeFormatter.ofPattern("HH:mm"))));
                    flightStmt.setInt(6, scheduleId);
                    String status = statusComboBox.getValue();
                    if (status == null || status.isEmpty()) {
                        flightStmt.setString(7, ""); // Let trigger set default
                    } else {
                        flightStmt.setString(7, status);
                    }
                    flightStmt.executeUpdate();
                }

                conn.commit(); // Commit transaction
                showAlert(Alert.AlertType.INFORMATION, "Success", "Flight added successfully!");
                clearForm();
            } catch (SQLException e) {
                conn.rollback(); // Rollback transaction on error
                if (e.getMessage().contains("PRIMARY KEY") || e.getMessage().contains("duplicate key")) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Cannot add flight as Flight ID already exists.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add flight: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add flight: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (flightID.getText().isEmpty() || departureAirport.getText().isEmpty() || arrivalAirport.getText().isEmpty() ||
            departureTime.getText().isEmpty() || arrivalTime.getText().isEmpty() ||
            flightDate.getValue() == null || aircraftComboBox.getValue() == null ||
            gate.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required!");
            return false;
        }

        try {
            Integer.parseInt(flightID.getText()); // Validate flightID is a number
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Flight ID must be a number!");
            return false;
        }

        try {
            LocalTime.parse(departureTime.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime.parse(arrivalTime.getText(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid time format! Use HH:mm");
            return false;
        }

        return true;
    }

    private int getAircraftId(String displayText) {
        String regNo = displayText.substring(displayText.indexOf("(") + 1, displayText.indexOf(")"));
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT aircraftID FROM Aircraft WHERE regNo = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, regNo);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("aircraftID");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void clearForm() {
        flightID.clear();
        departureAirport.clear();
        arrivalAirport.clear();
        departureTime.clear();
        arrivalTime.clear();
        flightDate.setValue(null);
        aircraftComboBox.setValue(null);
        gate.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        showHome();
    }

    // Navigation methods
    @FXML
    private void showHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
            Parent root = loader.load();
            AdminDashboardController controller = loader.getController();
            controller.setAdminName(Session.adminName);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            Stage stage = (Stage) flightID.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showAddFlight() {
        loadScene("AddFlight.fxml");
    }

    @FXML
    private void showEditFlight() {
        loadScene("EditFlight.fxml");
    }

    @FXML
    private void showViewPassengers() {
        loadScene("ViewPassenger.fxml");
    }

    @FXML
    private void showAssignCrew() {
        loadScene("AddCrew.fxml");
    }

    private void loadScene(String fxmlFile) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlFile));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) gate.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            // Implement search functionality
        }
    }

    @FXML
    private void handleSettings() {
        // Implement settings functionality
    }
} 