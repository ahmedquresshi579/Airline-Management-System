import java.sql.Date;

public class Schedule {
    private int scheduleID;
    private int adminID;
    private int aircraftID;
    private Date dates;
    private String gate;

    public Schedule(int scheduleID, int adminID, int aircraftID, Date dates, String gate) {
        this.scheduleID = scheduleID;
        this.adminID = adminID;
        this.aircraftID = aircraftID;
        this.dates = dates;
        this.gate = gate;
    }

    public int getScheduleID() { return scheduleID; }
    public int getAdminID() { return adminID; }
    public int getAircraftID() { return aircraftID; }
    public Date getDates() { return dates; }
    public String getGate() { return gate; }

    public void setScheduleID(int scheduleID) { this.scheduleID = scheduleID; }
    public void setAdminID(int adminID) { this.adminID = adminID; }
    public void setAircraftID(int aircraftID) { this.aircraftID = aircraftID; }
    public void setDates(Date dates) { this.dates = dates; }
    public void setGate(String gate) { this.gate = gate; }
}
