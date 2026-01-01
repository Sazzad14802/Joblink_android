package com.example.joblink.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private final DatabaseReference database;
    private String currentUserId;

    private FirebaseHelper() {
        database = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public DatabaseReference getDatabase() {
        return database;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public boolean isUserLoggedIn() {
        return currentUserId != null;
    }

    public void signOut() {
        currentUserId = null;
    }
}

