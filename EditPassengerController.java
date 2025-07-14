import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditPassengerController {
    @FXML private TextField fnameField;
    @FXML private TextField lnameField;
    @FXML private TextField emailField;
    @FXML private TextField passportNoField;
    @FXML private TextField contactNoField;
    @FXML private TextField globalSearchField;
    @FXML private Button settingsButton;
    @FXML private Button profileButton;

    private int passengerId;

    public void setPassengerId(int id) {
        this.passengerId = id;
        loadPassengerData();
    }

    private void loadPassengerData() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Passengers WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, passengerId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    fnameField.setText(rs.getString("fname"));
                    lnameField.setText(rs.getString("lname"));
                    emailField.setText(rs.getString("email"));
                    passportNoField.setText(rs.getString("passportNo"));
                    contactNoField.setText(rs.getString("contactNo"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load passenger data: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE Passengers SET fname=?, lname=?, email=?, passportNo=?, contactNo=? WHERE userID=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, fnameField.getText().trim());
                pstmt.setString(2, lnameField.getText().trim());
                pstmt.setString(3, emailField.getText().trim());
                pstmt.setString(4, passportNoField.getText().trim());
                pstmt.setString(5, contactNoField.getText().trim());
                pstmt.setInt(6, passengerId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Passenger information updated successfully!");
                    showViewPassengers(event);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update passenger information.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update passenger: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (fnameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "First name is required!");
            return false;
        }
        if (lnameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Last name is required!");
            return false;
        }
        if (emailField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Email is required!");
            return false;
        }
        if (!emailField.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address!");
            return false;
        }
        return true;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        showViewPassengers(event);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Navigation methods
    @FXML
    private void showHome(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            AdminDashboardController controller = loader.getController();
            controller.setAdminName(Session.adminName);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) fnameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load scene: " + e.getMessage());
        }
    }

    @FXML
    private void showAddFlight(ActionEvent event) {
        loadScene("AddFlight.fxml");
    }

    @FXML
    private void showEditFlight(ActionEvent event) {
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

    private void loadScene(String fxmlFile) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlFile));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) fnameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load scene: " + e.getMessage());
        }
    }
} 