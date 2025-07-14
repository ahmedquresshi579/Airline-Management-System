public class Session {
    public static int adminUserID;
    public static String adminName;
    public static int passengerID;
    public static String passengerName;

    public static String getUserId() {
        return String.valueOf(passengerID);
    }

    public static void logout() {
        adminUserID = 0;
        adminName = null;
        passengerID = 0;
        passengerName = null;
    }
} 