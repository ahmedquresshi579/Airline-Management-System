import java.sql.Time;

public class Flight {
    private int flightID;
    private String departureAirport;
    private String arrivalAirport;
    private Time departureTime;
    private Time arrivalTime;
    private int scheduleID;
    private String status;

    public Flight(int flightID, String departureAirport, String arrivalAirport, Time departureTime, Time arrivalTime, int scheduleID, String status) {
        this.flightID = flightID;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.scheduleID = scheduleID;
        this.status = status;
    }

    public int getFlightID() { return flightID; }
    public String getDepartureAirport() { return departureAirport; }
    public String getArrivalAirport() { return arrivalAirport; }
    public Time getDepartureTime() { return departureTime; }
    public Time getArrivalTime() { return arrivalTime; }
    public int getScheduleID() { return scheduleID; }
    public String getStatus() { return status; }

    public void setFlightID(int flightID) { this.flightID = flightID; }
    public void setDepartureAirport(String departureAirport) { this.departureAirport = departureAirport; }
    public void setArrivalAirport(String arrivalAirport) { this.arrivalAirport = arrivalAirport; }
    public void setDepartureTime(Time departureTime) { this.departureTime = departureTime; }
    public void setArrivalTime(Time arrivalTime) { this.arrivalTime = arrivalTime; }
    public void setScheduleID(int scheduleID) { this.scheduleID = scheduleID; }
    public void setStatus(String status) { this.status = status; }
}
