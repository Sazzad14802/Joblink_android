package com.example.joblink.models;

public class Job {
    private String jobId;
    private String title;
    private String description;
    private double salary;
    private String location;
    private String postedBy; // Employer UID
    private String postedByName; // Employer name
    private long postedDate;
    private int applicationCount;

    // Required empty constructor for Firebase
    public Job() {
    }

    public Job(String jobId, String title, String description, double salary, String location,
               String postedBy, String postedByName, long postedDate) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.salary = salary;
        this.location = location;
        this.postedBy = postedBy;
        this.postedByName = postedByName;
        this.postedDate = postedDate;
        this.applicationCount = 0;
    }

    // Getters and Setters
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getPostedByName() {
        return postedByName;
    }

    public void setPostedByName(String postedByName) {
        this.postedByName = postedByName;
    }

    public long getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(long postedDate) {
        this.postedDate = postedDate;
    }

    public int getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(int applicationCount) {
        this.applicationCount = applicationCount;
    }
}

