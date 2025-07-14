public class CrewFlightAssignment 
{
    private int assignmentID;
    private int crewID;
    private int flightID;
    private Crew crew;

     public CrewFlightAssignment(int assignmentID, int crewID, int flightID, Crew crew) {
        this.assignmentID = assignmentID;
        this.crewID = crewID;
        this.flightID = flightID;
        this.crew = crew;
    }
    
    public int getAssignmentID() {
        return assignmentID;
    }
    public void setAssignmentID(int assignmentID) {
        this.assignmentID = assignmentID;
    }
    public int getCrewID() {
        return crewID;
    }
    public void setCrewID(int crewID) {
        this.crewID = crewID;
    }
    public int getFlightID() {
        return flightID;
    }
    public void setFlightID(int flightID) {
        this.flightID = flightID;
    }
    public Crew getCrew() {
        return crew;
    }
    public void setCrew(Crew crew) {
        this.crew = crew;
    }
    
}
