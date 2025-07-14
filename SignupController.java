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
import javafx.scene.control.ComboBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import javafx.scene.control.Alert;

public class SignupController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private ImageView logoImageView;
    @FXML private ComboBox<String> roleComboBox;

    @FXML
    private void handleSignup(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            errorLabel.setText("All fields are required, including role.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errorLabel.setText("Invalid email format.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            // Check if email already exists in Admin table
            String checkAdminSql = "SELECT * FROM Admin WHERE email = ?";
            try (PreparedStatement checkAdminStmt = conn.prepareStatement(checkAdminSql)) {
                checkAdminStmt.setString(1, email);
                ResultSet rs = checkAdminStmt.executeQuery();
                if (rs.next()) {
                    errorLabel.setText("Email already registered as admin.");
                    return;
                }
            }
            
            // Check if email already exists in Passengers table
            String checkPassengerSql = "SELECT * FROM Passengers WHERE email = ?";
            try (PreparedStatement checkPassengerStmt = conn.prepareStatement(checkPassengerSql)) {
                checkPassengerStmt.setString(1, email);
                ResultSet rs = checkPassengerStmt.executeQuery();
                if (rs.next()) {
                    errorLabel.setText("Email already registered as passenger.");
                    return;
                }
            }

            // Insert into Users
            String userSql = "INSERT INTO Users (roles) VALUES (?)";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, role);
                userStmt.executeUpdate();
                
                // Get the auto-generated userID
                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userID = generatedKeys.getInt(1);
                        
                        if (role.equals("Admin")) {
                            String[] names = name.split(" ", 2);
                            String fname = names[0];
                            String lname = names.length > 1 ? names[1] : "";
                            String insertAdmin = "INSERT INTO Admin (userID, fname, lname, email, passwords) VALUES (?, ?, ?, ?, ?)";
                            try (PreparedStatement adminStmt = conn.prepareStatement(insertAdmin)) {
                                adminStmt.setInt(1, userID);
                                adminStmt.setString(2, fname);
                                adminStmt.setString(3, lname);
                                adminStmt.setString(4, email);
                                adminStmt.setString(5, password);
                                adminStmt.executeUpdate();
                            }
                        } else if (role.equals("Passenger")) {
                            String[] names = name.split(" ", 2);
                            String fname = names[0];
                            String lname = names.length > 1 ? names[1] : "";
                            String insertPassenger = "INSERT INTO Passengers (userID, fname, lname, email, passportNo, contactNo, passwords) VALUES (?, ?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement passStmt = conn.prepareStatement(insertPassenger)) {
                                passStmt.setInt(1, userID);
                                passStmt.setString(2, fname);
                                passStmt.setString(3, lname);
                                passStmt.setString(4, email);
                                passStmt.setString(5, ""); // passportNo placeholder
                                passStmt.setString(6, ""); // contactNo placeholder
                                passStmt.setString(7, password);
                                passStmt.executeUpdate();
                            }
                        }
                        errorLabel.setText("Sign up successful! Please sign in.");
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            }
        } catch (Exception e) {
            errorLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoginRedirect(ActionEvent event) throws IOException {
        Stage stage = (Stage) nameField.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }
} 