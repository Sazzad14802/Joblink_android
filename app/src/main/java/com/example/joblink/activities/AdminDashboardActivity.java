package com.example.joblink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblink.R;
import com.example.joblink.adapters.UserManagementAdapter;
import com.example.joblink.models.User;
import com.example.joblink.repositories.ApplicationRepository;
import com.example.joblink.repositories.JobRepository;
import com.example.joblink.repositories.UserRepository;
import com.example.joblink.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView welcomeText, totalUsersText, emptyView;
    private RecyclerView usersRecyclerView;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private UserRepository userRepository;
    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private UserManagementAdapter adapter;
    private List<User> userList;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViews();
        firebaseHelper = FirebaseHelper.getInstance();
        userRepository = new UserRepository();
        jobRepository = new JobRepository();
        applicationRepository = new ApplicationRepository();

        userList = new ArrayList<>();
        setupRecyclerView();
        loadUsers();
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        totalUsersText = findViewById(R.id.totalUsersText);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        welcomeText.setText("Admin Dashboard");
    }

    private void setupRecyclerView() {
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserManagementAdapter(userList, this::showDeleteUserDialog);
        usersRecyclerView.setAdapter(adapter);
    }

    private void loadUsers() {
        try {
            // Prevent multiple simultaneous loads
            if (isLoading) {
                android.util.Log.d("AdminDashboard", "Already loading, skipping duplicate call");
                return;
            }

            isLoading = true;
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            userList.clear();

            android.util.Log.d("AdminDashboard", "Starting to load users...");

            userRepository.getAllUsers().addOnSuccessListener(dataSnapshot -> {
                try {
                    android.util.Log.d("AdminDashboard", "Received data from Firebase");
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            android.util.Log.d("AdminDashboard", "User: " + user.getName() + " - Type: " + user.getAccountType());
                            if (!User.ACCOUNT_TYPE_ADMIN.equals(user.getAccountType())) {
                                userList.add(user);
                            }
                        }
                    }

                    android.util.Log.d("AdminDashboard", "Users loaded: " + userList.size());

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    isLoading = false;

                    if (totalUsersText != null) {
                        totalUsersText.setText("Total Users: " + userList.size());
                    }

                    if (userList.isEmpty()) {
                        if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                        if (usersRecyclerView != null) usersRecyclerView.setVisibility(View.GONE);
                    } else {
                        if (emptyView != null) emptyView.setVisibility(View.GONE);
                        if (usersRecyclerView != null) usersRecyclerView.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    android.util.Log.e("AdminDashboard", "Error processing users: " + e.getMessage(), e);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    isLoading = false;
                    Toast.makeText(this, "Error processing users: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(e -> {
                android.util.Log.e("AdminDashboard", "Firebase error: " + e.getMessage(), e);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                isLoading = false;
                Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } catch (Exception e) {
            android.util.Log.e("AdminDashboard", "Exception in loadUsers: " + e.getMessage(), e);
            isLoading = false;
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void countUserStats(User user) {
        // This is simplified - in production, you'd want to fetch these counts more efficiently
        if (user.getAccountType().equals(User.ACCOUNT_TYPE_EMPLOYER)) {
            jobRepository.getJobsByEmployer(user.getUid()).addOnSuccessListener(jobs -> {
                // Store count in user object (you might need to add a field for this)
            });
        } else if (user.getAccountType().equals(User.ACCOUNT_TYPE_SEEKER)) {
            applicationRepository.getApplicationsByUser(user.getUid()).addOnSuccessListener(apps -> {
                // Store count in user object
            });
        }
    }

    private void showDeleteUserDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getName() + "? This will also delete all their jobs and applications.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        progressBar.setVisibility(View.VISIBLE);

        // Delete user's jobs and related applications if employer
        if (user.getAccountType().equals(User.ACCOUNT_TYPE_EMPLOYER)) {
            jobRepository.getJobsByEmployer(user.getUid()).addOnSuccessListener(jobsSnapshot -> {
                List<String> jobIds = new ArrayList<>();

                // Collect all job IDs
                for (DataSnapshot snapshot : jobsSnapshot.getChildren()) {
                    String jobId = snapshot.getKey();
                    if (jobId != null) {
                        jobIds.add(jobId);
                    }
                }

                if (jobIds.isEmpty()) {
                    // No jobs to delete, proceed to delete user
                    deleteUserFromDatabase(user);
                } else {
                    // Delete applications for each job, then delete the jobs
                    deleteJobsAndApplications(jobIds, user);
                }
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error fetching user's jobs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else if (user.getAccountType().equals(User.ACCOUNT_TYPE_SEEKER)) {
            // Delete user's applications if seeker
            applicationRepository.getApplicationsByUser(user.getUid()).addOnSuccessListener(dataSnapshot -> {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String appId = snapshot.getKey();
                    if (appId != null) {
                        applicationRepository.deleteApplication(appId);
                    }
                }
                // After deleting applications, delete the user
                deleteUserFromDatabase(user);
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error deleting applications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // Admin or other types - just delete the user
            deleteUserFromDatabase(user);
        }
    }

    private void deleteJobsAndApplications(List<String> jobIds, User user) {
        // Counter to track deletions
        final int[] pendingDeletions = {jobIds.size()};
        final boolean[] hasError = {false};

        for (String jobId : jobIds) {
            // First delete all applications for this job
            applicationRepository.getApplicationsByJob(jobId).addOnSuccessListener(appsSnapshot -> {
                for (DataSnapshot appSnapshot : appsSnapshot.getChildren()) {
                    String appId = appSnapshot.getKey();
                    if (appId != null) {
                        applicationRepository.deleteApplication(appId);
                    }
                }

                // Then delete the job itself
                jobRepository.deleteJob(jobId).addOnSuccessListener(aVoid -> {
                    pendingDeletions[0]--;

                    // When all jobs are deleted, delete the user
                    if (pendingDeletions[0] == 0 && !hasError[0]) {
                        deleteUserFromDatabase(user);
                    }
                }).addOnFailureListener(e -> {
                    hasError[0] = true;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error deleting job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                hasError[0] = true;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error fetching job applications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void deleteUserFromDatabase(User user) {
        userRepository.deleteUser(user.getUid()).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
            loadUsers(); // Reload the list
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    firebaseHelper.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}

