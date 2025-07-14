import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private ImageView logoImageView;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter email and password.");
            return;
        }
        errorLabel.setText("");
        try (Connection conn = DBConnection.getConnection()) {
            // Try Admin table first
            String adminSql = "SELECT * FROM Admin WHERE email = ? AND passwords = ?";
            try (PreparedStatement adminStmt = conn.prepareStatement(adminSql)) {
                adminStmt.setString(1, email);
                adminStmt.setString(2, password);
                ResultSet rs = adminStmt.executeQuery();
                if (rs.next()) {
                    errorLabel.setText("Admin login successful!");
                    Session.adminUserID = rs.getInt("userID");
                    Session.adminName = rs.getString("fname");
                    try {
                        Stage stage = (Stage) emailField.getScene().getWindow();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
                        Parent root = loader.load();
                        AdminDashboardController controller = loader.getController();
                        controller.setAdminName(Session.adminName);
                        Scene scene = new Scene(root);
                        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
                        stage.setScene(scene);
                    } catch (Exception e) {
                        errorLabel.setText("Failed to load admin dashboard: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return;
                }
            }
            // Try Passengers table
            String passSql = "SELECT * FROM Passengers WHERE email = ? AND passwords = ?";
            try (PreparedStatement passStmt = conn.prepareStatement(passSql)) {
                passStmt.setString(1, email);
                passStmt.setString(2, password);
                ResultSet rs = passStmt.executeQuery();
                if (rs.next()) {
                    errorLabel.setText("Passenger login successful!");
                    Session.passengerID = rs.getInt("userID");
                    Session.passengerName = rs.getString("fname");
                    try {
                        Stage stage = (Stage) emailField.getScene().getWindow();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("PassengerDashboard.fxml"));
                        Parent root = loader.load();
                        PassengerDashboardController controller = loader.getController();
                        controller.setPassengerName(Session.passengerName);
                        Scene scene = new Scene(root);
                        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
                        stage.setScene(scene);
                    } catch (Exception e) {
                        errorLabel.setText("Failed to load passenger dashboard: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return;
                }
            }
            errorLabel.setText("Invalid credentials.");
        } catch (Exception e) {
            errorLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignupRedirect(ActionEvent event) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("signup.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            errorLabel.setText("Failed to load signup page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/resources/logo.png"));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            System.out.println("Logo image not found: " + e.getMessage());
        }
    }
} 