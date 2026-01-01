package com.example.joblink.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private String password;  // Plain text password
    private String accountType; // SEEKER, EMPLOYER, ADMIN
    private long createdAt;

    // Required empty constructor for Firebase
    public User() {
    }

    public User(String uid, String name, String email, String password, String accountType, long createdAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.password = password;
        this.accountType = accountType;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Account type constants
    public static final String ACCOUNT_TYPE_SEEKER = "SEEKER";
    public static final String ACCOUNT_TYPE_EMPLOYER = "EMPLOYER";
    public static final String ACCOUNT_TYPE_ADMIN = "ADMIN";
}

