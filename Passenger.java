public class Passenger {
    private int userID;
    private String fname;
    private String lname;
    private String email;
    private String passwords;
    private String passportNo;
    private String contactNo;

    public Passenger() {
        // Default constructor
    }

    public Passenger(int userID, String fname, String lname, String email, String passwords, String passportNo, String contactNo) {
        this.userID = userID;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.passwords = passwords;
        this.passportNo = passportNo;
        this.contactNo = contactNo;
    }

    // Getters
    public int getUserID() { return userID; }
    public String getFname() { return fname; }
    public String getLname() { return lname; }
    public String getEmail() { return email; }
    public String getPasswords() { return passwords; }
    public String getPassportNo() { return passportNo; }
    public String getContactNo() { return contactNo; }

    // Setters
    public void setUserID(int userID) { this.userID = userID; }
    public void setFname(String fname) { this.fname = fname; }
    public void setLname(String lname) { this.lname = lname; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswords(String passwords) { this.passwords = passwords; }
    public void setPassportNo(String passportNo) { this.passportNo = passportNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
}
