import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.collections.FXCollections;

public class EditFlightController {
    @FXML private TextField flightId;
    @FXML private DatePicker flightDate;
    @FXML private ComboBox<String> aircraftComboBox;
    @FXML private TextField gate;
    @FXML private TextField searchField;
    @FXML private Button settingsButton;
    @FXML private TextField departureAirport;
    @FXML private TextField arrivalAirport;
    @FXML private TextField departureTime;
    @FXML private TextField arrivalTime;
    @FXML private ComboBox<String> statusComboBox;

    @FXML
    private void handleSearch(javafx.event.ActionEvent event) {
        String flightIdText = flightId.getText();
        if (flightIdText == null || flightIdText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a Flight ID.");
            return;
        }
        try {
            int flightID = Integer.parseInt(flightIdText);
            try (java.sql.Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT f.departureAirport, f.arrivalAirport, f.departureTime, f.arrivalTime, " +
                        "s.dates, s.gate, a.model, a.regNo, f.scheduleID, f.status " +
                        "FROM Flights f " +
                        "JOIN Schedule s ON f.scheduleID = s.scheduleID " +
                        "JOIN Aircraft a ON s.aircraftID = a.aircraftID " +
                        "WHERE f.flightID = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, flightID);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            departureAirport.setText(rs.getString("departureAirport"));
                            arrivalAirport.setText(rs.getString("arrivalAirport"));
                            java.sql.Time depTime = rs.getTime("departureTime");
                            java.sql.Time arrTime = rs.getTime("arrivalTime");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                            if (depTime != null) {
                                departureTime.setText(depTime.toLocalTime().format(formatter));
                            } else {
                                departureTime.setText("");
                            }
                            if (arrTime != null) {
                                arrivalTime.setText(arrTime.toLocalTime().format(formatter));
                            } else {
                                arrivalTime.setText("");
                            }
                            flightDate.setValue(rs.getDate("dates").toLocalDate());
                            gate.setText(rs.getString("gate"));
                            // Set aircraft combo box value
                            String model = rs.getString("model");
                            String regNo = rs.getString("regNo");
                            aircraftComboBox.setValue(model + " (" + regNo + ")");
                            // Set status combo box value
                            statusComboBox.setValue(rs.getString("status"));
                            // Optionally, set scheduleID if you want to use it
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Not Found", "No flight found with the given ID.");
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Flight ID must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleEditFlight(ActionEvent event) {
        if (flightId.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Flight ID is required!");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // 1. Get scheduleID for this flight
            int scheduleId = -1;
            String getScheduleSql = "SELECT scheduleID FROM Flights WHERE flightID = ?";
            try (PreparedStatement getScheduleStmt = conn.prepareStatement(getScheduleSql)) {
                getScheduleStmt.setInt(1, Integer.parseInt(flightId.getText()));
                try (ResultSet rs = getScheduleStmt.executeQuery()) {
                    if (rs.next()) {
                        scheduleId = rs.getInt("scheduleID");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "No flight found with this ID.");
                        return;
                    }
                }
            }

            // 2. Update Flights table
            String updateFlightSql = "UPDATE Flights SET departureAirport=?, arrivalAirport=?, departureTime=?, arrivalTime=?, status=? WHERE flightID=?";
            try (PreparedStatement stmt = conn.prepareStatement(updateFlightSql)) {
                stmt.setString(1, departureAirport.getText());
                stmt.setString(2, arrivalAirport.getText());
                stmt.setTime(3, java.sql.Time.valueOf(java.time.LocalTime.parse(departureTime.getText(), DateTimeFormatter.ofPattern("HH:mm"))));
                stmt.setTime(4, java.sql.Time.valueOf(java.time.LocalTime.parse(arrivalTime.getText(), DateTimeFormatter.ofPattern("HH:mm"))));
                String status = statusComboBox.getValue();
                if (status == null || status.isEmpty()) {
                    stmt.setString(5, ""); // Let trigger set default
                } else {
                    stmt.setString(5, status);
                }
                stmt.setInt(6, Integer.parseInt(flightId.getText()));
                stmt.executeUpdate();
            }

            // 3. Update Schedule table
            int aircraftId = getAircraftId(aircraftComboBox.getValue());
            if (aircraftId == -1) {
                showAlert(Alert.AlertType.ERROR, "Error", "Selected aircraft does not exist.");
                conn.rollback();
                return;
            }
            String updateScheduleSql = "UPDATE Schedule SET dates=?, gate=?, aircraftID=? WHERE scheduleID=?";
            try (PreparedStatement stmt = conn.prepareStatement(updateScheduleSql)) {
                stmt.setDate(1, java.sql.Date.valueOf(flightDate.getValue()));
                stmt.setString(2, gate.getText());
                stmt.setInt(3, aircraftId);
                stmt.setInt(4, scheduleId);
                stmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            showAlert(Alert.AlertType.INFORMATION, "Success", "Flight and schedule updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // TODO: Handle cancel action
    }

    @FXML
    private void showHome(javafx.event.ActionEvent event) {
        showHome();
    }

    @FXML
    private void showAddFlight(javafx.event.ActionEvent event) {
        loadScene("AddFlight.fxml");
    }

    @FXML
    private void showEditFlight(javafx.event.ActionEvent event) {
        loadScene("EditFlight.fxml");
    }

    @FXML
    private void showViewPassengers(ActionEvent event) {
        loadScene("ViewPassenger.fxml");
    }

    @FXML
    private void showAssignCrew(ActionEvent event) {
        loadScene("AddCrew.fxml");
    }

    @FXML
    private void initialize() {
        loadAircraftData();
        statusComboBox.setItems(FXCollections.observableArrayList("Scheduled", "Completed", "Delayed"));
        // TODO: Initialize ComboBox, set up listeners, etc.
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
    private void showHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            AdminDashboardController controller = loader.getController();
            controller.setAdminName(Session.adminName);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) flightId.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAddFlight() {
        loadScene("AddFlight.fxml");
    }

    public void showEditFlight() {
        loadScene("EditFlight.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private int getAircraftId(String displayText) {
        int start = displayText.indexOf("(");
        int end = displayText.indexOf(")");
        if (start == -1 || end == -1 || end <= start) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid aircraft format.");
            return -1;
        }
        String regNo = displayText.substring(start + 1, end);
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

    private void loadAircraftData() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT model, regNo FROM Aircraft";
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(sql)) {
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
} 