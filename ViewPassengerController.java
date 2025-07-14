import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.HBox;
import java.io.File;

public class ViewPassengerController {
    @FXML private TableView<Passenger> passengersTable;
    @FXML private TableColumn<Passenger, Integer> userIDColumn;
    @FXML private TableColumn<Passenger, String> fnameColumn;
    @FXML private TableColumn<Passenger, String> lnameColumn;
    @FXML private TableColumn<Passenger, String> emailColumn;
    @FXML private TableColumn<Passenger, String> passportNoColumn;
    @FXML private TableColumn<Passenger, String> contactNoColumn;
    @FXML private TableColumn<Passenger, String> actionsColumn;
    @FXML private TextField searchField;
    @FXML private TextField globalSearchField;
    @FXML private Button settingsButton;
    @FXML private Label pageInfoLabel;

    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadPassengers();
    }

    private void setupTableColumns() {
        userIDColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        fnameColumn.setCellValueFactory(new PropertyValueFactory<>("fname"));
        lnameColumn.setCellValueFactory(new PropertyValueFactory<>("lname"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        passportNoColumn.setCellValueFactory(new PropertyValueFactory<>("passportNo"));
        contactNoColumn.setCellValueFactory(new PropertyValueFactory<>("contactNo"));
        
        // Setup actions column with buttons
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(e -> {
                    Passenger passenger = getTableView().getItems().get(getIndex());
                    handleEditPassenger(passenger);
                });
                deleteButton.setOnAction(e -> {
                    Passenger passenger = getTableView().getItems().get(getIndex());
                    handleDeletePassenger(passenger);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void loadPassengers() {
        try (Connection conn = DBConnection.getConnection()) {
            String searchText = searchField.getText();
            StringBuilder query = new StringBuilder("SELECT * FROM Passengers");
            boolean hasSearch = !searchText.isEmpty();
            if (hasSearch) {
                query.append(" WHERE userID = ?");
            }
            query.append(" ORDER BY userID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
            PreparedStatement pstmt = conn.prepareStatement(query.toString());
            int paramIndex = 1;
            if (hasSearch) {
                pstmt.setInt(paramIndex++, Integer.parseInt(searchText));
            }
            pstmt.setInt(paramIndex++, (currentPage - 1) * itemsPerPage);
            pstmt.setInt(paramIndex, itemsPerPage);
            ResultSet rs = pstmt.executeQuery();
            List<Passenger> passengers = new ArrayList<>();
            while (rs.next()) {
                Passenger passenger = new Passenger();
                passenger.setUserID(rs.getInt("userID"));
                passenger.setFname(rs.getString("fname"));
                passenger.setLname(rs.getString("lname"));
                passenger.setEmail(rs.getString("email"));
                passenger.setPassportNo(rs.getString("passportNo"));
                passenger.setContactNo(rs.getString("contactNo"));
                passengers.add(passenger);
            }
            passengersTable.getItems().setAll(passengers);
            updatePagination();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load passengers", Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid numeric ID.", Alert.AlertType.ERROR);
        }
    }

    private void updatePagination() {
        try (Connection conn = DBConnection.getConnection()) {
            String searchText = searchField.getText();
            
            StringBuilder countQuery = new StringBuilder(
                "SELECT COUNT(*) FROM Passengers p " +
                "WHERE 1=1"
            );
            
            if (!searchText.isEmpty()) {
                countQuery.append(" AND (p.fname LIKE ? OR p.lname LIKE ? OR p.email LIKE ? OR p.passportNo LIKE ? OR p.contactNo LIKE ?)");
            }
            
            PreparedStatement pstmt = conn.prepareStatement(countQuery.toString());
            int paramIndex = 1;
            
            if (!searchText.isEmpty()) {
                String searchPattern = "%" + searchText + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int totalItems = rs.getInt(1);
                totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
                pageInfoLabel.setText(String.format("Page %d of %d", currentPage, totalPages));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        currentPage = 1;
        loadPassengers();
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadPassengers();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadPassengers();
        }
    }

    private void handleEditPassenger(Passenger passenger) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditPassenger.fxml"));
            Parent root = loader.load();
            EditPassengerController controller = loader.getController();
            controller.setPassengerId(passenger.getUserID());
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            Stage stage = (Stage) passengersTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load edit passenger form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDeletePassenger(Passenger passenger) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Passenger");
        alert.setContentText("Are you sure you want to delete this passenger?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Passengers WHERE userID = ?");
                    pstmt.setInt(1, passenger.getUserID());
                    pstmt.executeUpdate();
                    loadPassengers();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to delete passenger", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
            Stage stage = (Stage) passengersTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load scene: " + e.getMessage(), Alert.AlertType.ERROR);
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
            File fxml = new File(fxmlFile);
            FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
            Parent root = loader.load();
            Scene scene = new Scene(root);
            File css = new File("style.css");
            scene.getStylesheets().add(css.toURI().toURL().toExternalForm());
            Stage stage = (Stage) passengersTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load scene: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
} 