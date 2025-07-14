import java.sql.Date;

public class Ticket {
    private int ticketID;
    private int passengerID;
    private int flightID;
    private String seatNo;
    private Date dates;

    public Ticket(int ticketID, int passengerID, int flightID, String seatNo, Date dates) {
        this.ticketID = ticketID;
        this.passengerID = passengerID;
        this.flightID = flightID;
        this.seatNo = seatNo;
        this.dates = dates;
    }

    public int getTicketID() { return ticketID; }
    public int getPassengerID() { return passengerID; }
    public int getFlightID() { return flightID; }
    public String getSeatNo() { return seatNo; }
    public Date getDates() { return dates; }

    public void setTicketID(int ticketID) { this.ticketID = ticketID; }
    public void setPassengerID(int passengerID) { this.passengerID = passengerID; }
    public void setFlightID(int flightID) { this.flightID = flightID; }
    public void setSeatNo(String seatNo) { this.seatNo = seatNo; }
    public void setDates(Date dates) { this.dates = dates; }
}
