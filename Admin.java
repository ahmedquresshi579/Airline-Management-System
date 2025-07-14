public class Admin {
    private int userID;
    private String fname;
    private String lname;
    private String passwords;

    public Admin(int userID, String fname, String lname, String passwords) {
        this.userID = userID;
        this.fname = fname;
        this.lname = lname;
        this.passwords = passwords;
    }

    public int getUserID() { return userID; }
    public String getFname() { return fname; }
    public String getLname() { return lname; }
    public String getPasswords() { return passwords; }

    public void setUserID(int userID) { this.userID = userID; }
    public void setFname(String fname) { this.fname = fname; }
    public void setLname(String lname) { this.lname = lname; }
    public void setPasswords(String passwords) { this.passwords = passwords; }
}