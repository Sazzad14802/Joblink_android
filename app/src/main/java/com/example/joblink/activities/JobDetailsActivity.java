package com.example.joblink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.R;
import com.example.joblink.models.Job;
import com.example.joblink.repositories.ApplicationRepository;
import com.example.joblink.repositories.JobRepository;
import com.example.joblink.repositories.UserRepository;
import com.example.joblink.utils.FirebaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JobDetailsActivity extends AppCompatActivity {
    private TextView titleText, companyText, salaryText, locationText, descriptionText, postedDateText;
    private Button applyButton, editButton, deleteButton, viewApplicationsButton;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private UserRepository userRepository;
    private Job currentJob;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Job Details");
        }

        initializeViews();
        firebaseHelper = FirebaseHelper.getInstance();
        jobRepository = new JobRepository();
        applicationRepository = new ApplicationRepository();
        userRepository = new UserRepository();

        String jobId = getIntent().getStringExtra("JOB_ID");
        isOwner = getIntent().getBooleanExtra("IS_OWNER", false);

        if (jobId != null) {
            loadJobDetails(jobId);
        } else {
            Toast.makeText(this, "Job not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupClickListeners();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        companyText = findViewById(R.id.companyText);
        salaryText = findViewById(R.id.salaryText);
        locationText = findViewById(R.id.locationText);
        descriptionText = findViewById(R.id.descriptionText);
        postedDateText = findViewById(R.id.postedDateText);
        applyButton = findViewById(R.id.applyButton);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);
        viewApplicationsButton = findViewById(R.id.viewApplicationsButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadJobDetails(String jobId) {
        progressBar.setVisibility(View.VISIBLE);

        jobRepository.getJobById(jobId).addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                currentJob = dataSnapshot.getValue(Job.class);
                if (currentJob != null) {
                    displayJobDetails();
                    setupButtonVisibility();
                    checkIfAlreadyApplied();
                }
            } else {
                Toast.makeText(this, "Job not found", Toast.LENGTH_SHORT).show();
                finish();
            }
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error loading job", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void displayJobDetails() {
        titleText.setText(currentJob.getTitle());
        companyText.setText("Posted by: " + currentJob.getPostedByName());
        salaryText.setText("$" + String.format(Locale.US, "%,.2f", currentJob.getSalary()));
        locationText.setText(currentJob.getLocation());
        descriptionText.setText(currentJob.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        postedDateText.setText("Posted on: " + sdf.format(new Date(currentJob.getPostedDate())));
    }

    private void setupButtonVisibility() {
        if (isOwner) {
            applyButton.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            viewApplicationsButton.setVisibility(View.VISIBLE);
        } else {
            applyButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            viewApplicationsButton.setVisibility(View.GONE);
        }
    }

    private void checkIfAlreadyApplied() {
        if (!isOwner) {
            String userId = firebaseHelper.getCurrentUserId();
            applicationRepository.checkExistingApplication(userId, currentJob.getJobId())
                    .addOnSuccessListener(dataSnapshot -> {
                        // Check if any of the user's applications match this specific jobId
                        boolean alreadyApplied = false;
                        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                            for (com.google.firebase.database.DataSnapshot appSnapshot : dataSnapshot.getChildren()) {
                                String appliedJobId = appSnapshot.child("jobId").getValue(String.class);
                                if (currentJob.getJobId().equals(appliedJobId)) {
                                    alreadyApplied = true;
                                    break;
                                }
                            }
                        }

                        if (alreadyApplied) {
                            applyButton.setEnabled(false);
                            applyButton.setText("Already Applied");
                        }
                    });
        }
    }

    private void setupClickListeners() {
        applyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ApplyJobActivity.class);
            intent.putExtra("JOB_ID", currentJob.getJobId());
            intent.putExtra("JOB_TITLE", currentJob.getTitle());
            intent.putExtra("EMPLOYER_ID", currentJob.getPostedBy());
            startActivity(intent);
        });

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditJobActivity.class);
            intent.putExtra("JOB_ID", currentJob.getJobId());
            startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> showDeleteDialog());

        viewApplicationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewApplicationsActivity.class);
            intent.putExtra("JOB_ID", currentJob.getJobId());
            intent.putExtra("JOB_TITLE", currentJob.getTitle());
            startActivity(intent);
        });
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Job")
                .setMessage("Are you sure you want to delete this job? All applications will also be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> deleteJob())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteJob() {
        progressBar.setVisibility(View.VISIBLE);

        // Delete all applications for this job
        applicationRepository.getApplicationsByJob(currentJob.getJobId())
                .addOnSuccessListener(dataSnapshot -> {
                    for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String appId = snapshot.getKey();
                        if (appId != null) {
                            applicationRepository.deleteApplication(appId);
                        }
                    }
                });

        // Delete the job
        jobRepository.deleteJob(currentJob.getJobId()).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Job deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error deleting job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentJob != null) {
            loadJobDetails(currentJob.getJobId());
        }
    }
}

