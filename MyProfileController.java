import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class MyProfileController {
    @FXML private TextField fnameField;
    @FXML private TextField lnameField;
    @FXML private TextField emailField;
    @FXML private TextField passportNoField;
    @FXML private TextField contactNoField;
    @FXML private TextField creditCardField;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button settingsButton;
    @FXML private Button signOutButton;

    private String originalFname;
    private String originalLname;
    private String originalEmail;
    private String originalPassportNo;
    private String originalContactNo;
    private String originalCreditCard;

    @FXML
    public void initialize() {
        loadProfile();
    }

    private void loadProfile() {
        String userId = Session.getUserId();
        if (userId == null) return;
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT fname, lname, email, passportNo, contactNo, creditCard FROM Passengers WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    originalFname = rs.getString("fname");
                    originalLname = rs.getString("lname");
                    originalEmail = rs.getString("email");
                    originalPassportNo = rs.getString("passportNo");
                    originalContactNo = rs.getString("contactNo");
                    originalCreditCard = rs.getString("creditCard");

                    fnameField.setText(originalFname);
                    lnameField.setText(originalLname);
                    emailField.setText(originalEmail);
                    passportNoField.setText(originalPassportNo);
                    contactNoField.setText(originalContactNo);
                    creditCardField.setText(originalCreditCard);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load profile: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEdit() {
        // Enable editing
        fnameField.setEditable(true);
        lnameField.setEditable(true);
        emailField.setEditable(true);
        passportNoField.setEditable(true);
        contactNoField.setEditable(true);
        creditCardField.setEditable(true);

        // Show save and cancel buttons
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
        editButton.setVisible(false);

        // Add borders to show editability
        fnameField.setStyle(fnameField.getStyle() + "; -fx-border-color: #6a4df4;");
        lnameField.setStyle(lnameField.getStyle() + "; -fx-border-color: #6a4df4;");
        emailField.setStyle(emailField.getStyle() + "; -fx-border-color: #6a4df4;");
        passportNoField.setStyle(passportNoField.getStyle() + "; -fx-border-color: #6a4df4;");
        contactNoField.setStyle(contactNoField.getStyle() + "; -fx-border-color: #6a4df4;");
        creditCardField.setStyle(creditCardField.getStyle() + "; -fx-border-color: #6a4df4;");
    }

    @FXML
    private void handleSave() {
        String userId = Session.getUserId();
        if (userId == null) return;

        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE Passengers SET fname = ?, lname = ?, email = ?, passportNo = ?, contactNo = ?, creditCard = ? WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, fnameField.getText());
                pstmt.setString(2, lnameField.getText());
                pstmt.setString(3, emailField.getText());
                pstmt.setString(4, passportNoField.getText());
                pstmt.setString(5, contactNoField.getText());
                pstmt.setString(6, creditCardField.getText());
                pstmt.setString(7, userId);

                int result = pstmt.executeUpdate();
                if (result > 0) {
                    showAlert("Success", "Profile updated successfully!", Alert.AlertType.INFORMATION);
                    resetEditMode();
                    loadProfile(); // Reload to confirm changes
                } else {
                    showAlert("Error", "Failed to update profile", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update profile: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        // Restore original values
        fnameField.setText(originalFname);
        lnameField.setText(originalLname);
        emailField.setText(originalEmail);
        passportNoField.setText(originalPassportNo);
        contactNoField.setText(originalContactNo);
        creditCardField.setText(originalCreditCard);

        resetEditMode();
    }

    private void resetEditMode() {
        // Disable editing
        fnameField.setEditable(false);
        lnameField.setEditable(false);
        emailField.setEditable(false);
        passportNoField.setEditable(false);
        contactNoField.setEditable(false);
        creditCardField.setEditable(false);

        // Hide save and cancel buttons
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        editButton.setVisible(true);

        // Remove borders
        fnameField.setStyle(fnameField.getStyle().replace("; -fx-border-color: #6a4df4;", ""));
        lnameField.setStyle(lnameField.getStyle().replace("; -fx-border-color: #6a4df4;", ""));
        emailField.setStyle(emailField.getStyle().replace("; -fx-border-color: #6a4df4;", ""));
        passportNoField.setStyle(passportNoField.getStyle().replace("; -fx-border-color: #6a4df4;", ""));
        contactNoField.setStyle(contactNoField.getStyle().replace("; -fx-border-color: #6a4df4;", ""));
        creditCardField.setStyle(creditCardField.getStyle().replace("; -fx-border-color: #6a4df4;", ""));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void showHome() { loadScene("PassengerDashboard.fxml"); }
    @FXML
    private void showBookFlight() { loadScene("BookFlight.fxml"); }
    @FXML
    private void showMyBookings() { loadScene("MyBookings.fxml"); }
    @FXML
    private void showProfile() { loadScene("MyProfile.fxml"); }
    @FXML
    private void handleSignOut() { Session.logout(); loadScene("Login.fxml"); }

    private void loadScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) fnameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 