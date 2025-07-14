import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PassengerDashboardController {
    @FXML private Label passengerNameLabel;
    @FXML private Label upcomingFlightLabel;
    @FXML private Label flightRouteLabel;
    @FXML private Label flightDateLabel;
    @FXML private TextField searchField;
    @FXML private Button settingsButton;
    @FXML private Button signOutButton;
    @FXML private TableView<Booking> recentBookingsTable;
    @FXML private TableColumn<Booking, String> flightIdColumn;
    @FXML private TableColumn<Booking, String> fromColumn;
    @FXML private TableColumn<Booking, String> toColumn;
    @FXML private TableColumn<Booking, String> dateColumn;
    @FXML private Label latestDestinationLabel;

    @FXML
    public void initialize() {
        // Set passenger name from session if available
        if (Session.passengerName != null && !Session.passengerName.isEmpty()) {
            passengerNameLabel.setText("Hello, " + Session.passengerName + "!");
        }
        // Initialize table columns
        flightIdColumn.setCellValueFactory(new PropertyValueFactory<>("flightId"));
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Set up event handlers
        searchField.setOnAction(e -> handleSearch());
        settingsButton.setOnAction(e -> handleSettings());
        signOutButton.setOnAction(e -> handleSignOut());

        // Load initial data
        loadDashboardData();
    }

    public void setPassengerName(String name) {
        passengerNameLabel.setText("Hello, " + name + "!");
        loadDashboardData();
    }

    private void loadDashboardData() {
        try (Connection conn = DBConnection.getConnection()) {
            // Load most recent booking for the first square
            String latestBookedSql = "SELECT TOP 1 f.departureAirport, f.arrivalAirport, s.dates " +
                "FROM Tickets t " +
                "JOIN Flights f ON t.flightID = f.flightID " +
                "JOIN Schedule s ON f.scheduleID = s.scheduleID " +
                "WHERE t.passengerID = ? AND f.status = 'Booked' " +
                "ORDER BY s.dates DESC, t.ticketID DESC";
            try (PreparedStatement stmt = conn.prepareStatement(latestBookedSql)) {
                stmt.setInt(1, Session.passengerID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String route = rs.getString("departureAirport") + " -> " + rs.getString("arrivalAirport");
                    flightRouteLabel.setText(route);
                    flightDateLabel.setText(rs.getString("dates"));
                    latestDestinationLabel.setText(rs.getString("arrivalAirport"));
                } else {
                    flightRouteLabel.setText("-");
                    flightDateLabel.setText("N/A");
                    latestDestinationLabel.setText("-");
                }
            }

            // Load recent bookings
            String recentSql = "SELECT TOP 5 t.*, f.status, s.dates as flightDate, f.departureAirport, f.arrivalAirport FROM Tickets t " +
                             "JOIN Flights f ON t.flightID = f.flightID " +
                             "JOIN Schedule s ON f.scheduleID = s.scheduleID " +
                             "WHERE t.passengerID = ? " +
                             "ORDER BY s.dates DESC";
            try (PreparedStatement stmt = conn.prepareStatement(recentSql)) {
                stmt.setInt(1, Session.passengerID);
                ResultSet rs = stmt.executeQuery();
                ObservableList<Booking> bookings = FXCollections.observableArrayList();
                boolean first = true;
                while (rs.next()) {
                    Booking booking = new Booking(
                        rs.getString("flightID"),
                        rs.getString("departureAirport"),
                        rs.getString("arrivalAirport"),
                        rs.getString("flightDate")
                    );
                    bookings.add(booking);
                    first = false;
                }
                if (bookings.isEmpty()) {
                    // Removed code for setting recent booking labels to default
                }
                recentBookingsTable.setItems(bookings);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dashboard data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            // Implement flight search functionality
            loadScene("SearchFlights.fxml");
        }
    }

    @FXML
    private void handleSettings() {
        // Implement settings functionality
        loadScene("PassengerSettings.fxml");
    }

    @FXML
    private void handleSignOut() {
        try {
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showHome() {
        loadScene("PassengerDashboard.fxml");
    }

    @FXML
    private void showMyBookings() {
        loadScene("MyBookings.fxml");
    }

    @FXML
    private void showBookFlight() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BookFlight.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) passengerNameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load BookFlight.fxml", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showProfile() {
        loadScene("MyProfile.fxml");
    }

    private void loadScene(String fxmlFile) {
        try {
            Stage stage = (Stage) passengerNameLabel.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load scene: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Booking class for table data
    public static class Booking {
        private final String flightId;
        private final String from;
        private final String to;
        private final String date;

        public Booking(String flightId, String from, String to, String date) {
            this.flightId = flightId;
            this.from = from;
            this.to = to;
            this.date = date;
        }

        public String getFlightId() { return flightId; }
        public String getFrom() { return from; }
        public String getTo() { return to; }
        public String getDate() { return date; }
    }
} 