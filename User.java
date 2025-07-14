public class User {
    private int userId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String userType; // "PASSENGER" or "ADMIN"

    public User(int userId, String username, String password, String firstName, String lastName, String userType) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUserType() { return userType; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUserType(String userType) { this.userType = userType; }
}
