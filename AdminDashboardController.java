import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

public class AdminDashboardController {
    @FXML private StackPane mainContent;
    @FXML private VBox homePane, addFlightPane, editFlightPane, viewPassengersPane, assignCrewPane;
    @FXML private Label adminNameLabel;
    @FXML private Label flightsLabel, passengersLabel, crewLabel, upcomingFlightsLabel, pendingApprovalsLabel;
    @FXML private Label recentFlightLabel, recentFlightLabel1, recentFlightLabel11;
    @FXML private Label latestFlightRouteLabel;
    @FXML private TextField searchField;
    @FXML private Button settingsButton;
    @FXML private Button signOutButton;
    @FXML private Label aircraftLabel;
    @FXML private Label recentPassengerIdLabel;
    @FXML private PieChart crewPieChart;

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

    private void setActivePane(VBox activePane) {
        homePane.setVisible(false);
        addFlightPane.setVisible(false);
        editFlightPane.setVisible(false);
        viewPassengersPane.setVisible(false);
        assignCrewPane.setVisible(false);
        activePane.setVisible(true);
    }

    public void setAdminName(String name) {
        adminNameLabel.setText("Hello, " + name + "!");
        loadDashboardData();
    }

    private void loadDashboardData() {
        try (Connection conn = DBConnection.getConnection()) {
            // Load flight count
            String flightSql = "SELECT COUNT(*) as count FROM Flights";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(flightSql)) {
                if (rs.next()) {
                    flightsLabel.setText("Flights: " + rs.getInt("count"));
                }
            }

            // Load passenger count
            String passengerSql = "SELECT COUNT(*) as count FROM Passengers";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(passengerSql)) {
                if (rs.next()) {
                    passengersLabel.setText("Passengers: " + rs.getInt("count"));
                }
            }

            // Load crew count
            String crewSql = "SELECT COUNT(*) as count FROM Crew";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(crewSql)) {
                if (rs.next()) {
                    crewLabel.setText("Crew Members: " + rs.getInt("count"));
                }
            }

            // Load upcoming flights
            String upcomingSql = "SELECT COUNT(*) as count FROM Flights WHERE status = 'Scheduled' or status = 'Booked'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(upcomingSql)) {
                if (rs.next()) {
                    upcomingFlightsLabel.setText("Upcoming Flights: " + rs.getInt("count"));
                }
            }

            // Load recent flight info (latest flight and pilot)
            String latestFlightSql = "SELECT TOP 1 f.flightID, f.departureAirport, f.arrivalAirport, c.fname as pilotName, c.lname as pilotLName " +
                    "FROM Flights f " +
                    "LEFT JOIN CrewFlightManagement cfm ON f.flightID = cfm.flightID " +
                    "LEFT JOIN Crew c ON cfm.crewID = c.crewID AND c.designation = 'Pilot' " +
                    "ORDER BY f.scheduleID DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(latestFlightSql)) {
                if (rs.next()) {
                    String departure = rs.getString("departureAirport");
                    String arrival = rs.getString("arrivalAirport");
                    String pilotName = rs.getString("pilotName");
                    String pilotLName = rs.getString("pilotLName");
                    String displayPilot = (pilotName == null || pilotName.isEmpty()) ? "N/A" : pilotName + (pilotLName != null ? (" " + pilotLName) : "");
                    if (latestFlightRouteLabel != null) latestFlightRouteLabel.setText((departure != null && arrival != null) ? (departure + " -> " + arrival) : "N/A");
                    recentFlightLabel.setText(displayPilot);
                } else {
                    if (latestFlightRouteLabel != null) latestFlightRouteLabel.setText("N/A");
                    recentFlightLabel.setText("N/A");
                }
            }

            // Load total income using stored procedure
            try (java.sql.CallableStatement stmt = conn.prepareCall("{ call GetTotalIncome }")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    recentFlightLabel11.setText("$" + rs.getInt("totalIncome"));
                } else {
                    recentFlightLabel11.setText("$0");
                }
            }

            // Load latest passenger info
            String latestPassengerSql = "SELECT TOP 1 userID, fname, lname FROM Passengers ORDER BY userID DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(latestPassengerSql)) {
                if (rs.next()) {
                    int userId = rs.getInt("userID");
                    String fname = rs.getString("fname");
                    String lname = rs.getString("lname");
                    recentFlightLabel1.setText(fname + " " + lname);
                    if (recentPassengerIdLabel != null) recentPassengerIdLabel.setText(String.valueOf(userId));
                } else {
                    recentFlightLabel1.setText("N/A");
                    if (recentPassengerIdLabel != null) recentPassengerIdLabel.setText("N/A");
                }
            }

            // Load aircraft count
            String aircraftSql = "SELECT COUNT(*) as count FROM Aircraft";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(aircraftSql)) {
                if (rs.next()) {
                    aircraftLabel.setText("Aircraft: " + rs.getInt("count"));
                }
            }

            // Load crew distribution
            loadCrewChart();

        } catch (Exception e) {
            e.printStackTrace();
            // Handle database errors appropriately
        }
    }

    private void loadCrewChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT designation, COUNT(*) as count FROM Crew GROUP BY designation";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    pieChartData.add(new PieChart.Data(rs.getString("designation"), rs.getInt("count")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        crewPieChart.setData(pieChartData);
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            // Implement search functionality
            // This could search across flights, passengers, crew, etc.
        }
    }

    @FXML
    private void handleSettings() {
        // Implement settings functionality
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
    public void initialize() {
        // Set up any additional initialization
        searchField.setOnAction(e -> handleSearch());
        settingsButton.setOnAction(e -> handleSettings());
        signOutButton.setOnAction(e -> handleSignOut());
        if (Session.adminName != null && !Session.adminName.isEmpty()) {
            setAdminName(Session.adminName);
        }
    }

    @FXML
    private void handleRecentFlightClick() {
        loadScene("TicketsAndFlights.fxml");
    }

    private void loadScene(String fxmlFile) {
        try {
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle scene loading error
        }
    }
} 