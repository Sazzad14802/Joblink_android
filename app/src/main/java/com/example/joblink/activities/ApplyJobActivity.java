package com.example.joblink.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.R;
import com.example.joblink.models.Application;
import com.example.joblink.models.User;
import com.example.joblink.repositories.ApplicationRepository;
import com.example.joblink.repositories.JobRepository;
import com.example.joblink.repositories.UserRepository;
import com.example.joblink.utils.FirebaseHelper;

public class ApplyJobActivity extends AppCompatActivity {
    private TextView jobTitleText;
    private EditText experienceInput;
    private Button submitButton;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private ApplicationRepository applicationRepository;
    private UserRepository userRepository;
    private JobRepository jobRepository;

    private String jobId;
    private String jobTitle;
    private String employerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_job);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Apply for Job");
        }

        initializeViews();
        firebaseHelper = FirebaseHelper.getInstance();
        applicationRepository = new ApplicationRepository();
        userRepository = new UserRepository();
        jobRepository = new JobRepository();

        jobId = getIntent().getStringExtra("JOB_ID");
        jobTitle = getIntent().getStringExtra("JOB_TITLE");
        employerId = getIntent().getStringExtra("EMPLOYER_ID");

        jobTitleText.setText(jobTitle);

        submitButton.setOnClickListener(v -> submitApplication());
    }

    private void initializeViews() {
        jobTitleText = findViewById(R.id.jobTitleText);
        experienceInput = findViewById(R.id.experienceInput);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void submitApplication() {
        String experience = experienceInput.getText().toString().trim();

        if (TextUtils.isEmpty(experience)) {
            experienceInput.setError("Please describe your experience");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        String userId = firebaseHelper.getCurrentUserId();

        // Check if already applied
        applicationRepository.checkExistingApplication(userId, jobId)
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(this, "You have already applied for this job", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Get user data
                    userRepository.getUserById(userId).addOnSuccessListener(userSnapshot -> {
                        if (userSnapshot.exists()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                Application application = new Application(
                                        null,
                                        jobId,
                                        jobTitle,
                                        userId,
                                        user.getName(),
                                        user.getEmail(),
                                        Application.STATUS_PENDING,
                                        System.currentTimeMillis(),
                                        experience,
                                        employerId
                                );

                                applicationRepository.createApplication(application)
                                        .addOnSuccessListener(aVoid -> {
                                            // Update job application count
                                            updateJobApplicationCount();

                                            progressBar.setVisibility(View.GONE);
                                            submitButton.setEnabled(true);
                                            Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            submitButton.setEnabled(true);
                                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    }).addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                });
    }

    private void updateJobApplicationCount() {
        applicationRepository.getApplicationsByJob(jobId).addOnSuccessListener(dataSnapshot -> {
            jobRepository.updateApplicationCount(jobId, (int) dataSnapshot.getChildrenCount());
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
}

