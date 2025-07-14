import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class MyBookingsController {
    @FXML private TableView<BookingDisplay> bookingsTable;
    @FXML private TableColumn<BookingDisplay, Integer> ticketIdColumn;
    @FXML private TableColumn<BookingDisplay, Integer> flightIdColumn;
    @FXML private TableColumn<BookingDisplay, String> fromColumn;
    @FXML private TableColumn<BookingDisplay, String> toColumn;
    @FXML private TableColumn<BookingDisplay, String> departureTimeColumn;
    @FXML private TableColumn<BookingDisplay, String> arrivalTimeColumn;
    @FXML private TableColumn<BookingDisplay, String> seatNoColumn;
    @FXML private TableColumn<BookingDisplay, String> bookingDateColumn;
    @FXML private TableColumn<BookingDisplay, String> statusColumn;
    @FXML private TableColumn<BookingDisplay, Void> actionColumn;
    @FXML private Button settingsButton;
    @FXML private Button signOutButton;

    private ObservableList<BookingDisplay> bookings = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        ticketIdColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("ticketID"));
        flightIdColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("flightID"));
        fromColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("from"));
        toColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("to"));
        departureTimeColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("departureTime"));
        arrivalTimeColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("arrivalTime"));
        seatNoColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("seatNo"));
        bookingDateColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("bookingDate"));
        statusColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        bookingsTable.setItems(bookings);
        actionColumn.setCellFactory(col -> new TableCell<BookingDisplay, Void>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.getStyleClass().add("primary-btn");
                deleteBtn.setStyle("-fx-background-color: #ff5c5c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 16;");
                deleteBtn.setOnAction(event -> {
                    BookingDisplay booking = getTableView().getItems().get(getIndex());
                    handleDeleteBooking(booking);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        loadBookings();
    }

    private void loadBookings() {
        bookings.clear();
        String userId = Session.getUserId();
        if (userId == null) return;
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT t.ticketID, t.flightID, f.departureAirport, f.arrivalAirport, f.departureTime, f.arrivalTime, t.seatNo, t.dates, f.status " +
                           "FROM Tickets t " +
                           "JOIN Flights f ON t.flightID = f.flightID " +
                           "WHERE t.passengerID = ? " +
                           "ORDER BY t.dates DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, userId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    java.sql.Time depTime = rs.getTime("departureTime");
                    java.sql.Time arrTime = rs.getTime("arrivalTime");
                    String depTimeStr = depTime != null ? depTime.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "";
                    String arrTimeStr = arrTime != null ? arrTime.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "";
                    BookingDisplay booking = new BookingDisplay(
                        rs.getInt("ticketID"),
                        rs.getInt("flightID"),
                        rs.getString("departureAirport"),
                        rs.getString("arrivalAirport"),
                        depTimeStr,
                        arrTimeStr,
                        rs.getString("seatNo"),
                        rs.getString("dates"),
                        rs.getString("status")
                    );
                    bookings.add(booking);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteBooking(BookingDisplay booking) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "EXEC DeleteTicket ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, booking.getTicketID());
                int result = pstmt.executeUpdate();
                if (result > 0) {
                    showAlert("Success", "Booking deleted successfully!", Alert.AlertType.INFORMATION);
                    loadBookings();
                } else {
                    showAlert("Error", "Failed to delete booking", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to delete booking: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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
            Stage stage = (Stage) bookingsTable.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 