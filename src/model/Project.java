package model;

//Represents a community project in the VolunTrack system
public class Project {
    private int id;
    private String title;
    private String location;
    private String day;
    private double hourlyValue;
    private int totalSlots;
    private int registeredSlots;
    private boolean enabled;
    private int selectedSlots = 1;
    private int selectedHours = 1;
    
    //Constructor for the project
    public Project(int id, String title, String location, String day,
                   double hourlyValue, int totalSlots,
                   int registeredSlots, boolean enabled) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.day = day;
        this.hourlyValue = hourlyValue;
        this.totalSlots = totalSlots;
        this.registeredSlots = registeredSlots;
        this.enabled = enabled;
    }

    //Calculates how many slots are still available   
    public int getAvailableSlots() {
        return totalSlots - registeredSlots;
    }

    //Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public double getHourlyValue() { return hourlyValue; }
    public void setHourlyValue(double hourlyValue) { this.hourlyValue = hourlyValue; }

    public int getTotalSlots() { return totalSlots; }
    public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }

    public int getRegisteredSlots() { return registeredSlots; }
    public void setRegisteredSlots(int registeredSlots) { this.registeredSlots = registeredSlots; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getSelectedSlots() { return selectedSlots; }
    public void setSelectedSlots(int slots) { this.selectedSlots = slots; }

    public int getSelectedHours() { return selectedHours; }
    public void setSelectedHours(int hours) { this.selectedHours = hours; }
}
