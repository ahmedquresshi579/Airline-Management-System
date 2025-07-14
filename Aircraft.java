public class Aircraft {
    private int aircraftID;
    private String model;
    private int capacity;
    private String regNo;
    private String manufacturer;
    private int yearOfManufacture;

    public Aircraft(int aircraftID, String model, int capacity, String regNo, String manufacturer, int yearOfManufacture) {
        this.aircraftID = aircraftID;
        this.model = model;
        this.capacity = capacity;
        this.regNo = regNo;
        this.manufacturer = manufacturer;
        this.yearOfManufacture = yearOfManufacture;
    }

    public int getAircraftID() { return aircraftID; }
    public String getModel() { return model; }
    public int getCapacity() { return capacity; }
    public String getRegNo() { return regNo; }
    public String getManufacturer() { return manufacturer; }
    public int getYearOfManufacture() { return yearOfManufacture; }

    public void setAircraftID(int aircraftID) { this.aircraftID = aircraftID; }
    public void setModel(String model) { this.model = model; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public void setYearOfManufacture(int yearOfManufacture) { this.yearOfManufacture = yearOfManufacture; }
}
