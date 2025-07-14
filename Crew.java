public class Crew {
    private int userID;
    private String fname;
    private String lname;
    private String designation;
    private String contactNo;

    public Crew(int userID, String fname, String lname, String designation, String contactNo) {
        this.userID = userID;
        this.fname = fname;
        this.lname = lname;
        this.designation = designation;
        this.contactNo = contactNo;
    }

    public int getUserID() { return userID; }
    public String getFname() { return fname; }
    public String getLname() { return lname; }
    public String getDesignation() { return designation; }
    public String getContactNo() { return contactNo; }

    public void setUserID(int userID) { this.userID = userID; }
    public void setFname(String fname) { this.fname = fname; }
    public void setLname(String lname) { this.lname = lname; }
    public void setDesignation(String designation) { this.designation = designation; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
}
