package com.example.joblink.repositories;

import com.example.joblink.models.Job;
import com.example.joblink.utils.FirebaseHelper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class JobRepository {
    private static final String TAG = "JobRepository";
    private static final String JOBS_PATH = "jobs";
    private final DatabaseReference database;

    public JobRepository() {
        this.database = FirebaseHelper.getInstance().getDatabase();
    }

    // Create job
    public Task<Void> createJob(Job job) {
        String jobId = database.child(JOBS_PATH).push().getKey();
        if (jobId != null) {
            job.setJobId(jobId);
            return database.child(JOBS_PATH).child(jobId).setValue(job);
        }
        return null;
    }

    // Update job
    public Task<Void> updateJob(Job job) {
        return database.child(JOBS_PATH).child(job.getJobId()).setValue(job);
    }

    // Delete job
    public Task<Void> deleteJob(String jobId) {
        return database.child(JOBS_PATH).child(jobId).removeValue();
    }

    // Get job by ID
    public Task<DataSnapshot> getJobById(String jobId) {
        return database.child(JOBS_PATH).child(jobId).get();
    }

    // Get all jobs
    public Task<DataSnapshot> getAllJobs() {
        return database.child(JOBS_PATH).get();
    }

    // Get jobs by employer
    public Task<DataSnapshot> getJobsByEmployer(String employerId) {
        return database.child(JOBS_PATH)
                .orderByChild("postedBy")
                .equalTo(employerId)
                .get();
    }

    // Get jobs with salary filter
    public Task<DataSnapshot> getJobsBySalary(double minSalary) {
        return database.child(JOBS_PATH)
                .orderByChild("salary")
                .startAt(minSalary)
                .get();
    }

    // Update application count
    public Task<Void> updateApplicationCount(String jobId, int count) {
        return database.child(JOBS_PATH).child(jobId).child("applicationCount").setValue(count);
    }
}

