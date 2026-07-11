package model;

import java.time.LocalDateTime;

//Represents a confirmed volunteer registration for a project

public class Registration {
    private int id;
    private int userId;
    private int projectId;
    private int slots;
    private int hours;
    private double contribution;
    private LocalDateTime confirmedAt;
    private String projectTitle;
    private String projectLocation;
    private String projectDay;

    //Constructor for the registration
    public Registration(int id, int userId, int projectId,
                        int slots, int hours,
                        double contribution, LocalDateTime confirmedAt) {
        this.id = id;
        this.userId = userId;
        this.projectId = projectId;
        this.slots = slots;
        this.hours = hours;
        this.contribution = contribution;
        this.confirmedAt = confirmedAt;
    }

    //Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public int getSlots() { return slots; }
    public void setSlots(int slots) { this.slots = slots; }

    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }

    public double getContribution() { return contribution; }
    public void setContribution(double contribution) { this.contribution = contribution; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getProjectLocation() { return projectLocation; }
    public void setProjectLocation(String projectLocation) { this.projectLocation = projectLocation; }

    public String getProjectDay() { return projectDay; }
    public void setProjectDay(String projectDay) { this.projectDay = projectDay; }
}
