package com.example.joblink.models;

public class Application {
    private String applicationId;
    private String jobId;
    private String jobTitle;
    private String userId; // Job seeker UID
    private String userName;
    private String userEmail;
    private String status; // pending, accepted, rejected
    private long appliedDate;
    private String experience;
    private String employerId; // To filter applications for employers

    // Required empty constructor for Firebase
    public Application() {
    }

    public Application(String applicationId, String jobId, String jobTitle, String userId,
                       String userName, String userEmail, String status, long appliedDate,
                       String experience, String employerId) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.status = status;
        this.appliedDate = appliedDate;
        this.experience = experience;
        this.employerId = employerId;
    }

    // Getters and Setters
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(long appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    // Status constants
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACCEPTED = "accepted";
    public static final String STATUS_REJECTED = "rejected";
}

