package com.example.joblink.repositories;

import com.example.joblink.models.Application;
import com.example.joblink.utils.FirebaseHelper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class ApplicationRepository {
    private static final String TAG = "ApplicationRepository";
    private static final String APPLICATIONS_PATH = "applications";
    private final DatabaseReference database;

    public ApplicationRepository() {
        this.database = FirebaseHelper.getInstance().getDatabase();
    }

    // Create application
    public Task<Void> createApplication(Application application) {
        String applicationId = database.child(APPLICATIONS_PATH).push().getKey();
        if (applicationId != null) {
            application.setApplicationId(applicationId);
            return database.child(APPLICATIONS_PATH).child(applicationId).setValue(application);
        }
        return null;
    }

    // Update application status
    public Task<Void> updateApplicationStatus(String applicationId, String status) {
        return database.child(APPLICATIONS_PATH).child(applicationId).child("status").setValue(status);
    }

    // Get application by ID
    public Task<DataSnapshot> getApplicationById(String applicationId) {
        return database.child(APPLICATIONS_PATH).child(applicationId).get();
    }

    // Get applications by user (job seeker)
    public Task<DataSnapshot> getApplicationsByUser(String userId) {
        return database.child(APPLICATIONS_PATH)
                .orderByChild("userId")
                .equalTo(userId)
                .get();
    }

    // Get applications by user and status
    public Task<DataSnapshot> getApplicationsByUserAndStatus(String userId, String status) {
        return database.child(APPLICATIONS_PATH)
                .orderByChild("userId")
                .equalTo(userId)
                .get();
    }

    // Get applications for a job
    public Task<DataSnapshot> getApplicationsByJob(String jobId) {
        return database.child(APPLICATIONS_PATH)
                .orderByChild("jobId")
                .equalTo(jobId)
                .get();
    }

    // Get applications by employer (all jobs posted by employer)
    public Task<DataSnapshot> getApplicationsByEmployer(String employerId) {
        return database.child(APPLICATIONS_PATH)
                .orderByChild("employerId")
                .equalTo(employerId)
                .get();
    }

    // Check if user already applied for a job
    public Task<DataSnapshot> checkExistingApplication(String userId, String jobId) {
        return database.child(APPLICATIONS_PATH)
                .orderByChild("userId")
                .equalTo(userId)
                .get();
    }

    // Delete application
    public Task<Void> deleteApplication(String applicationId) {
        return database.child(APPLICATIONS_PATH).child(applicationId).removeValue();
    }

    // Delete applications by job (when job is deleted)
    public Task<DataSnapshot> getApplicationsByJobForDeletion(String jobId) {
        return database.child(APPLICATIONS_PATH)
                .orderByChild("jobId")
                .equalTo(jobId)
                .get();
    }

    // Delete applications by user (when user is deleted)
    public Task<DataSnapshot> getApplicationsByUserForDeletion(String userId) {
        return database.child(APPLICATIONS_PATH)
                .orderByChild("userId")
                .equalTo(userId)
                .get();
    }
}

