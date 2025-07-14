import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookFlightController {
    @FXML private ComboBox<String> fromAirportCombo;
    @FXML private ComboBox<String> toAirportCombo;
    @FXML private DatePicker flightDatePicker;
    @FXML private TableView<FlightDisplay> flightsTable;
    @FXML private TableColumn<FlightDisplay, Integer> flightIdColumn;
    @FXML private TableColumn<FlightDisplay, String> departureColumn;
    @FXML private TableColumn<FlightDisplay, String> arrivalColumn;
    @FXML private TableColumn<FlightDisplay, Time> timeColumn;
    @FXML private TableColumn<FlightDisplay, String> modelColumn;
    @FXML private TableColumn<FlightDisplay, Integer> capacityColumn;
    @FXML private TableColumn<FlightDisplay, Double> priceColumn;
    @FXML private TableColumn<FlightDisplay, Void> actionColumn;
    @FXML private Button settingsButton;
    @FXML private Button signOutButton;

    private ObservableList<FlightDisplay> flights = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        flightDatePicker.setValue(LocalDate.now());
        loadAirports();
        flightIdColumn.setCellValueFactory(new PropertyValueFactory<>("flightID"));
        departureColumn.setCellValueFactory(new PropertyValueFactory<>("departureAirport"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalAirport"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        flightsTable.setItems(flights);

        // Add Book Flight button to each row
        actionColumn.setCellFactory(col -> new TableCell<FlightDisplay, Void>() {
            private final Button bookBtn = new Button("Book Flight");
            {
                bookBtn.getStyleClass().add("primary-btn");
                bookBtn.setOnAction(event -> {
                    FlightDisplay flight = getTableView().getItems().get(getIndex());
                    handleBookFlight(flight);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(bookBtn);
                }
            }
        });
    }

    private void loadAirports() {
        try (Connection conn = DBConnection.getConnection()) {
            String departureQuery = "SELECT DISTINCT departureAirport as airport_code, departureAirport as airport_name FROM Flights ORDER BY departureAirport";
            try (PreparedStatement pstmt = conn.prepareStatement(departureQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                List<String> departureAirports = new ArrayList<>();
                while (rs.next()) {
                    String code = rs.getString("airport_code");
                    String name = rs.getString("airport_name");
                    departureAirports.add(code + " - " + name);
                }
                fromAirportCombo.setItems(FXCollections.observableArrayList(departureAirports));
            }
            String arrivalQuery = "SELECT DISTINCT arrivalAirport as airport_code, arrivalAirport as airport_name FROM Flights ORDER BY arrivalAirport";
            try (PreparedStatement pstmt = conn.prepareStatement(arrivalQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                List<String> arrivalAirports = new ArrayList<>();
                while (rs.next()) {
                    String code = rs.getString("airport_code");
                    String name = rs.getString("airport_name");
                    arrivalAirports.add(code + " - " + name);
                }
                toAirportCombo.setItems(FXCollections.observableArrayList(arrivalAirports));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load airports", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSearch() {
        String fromAirport = fromAirportCombo.getValue();
        String toAirport = toAirportCombo.getValue();
        LocalDate date = flightDatePicker.getValue();
        if (fromAirport == null || toAirport == null || date == null) {
            showAlert("Error", "Please fill in all fields", Alert.AlertType.ERROR);
            return;
        }
        String fromCode = fromAirport.split(" - ")[0];
        String toCode = toAirport.split(" - ")[0];
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT f.flightID, f.departureAirport, f.arrivalAirport, f.departureTime, f.arrivalTime, a.model, s.availableSeats, f.status, f.scheduleID " +
                          "FROM Flights f " +
                          "JOIN Schedule s ON f.scheduleID = s.scheduleID " +
                          "JOIN Aircraft a ON s.aircraftID = a.aircraftID " +
                          "WHERE f.departureAirport = ? " +
                          "AND f.arrivalAirport = ? " +
                          "AND CAST(s.dates AS DATE) = ? " +
                          "AND f.status <> 'Completed' " +
                          "ORDER BY f.departureTime";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, fromCode);
                pstmt.setString(2, toCode);
                pstmt.setString(3, formattedDate);
                ResultSet rs = pstmt.executeQuery();
                flights.clear();
                while (rs.next()) {
                    FlightDisplay flight = new FlightDisplay(
                        rs.getInt("flightID"),
                        rs.getString("departureAirport"),
                        rs.getString("arrivalAirport"),
                        rs.getTime("departureTime"),
                        rs.getTime("arrivalTime"),
                        rs.getString("model"),
                        rs.getInt("availableSeats"),
                        "$50",
                        rs.getString("status"),
                        rs.getInt("scheduleID")
                    );
                    flights.add(flight);
                }
                if (flights.isEmpty()) {
                    showAlert("Information", "No flights found for the selected criteria", Alert.AlertType.INFORMATION);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to search flights: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean isProfileComplete(String userId) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT fname, lname, email, passportNo, contactNo, creditCard FROM Passengers WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    for (String field : new String[]{
                            rs.getString("fname"),
                            rs.getString("lname"),
                            rs.getString("email"),
                            rs.getString("passportNo"),
                            rs.getString("contactNo"),
                            rs.getString("creditCard")
                    }) {
                        if (field == null || field.trim().isEmpty()) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void handleBookFlight(FlightDisplay flight) {
        String passengerId = Session.getUserId();
        if (passengerId == null) {
            showAlert("Error", "Please log in to book a flight", Alert.AlertType.ERROR);
            return;
        }
        if (!isProfileComplete(passengerId)) {
            showAlert("Error", "Please complete your profile before booking a flight.", Alert.AlertType.ERROR);
            return;
        }
        try {
            try (Connection conn = DBConnection.getConnection()) {
                // 1. Get all booked seats for this flight
                String seatQuery = "SELECT seatNo FROM Tickets WHERE flightID = ?";
                PreparedStatement seatStmt = conn.prepareStatement(seatQuery);
                seatStmt.setInt(1, flight.getFlightID());
                ResultSet seatRs = seatStmt.executeQuery();
                java.util.Set<String> bookedSeats = new java.util.HashSet<>();
                while (seatRs.next()) {
                    bookedSeats.add(seatRs.getString("seatNo"));
                }
                seatRs.close();
                seatStmt.close();

                // 2. Generate all possible seats (A1, A2, ..., An)
                int capacity = flight.getCapacity();
                String assignedSeat = null;
                for (int i = 1; i <= capacity; i++) {
                    String seat = "A" + i;
                    if (!bookedSeats.contains(seat)) {
                        assignedSeat = seat;
                        break;
                    }
                }
                if (assignedSeat == null) {
                    showAlert("Error", "No available seats on this flight.", Alert.AlertType.ERROR);
                    return;
                }

                // 3. Insert ticket with assigned seat (normal INSERT)
                String query = "INSERT INTO Tickets (passengerID, flightID, seatNo, dates) VALUES (?, ?, ?, GETDATE())";
                try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, passengerId);
                    pstmt.setInt(2, flight.getFlightID());
                    pstmt.setString(3, assignedSeat);
                    int result = pstmt.executeUpdate();
                    if (result > 0) {
                        // Instead of getGeneratedKeys(), fetch the ticketID manually
                        String getTicketIdSql = "SELECT TOP 1 ticketID FROM Tickets WHERE passengerID = ? AND flightID = ? ORDER BY ticketID DESC";
                        PreparedStatement getIdStmt = conn.prepareStatement(getTicketIdSql);
                        getIdStmt.setString(1, passengerId);
                        getIdStmt.setInt(2, flight.getFlightID());
                        ResultSet idRs = getIdStmt.executeQuery();
                        if (idRs.next()) {
                            int ticketID = idRs.getInt("ticketID");
                            if (ticketID > 0) {
                                // Call AddPayment procedure
                                String addPaymentSql = "{ call AddPayment(?, ?, ?, ?, ?) }";
                                try (CallableStatement paymentStmt = conn.prepareCall(addPaymentSql)) {
                                    paymentStmt.setInt(1, ticketID);
                                    paymentStmt.setInt(2, 50); // or flight.getPrice() if dynamic
                                    paymentStmt.setString(3, "Credit Card");
                                    paymentStmt.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                                    paymentStmt.setString(5, "Completed");
                                    paymentStmt.execute();

                                    showAlert("Success", "Flight booked successfully! Seat: " + assignedSeat, Alert.AlertType.INFORMATION);
                                    handleSearch();
                                }
                            } else {
                                showAlert("Error", "Ticket booking failed (overbooked or invalid).", Alert.AlertType.ERROR);
                            }
                        } else {
                            showAlert("Error", "Ticket booking failed (no ticketID).", Alert.AlertType.ERROR);
                        }
                    } else {
                        showAlert("Error", "Failed to book flight", Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to book flight", Alert.AlertType.ERROR);
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
    private void showHome() {
        loadScene("PassengerDashboard.fxml");
    }

    @FXML
    private void showBookFlight() {
        loadScene("BookFlight.fxml");
    }

    @FXML
    private void showMyBookings() {
        loadScene("MyBookings.fxml");
    }

    @FXML
    private void showProfile() {
        loadScene("MyProfile.fxml");
    }

    @FXML
    private void handleSignOut() {
        Session.logout();
        loadScene("Login.fxml");
    }

    private void loadScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) flightsTable.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load " + fxml, Alert.AlertType.ERROR);
        }
    }
} 