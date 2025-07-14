import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class TicketsAndFlightsController {
    @FXML private ComboBox<String> flightIdCombo;
    @FXML private ComboBox<String> passengerIdCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private TableView<FlightTicketDisplay> flightsTable;
    @FXML private TableColumn<FlightTicketDisplay, Integer> flightIdColumn;
    @FXML private TableColumn<FlightTicketDisplay, String> departureColumn;
    @FXML private TableColumn<FlightTicketDisplay, String> arrivalColumn;
    @FXML private TableColumn<FlightTicketDisplay, String> statusColumn;
    @FXML private TableColumn<FlightTicketDisplay, String> dateColumn;
    @FXML private TextField searchField;
    @FXML private Button settingsButton;
    @FXML private Button signOutButton;
    @FXML private TableColumn<FlightTicketDisplay, String> aircraftColumn;
    @FXML private TableColumn<FlightTicketDisplay, Integer> ticketQuantityColumn;

    private ObservableList<FlightTicketDisplay> flights = FXCollections.observableArrayList();

    @FXML
    private void showHome() {
        loadScene("AdminDashboard.fxml");
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
    private void handleSettings() {
        // Implement settings functionality
    }

    @FXML
    public void initialize() {
        loadComboBoxData();
        statusCombo.setItems(FXCollections.observableArrayList("Scheduled", "Booked", "Completed", "Delayed"));
        flightIdColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("flightID"));
        departureColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("departure"));
        arrivalColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("arrival"));
        statusColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("date"));
        aircraftColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("aircraft"));
        ticketQuantityColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("ticketQuantity"));
        flightsTable.setItems(flights);
        loadAllFlights();
        searchButton.setOnAction(e -> handleSearch());
        clearButton.setOnAction(e -> handleClear());
        searchField.setOnAction(e -> handleSearch());
        settingsButton.setOnAction(e -> handleSettings());
        signOutButton.setOnAction(e -> handleSignOut());
    }

    private void loadComboBoxData() {
        try (Connection conn = DBConnection.getConnection()) {
            // Flight IDs
            ObservableList<String> flightIds = FXCollections.observableArrayList();
            ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT flightID FROM Flights");
            while (rs.next()) flightIds.add(rs.getString("flightID"));
            flightIdCombo.setItems(flightIds);
            // Passenger IDs
            ObservableList<String> passengerIds = FXCollections.observableArrayList();
            rs = conn.createStatement().executeQuery("SELECT DISTINCT userID FROM Passengers");
            while (rs.next()) passengerIds.add(rs.getString("userID"));
            passengerIdCombo.setItems(passengerIds);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadAllFlights() {
        flights.clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT TOP 10 f.flightID, f.departureAirport, f.arrivalAirport, a.model as aircraft, f.status, s.dates, (SELECT COUNT(*) FROM Tickets t WHERE t.flightID = f.flightID) as ticketQuantity FROM Flights f LEFT JOIN Schedule s ON f.scheduleID = s.scheduleID LEFT JOIN Aircraft a ON s.aircraftID = a.aircraftID";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                flights.add(new FlightTicketDisplay(
                    rs.getInt("flightID"),
                    rs.getString("departureAirport"),
                    rs.getString("arrivalAirport"),
                    rs.getString("aircraft"),
                    rs.getInt("ticketQuantity"),
                    rs.getString("status"),
                    rs.getString("dates")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSearch() {
        flights.clear();
        String selectedFlightId = flightIdCombo.getValue();
        String selectedPassengerId = passengerIdCombo.getValue();
        String selectedStatus = statusCombo.getValue();
        boolean filterByPassenger = selectedPassengerId != null && !selectedPassengerId.isEmpty();
        StringBuilder sql = new StringBuilder();
        if (filterByPassenger) {
            sql.append("SELECT TOP 10 f.flightID, f.departureAirport, f.arrivalAirport, a.model as aircraft, f.status, s.dates, (SELECT COUNT(*) FROM Tickets t WHERE t.flightID = f.flightID AND t.passengerID = " + selectedPassengerId + ") as ticketQuantity FROM Flights f LEFT JOIN Schedule s ON f.scheduleID = s.scheduleID LEFT JOIN Aircraft a ON s.aircraftID = a.aircraftID WHERE 1=1");
        } else {
            sql.append("SELECT TOP 10 f.flightID, f.departureAirport, f.arrivalAirport, a.model as aircraft, f.status, s.dates FROM Flights f LEFT JOIN Schedule s ON f.scheduleID = s.scheduleID LEFT JOIN Aircraft a ON s.aircraftID = a.aircraftID WHERE 1=1");
        }
        if (selectedFlightId != null && !selectedFlightId.isEmpty()) sql.append(" AND f.flightID = " + selectedFlightId);
        if (filterByPassenger) sql.append(" AND EXISTS (SELECT 1 FROM Tickets t WHERE t.flightID = f.flightID AND t.passengerID = " + selectedPassengerId + ")");
        if (selectedStatus != null && !selectedStatus.isEmpty()) sql.append(" AND f.status = '" + selectedStatus + "'");
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(sql.toString());
            while (rs.next()) {
                flights.add(new FlightTicketDisplay(
                    rs.getInt("flightID"),
                    rs.getString("departureAirport"),
                    rs.getString("arrivalAirport"),
                    rs.getString("aircraft"),
                    filterByPassenger ? rs.getInt("ticketQuantity") : null,
                    rs.getString("status"),
                    rs.getString("dates")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleClear() {
        flightIdCombo.getSelectionModel().clearSelection();
        passengerIdCombo.getSelectionModel().clearSelection();
        statusCombo.getSelectionModel().clearSelection();
        loadAllFlights();
    }

    private void loadScene(String fxmlFile) {
        try {
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Data class for TableView
    public static class FlightTicketDisplay {
        private final Integer flightID;
        private final String departure;
        private final String arrival;
        private final String aircraft;
        private final Integer ticketQuantity;
        private final String status;
        private final String date;
        public FlightTicketDisplay(Integer flightID, String departure, String arrival, String aircraft, Integer ticketQuantity, String status, String date) {
            this.flightID = flightID;
            this.departure = departure;
            this.arrival = arrival;
            this.aircraft = aircraft;
            this.ticketQuantity = ticketQuantity;
            this.status = status;
            this.date = date;
        }
        public Integer getFlightID() { return flightID; }
        public String getDeparture() { return departure; }
        public String getArrival() { return arrival; }
        public String getAircraft() { return aircraft; }
        public Integer getTicketQuantity() { return ticketQuantity; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
    }
} 