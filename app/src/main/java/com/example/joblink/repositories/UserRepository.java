package com.example.joblink.repositories;

import com.example.joblink.models.User;
import com.example.joblink.utils.FirebaseHelper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String USERS_PATH = "users";
    private final DatabaseReference database;

    public UserRepository() {
        this.database = FirebaseHelper.getInstance().getDatabase();
    }

    // Create or update user
    public Task<Void> saveUser(User user) {
        return database.child(USERS_PATH).child(user.getUid()).setValue(user);
    }

    // Get user by ID
    public Task<DataSnapshot> getUserById(String uid) {
        return database.child(USERS_PATH).child(uid).get();
    }

    // Get user by email (returns Task for consistency)
    public Task<DataSnapshot> getUserByEmail(String email) {
        return database.child(USERS_PATH)
                .orderByChild("email")
                .equalTo(email)
                .get();
    }

    // Get all users (for admin)
    public Task<DataSnapshot> getAllUsers() {
        return database.child(USERS_PATH).get();
    }

    // Delete user
    public Task<Void> deleteUser(String uid) {
        return database.child(USERS_PATH).child(uid).removeValue();
    }

    // Update user field
    public Task<Void> updateUser(String uid, String field, Object value) {
        return database.child(USERS_PATH).child(uid).child(field).setValue(value);
    }
}

