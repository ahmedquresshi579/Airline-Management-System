import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class AddCrewController {
    @FXML private TextField fnameField;
    @FXML private TextField lnameField;
    @FXML private ComboBox<String> designationComboBox;
    @FXML private TextField contactNoField;
    @FXML private TextField globalSearchField;
    @FXML private Button settingsButton;
    @FXML private TextField crewIdField;

    @FXML
    public void initialize() {
        // Initialize designation options
        List<String> designations = Arrays.asList("Pilot", "Co-Pilot", "Air Hostess");
        designationComboBox.getItems().addAll(designations);
    }

    @FXML
    private void handleAddCrew() {
        String crewIdText = crewIdField.getText().trim();
        String fname = fnameField.getText().trim();
        String lname = lnameField.getText().trim();
        String designation = designationComboBox.getValue();
        String contactNo = contactNoField.getText().trim();

        // Validate inputs
        if (crewIdText.isEmpty() || fname.isEmpty() || lname.isEmpty() || designation == null || contactNo.isEmpty()) {
            showAlert("Error", "Please fill in all fields", Alert.AlertType.ERROR);
            return;
        }
        int crewId;
        try {
            crewId = Integer.parseInt(crewIdText);
        } catch (NumberFormatException e) {
            showAlert("Error", "Crew ID must be a number", Alert.AlertType.ERROR);
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            String sql = "INSERT INTO Crew (crewID, fname, lname, designation, contactNo) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, crewId);
            pstmt.setString(2, fname);
            pstmt.setString(3, lname);
            pstmt.setString(4, designation);
            pstmt.setString(5, contactNo);
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
            showAlert("Success", "Crew member added successfully!", Alert.AlertType.INFORMATION);
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add crew member: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void clearForm() {
        crewIdField.clear();
        fnameField.clear();
        lnameField.clear();
        designationComboBox.setValue(null);
        contactNoField.clear();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        loadScene("AdminDashboard.fxml");
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
            Stage stage = (Stage) fnameField.getScene().getWindow();
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
    private void showAddCrew() {
        loadScene("AddCrew.fxml");
    }

    @FXML
    private void showAssignCrew() {
        loadScene("AssignCrew.fxml");
    }

    @FXML
    private void loadScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            Stage stage = (Stage) fnameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 