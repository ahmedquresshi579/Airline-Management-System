import java.sql.Date;

public class Payment {
    private int paymentID;
    private int ticketID;
    private int amount;
    private String method;
    private Date dates;
    private String status;

    public Payment(int paymentID, int ticketID, int amount, String method, Date dates, String status) {
        this.paymentID = paymentID;
        this.ticketID = ticketID;
        this.amount = amount;
        this.method = method;
        this.dates = dates;
        this.status = status;
    }

    public int getPaymentID() { return paymentID; }
    public int getTicketID() { return ticketID; }
    public int getAmount() { return amount; }
    public String getMethod() { return method; }
    public Date getDates() { return dates; }
    public String getStatus() { return status; }

    public void setPaymentID(int paymentID) { this.paymentID = paymentID; }
    public void setTicketID(int ticketID) { this.ticketID = ticketID; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setMethod(String method) { this.method = method; }
    public void setDates(Date dates) { this.dates = dates; }
    public void setStatus(String status) { this.status = status; }
}
