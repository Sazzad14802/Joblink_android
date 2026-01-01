package com.example.joblink.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.R;
import com.example.joblink.models.Job;
import com.example.joblink.repositories.JobRepository;
import com.example.joblink.repositories.UserRepository;
import com.example.joblink.utils.FirebaseHelper;

public class EditJobActivity extends AppCompatActivity {
    private EditText titleInput, descriptionInput, salaryInput, locationInput;
    private Button postJobButton;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private JobRepository jobRepository;
    private UserRepository userRepository;
    private Job editingJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        firebaseHelper = FirebaseHelper.getInstance();
        jobRepository = new JobRepository();
        userRepository = new UserRepository();

        // Load the job to edit
        String jobId = getIntent().getStringExtra("JOB_ID");
        if (jobId != null) {
            setTitle("Edit Job");
            loadJobData(jobId);
        } else {
            Toast.makeText(this, "Error: No job ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        postJobButton.setOnClickListener(v -> updateJob());
    }

    private void initializeViews() {
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        salaryInput = findViewById(R.id.salaryInput);
        locationInput = findViewById(R.id.locationInput);
        postJobButton = findViewById(R.id.postJobButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadJobData(String jobId) {
        progressBar.setVisibility(View.VISIBLE);
        jobRepository.getJobById(jobId).addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                editingJob = dataSnapshot.getValue(Job.class);
                if (editingJob != null) {
                    titleInput.setText(editingJob.getTitle());
                    descriptionInput.setText(editingJob.getDescription());
                    salaryInput.setText(String.valueOf(editingJob.getSalary()));
                    locationInput.setText(editingJob.getLocation());
                    postJobButton.setText("Update Job");
                }
            }
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error loading job", Toast.LENGTH_SHORT).show();
            finish();
        });
    }


    private void updateJob() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String salaryStr = salaryInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();

        if (!validateInputs(title, description, salaryStr, location)) {
            return;
        }

        double salary = Double.parseDouble(salaryStr);
        progressBar.setVisibility(View.VISIBLE);
        postJobButton.setEnabled(false);

        editingJob.setTitle(title);
        editingJob.setDescription(description);
        editingJob.setSalary(salary);
        editingJob.setLocation(location);

        jobRepository.updateJob(editingJob).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            postJobButton.setEnabled(true);
            Toast.makeText(this, "Job updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            postJobButton.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInputs(String title, String description, String salary, String location) {
        if (TextUtils.isEmpty(title)) {
            titleInput.setError("Title is required");
            return false;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionInput.setError("Description is required");
            return false;
        }

        if (TextUtils.isEmpty(salary)) {
            salaryInput.setError("Salary is required");
            return false;
        }

        try {
            double sal = Double.parseDouble(salary);
            if (sal <= 0) {
                salaryInput.setError("Salary must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            salaryInput.setError("Invalid salary format");
            return false;
        }

        if (TextUtils.isEmpty(location)) {
            locationInput.setError("Location is required");
            return false;
        }

        return true;
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

