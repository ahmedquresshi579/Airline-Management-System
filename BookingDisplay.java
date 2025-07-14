public class BookingDisplay {
    private int ticketID;
    private int flightID;
    private String from;
    private String to;
    private String departureTime;
    private String arrivalTime;
    private String seatNo;
    private String bookingDate;
    private String status;

    public BookingDisplay(int ticketID, int flightID, String from, String to, String departureTime, String arrivalTime, String seatNo, String bookingDate, String status) {
        this.ticketID = ticketID;
        this.flightID = flightID;
        this.from = from;
        this.to = to;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.seatNo = seatNo;
        this.bookingDate = bookingDate;
        this.status = status;
    }

    public int getTicketID() { return ticketID; }
    public int getFlightID() { return flightID; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getSeatNo() { return seatNo; }
    public String getBookingDate() { return bookingDate; }
    public String getStatus() { return status; }
} 