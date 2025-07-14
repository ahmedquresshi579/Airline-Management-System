import java.sql.Time;

public class FlightDisplay {
    private int flightID;
    private String departureAirport;
    private String arrivalAirport;
    private Time departureTime;
    private Time arrivalTime;
    private String model;
    private int capacity;
    private String price;
    private String status;
    private int scheduleID;

    public FlightDisplay(int flightID, String departureAirport, String arrivalAirport, Time departureTime, Time arrivalTime, String model, int capacity, String price, String status, int scheduleID) {
        this.flightID = flightID;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.model = model;
        this.capacity = capacity;
        this.price = price;
        this.status = status;
        this.scheduleID = scheduleID;
    }

    public int getFlightID() { return flightID; }
    public String getDepartureAirport() { return departureAirport; }
    public String getArrivalAirport() { return arrivalAirport; }
    public Time getDepartureTime() { return departureTime; }
    public Time getArrivalTime() { return arrivalTime; }
    public String getModel() { return model; }
    public int getCapacity() { return capacity; }
    public String getPrice() { return price; }
    public String getStatus() { return status; }
    public int getScheduleID() { return scheduleID; }
} 