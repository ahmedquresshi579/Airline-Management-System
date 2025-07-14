import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.layout.HBox;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;

public class AssignCrewController {
    @FXML private TextField flightIdSearchField;
    @FXML private Label flightDetailsLabel;
    @FXML private HBox flightDetailsBox;
    @FXML private TextField crewIdSearchField;
    @FXML private Label crewDetailsLabel;
    @FXML private HBox crewDetailsBox;
    @FXML private TableView<CrewFlightAssignment> assignedCrewTable;
    @FXML private TableColumn<CrewFlightAssignment, Integer> assignmentIdColumn;
    @FXML private TableColumn<CrewFlightAssignment, Integer> crewIdColumn;
    @FXML private TableColumn<CrewFlightAssignment, String> nameColumn;
    @FXML private TableColumn<CrewFlightAssignment, String> designationColumn;
    @FXML private TextField globalSearchField;
    @FXML private Button settingsButton;

    private ObservableList<CrewFlightAssignment> assignedCrewList = FXCollections.observableArrayList();
    private Integer selectedFlightId = null;
    private Integer selectedCrewId = null;

    @FXML
    public void initialize() {
        assignmentIdColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getAssignmentID()));
        crewIdColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCrewID()));
        nameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
            cellData.getValue().getCrew().getFname() + " " + cellData.getValue().getCrew().getLname()
        ));
        designationColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
            cellData.getValue().getCrew().getDesignation()
        ));
        assignedCrewTable.setItems(assignedCrewList);
        flightDetailsBox.setVisible(false);
        crewDetailsBox.setVisible(false);
    }

    @FXML
    private void handleFlightSearch() {
        String flightIdStr = flightIdSearchField.getText().trim();
        if (flightIdStr.isEmpty()) {
            showAlert("Error", "Please enter a Flight ID", Alert.AlertType.ERROR);
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT f.flightID, f.departureAirport, f.arrivalAirport, s.dates AS flightDate, f.status, a.regNo " +
                         "FROM Flights f " +
                         "JOIN Schedule s ON f.scheduleID = s.scheduleID " +
                         "JOIN Aircraft a ON s.aircraftID = a.aircraftID " +
                         "WHERE f.flightID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(flightIdStr));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                selectedFlightId = rs.getInt("flightID");
                String details = String.format("Flight: %d | %s → %s | Date: %s | Aircraft: %s | Status: %s",
                        rs.getInt("flightID"), rs.getString("departureAirport"), rs.getString("arrivalAirport"),
                        rs.getString("flightDate"), rs.getString("regNo"), rs.getString("status"));
                flightDetailsLabel.setText(details);
                flightDetailsBox.setVisible(true);
                loadAssignedCrew(selectedFlightId);
            } else {
                selectedFlightId = null;
                flightDetailsLabel.setText("No flight found.");
                flightDetailsBox.setVisible(true);
                assignedCrewList.clear();
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to search flight: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCrewSearch() {
        String crewIdStr = crewIdSearchField.getText().trim();
        if (crewIdStr.isEmpty()) {
            showAlert("Error", "Please enter a Crew ID", Alert.AlertType.ERROR);
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT crewID, fname, lname, designation, contactNo FROM Crew WHERE crewID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(crewIdStr));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                selectedCrewId = rs.getInt("crewID");
                String details = String.format("Crew: %d | %s %s | %s | Contact: %s",
                        rs.getInt("crewID"), rs.getString("fname"), rs.getString("lname"),
                        rs.getString("designation"), rs.getString("contactNo"));
                crewDetailsLabel.setText(details);
                crewDetailsBox.setVisible(true);
            } else {
                selectedCrewId = null;
                crewDetailsLabel.setText("No crew found.");
                crewDetailsBox.setVisible(true);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to search crew: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAssign() {
        Integer flightId = selectedFlightId;
        Integer crewId = selectedCrewId;
        if (flightId == null || crewId == null) {
            showAlert("Error", "Please search and select a valid flight and crew.", Alert.AlertType.ERROR);
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            // Get the designation of the crew being assigned
            String designation = null;
            String crewSql = "SELECT designation FROM Crew WHERE crewID = ?";
            try (PreparedStatement crewStmt = conn.prepareStatement(crewSql)) {
                crewStmt.setInt(1, crewId);
                try (ResultSet crewRs = crewStmt.executeQuery()) {
                    if (crewRs.next()) {
                        designation = crewRs.getString("designation");
                    }
                }
            }
            if (designation == null) {
                showAlert("Error", "Crew not found.", Alert.AlertType.ERROR);
                return;
            }
            // Only check for Pilot and Co-Pilot
            if (designation.equalsIgnoreCase("Pilot") || designation.equalsIgnoreCase("Co-Pilot")) {
                String checkSql = "SELECT COUNT(*) AS count FROM CrewFlightManagement cfm JOIN Crew c ON cfm.crewID = c.crewID WHERE cfm.flightID = ? AND c.designation = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, flightId);
                    checkStmt.setString(2, designation);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt("count") >= 1) {
                            showAlert("Error", "Cannot assign more than 1 " + designation + " to this flight.", Alert.AlertType.ERROR);
                            return;
                        }
                    }
                }
            }
            // Check if already assigned
            String checkSql = "SELECT * FROM CrewFlightManagement WHERE flightID = ? AND crewID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, flightId);
            checkStmt.setInt(2, crewId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                showAlert("Info", "Crew is already assigned to this flight.", Alert.AlertType.INFORMATION);
            } else {
                String insertSql = "INSERT INTO CrewFlightManagement (flightID, crewID) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, flightId);
                insertStmt.setInt(2, crewId);
                insertStmt.executeUpdate();
                insertStmt.close();
                showAlert("Success", "Crew assigned successfully!", Alert.AlertType.INFORMATION);
                loadAssignedCrew(flightId);
            }
            rs.close();
            checkStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to assign crew: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAssignedCrew(int flightId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT cfm.assignmentID, cfm.crewID, cfm.flightID, c.fname, c.lname, c.designation, c.contactNo " +
                         "FROM CrewFlightManagement cfm JOIN Crew c ON cfm.crewID = c.crewID WHERE cfm.flightID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flightId);
            ResultSet rs = pstmt.executeQuery();
            assignedCrewList.clear();
            while (rs.next()) {
                Crew crew = new Crew(
                    rs.getInt("crewID"),
                    rs.getString("fname"),
                    rs.getString("lname"),
                    rs.getString("designation"),
                    rs.getString("contactNo")
                );
                CrewFlightAssignment assignment = new CrewFlightAssignment(
                    rs.getInt("assignmentID"),
                    rs.getInt("crewID"),
                    rs.getInt("flightID"),
                    crew
                );
                assignedCrewList.add(assignment);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load assigned crew: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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
            Stage stage = (Stage) flightIdSearchField.getScene().getWindow();
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

    private void loadScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            Stage stage = (Stage) flightIdSearchField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class CrewAssignment {
    private int crewId;
    private String name;
    private String designation;
    private String actions;
    public CrewAssignment(int crewId, String name, String designation, String actions) {
        this.crewId = crewId;
        this.name = name;
        this.designation = designation;
        this.actions = actions;
    }
    public int getCrewId() { return crewId; }
    public String getName() { return name; }
    public String getDesignation() { return designation; }
    public String getActions() { return actions; }
} 