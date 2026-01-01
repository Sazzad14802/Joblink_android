package com.example.joblink.activities;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblink.R;
import com.example.joblink.adapters.JobAdapter;
import com.example.joblink.models.Job;
import com.example.joblink.repositories.ApplicationRepository;
import com.example.joblink.repositories.JobRepository;
import com.example.joblink.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BrowseJobsActivity extends AppCompatActivity {
    private RecyclerView jobsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private EditText minSalaryInput;
    private Button filterButton, clearFilterButton;

    private FirebaseHelper firebaseHelper;
    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private JobAdapter adapter;
    private List<Job> jobList;
    private List<String> appliedJobIds;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_jobs);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Browse Jobs");
        }

        initializeViews();
        firebaseHelper = FirebaseHelper.getInstance();
        jobRepository = new JobRepository();
        applicationRepository = new ApplicationRepository();

        jobList = new ArrayList<>();
        appliedJobIds = new ArrayList<>();
        setupRecyclerView();
        loadAppliedJobs();

        filterButton.setOnClickListener(v -> filterJobs());
        clearFilterButton.setOnClickListener(v -> {
            minSalaryInput.setText("");
            loadJobs();
        });
    }

    private void initializeViews() {
        jobsRecyclerView = findViewById(R.id.jobsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        minSalaryInput = findViewById(R.id.minSalaryInput);
        filterButton = findViewById(R.id.filterButton);
        clearFilterButton = findViewById(R.id.clearFilterButton);
    }

    private void setupRecyclerView() {
        jobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JobAdapter(jobList, false, this::onJobClick, null, null);
        jobsRecyclerView.setAdapter(adapter);
    }

    private void loadAppliedJobs() {
        String uid = firebaseHelper.getCurrentUserId();
        applicationRepository.getApplicationsByUser(uid).addOnSuccessListener(dataSnapshot -> {
            appliedJobIds.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                String jobId = snapshot.child("jobId").getValue(String.class);
                if (jobId != null) {
                    appliedJobIds.add(jobId);
                }
            }
            loadJobs();
        });
    }

    private void loadJobs() {
        // Prevent multiple simultaneous loads
        if (isLoading) {
            android.util.Log.d("BrowseJobs", "Already loading, skipping duplicate call");
            return;
        }

        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        jobList.clear();

        String currentUserId = firebaseHelper.getCurrentUserId();
        java.util.Set<String> addedJobIds = new java.util.HashSet<>();
        java.util.Set<String> addedJobSignatures = new java.util.HashSet<>();

        // Use array to hold mutable counters (workaround for lambda limitation)
        final int[] counters = new int[2]; // [0] = totalCount, [1] = duplicateCount

        jobRepository.getAllJobs().addOnSuccessListener(dataSnapshot -> {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Job job = snapshot.getValue(Job.class);
                counters[0]++; // totalCount

                if (job != null && job.getJobId() != null && job.getTitle() != null) {
                    // Create unique signature based on content
                    String jobSignature = job.getTitle() + "|" + job.getSalary() + "|" +
                                        job.getLocation() + "|" + job.getPostedBy();

                    // Debug: Check why job might be filtered
                    boolean isOwnJob = job.getPostedBy().equals(currentUserId);
                    boolean isApplied = appliedJobIds.contains(job.getJobId());
                    boolean isDuplicateId = addedJobIds.contains(job.getJobId());
                    boolean isDuplicateContent = addedJobSignatures.contains(jobSignature);

                    if (isDuplicateId || isDuplicateContent) {
                        counters[1]++; // duplicateCount
                        android.util.Log.d("BrowseJobs", "Duplicate detected: " + job.getTitle() +
                            " (IDdup: " + isDuplicateId + ", Content dup: " + isDuplicateContent + ")");
                    }

                    // Exclude: current user's jobs, already applied, duplicate IDs, duplicate content
                    if (!isOwnJob && !isApplied && !isDuplicateId && !isDuplicateContent) {
                        jobList.add(job);
                        addedJobIds.add(job.getJobId());
                        addedJobSignatures.add(jobSignature);
                        android.util.Log.d("BrowseJobs", "Added job: " + job.getTitle() + " (ID: " + job.getJobId() + ")");
                    }
                }
            }

            android.util.Log.d("BrowseJobs", "Total jobs in Firebase: " + counters[0]);
            android.util.Log.d("BrowseJobs", "Duplicates filtered: " + counters[1]);
            android.util.Log.d("BrowseJobs", "Jobs displayed: " + jobList.size());

            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            isLoading = false; // Reset loading flag

            if (jobList.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                jobsRecyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                jobsRecyclerView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            isLoading = false; // Reset loading flag on error
            Toast.makeText(this, "Error loading jobs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void filterJobs() {
        String minSalaryStr = minSalaryInput.getText().toString().trim();

        if (TextUtils.isEmpty(minSalaryStr)) {
            Toast.makeText(this, "Please enter minimum salary", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double minSalary = Double.parseDouble(minSalaryStr);

            progressBar.setVisibility(View.VISIBLE);
            jobList.clear();

            String currentUserId = firebaseHelper.getCurrentUserId();
            java.util.Set<String> addedJobIds = new java.util.HashSet<>();
            java.util.Set<String> addedJobSignatures = new java.util.HashSet<>();

            jobRepository.getJobsBySalary(minSalary).addOnSuccessListener(dataSnapshot -> {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Job job = snapshot.getValue(Job.class);
                    if (job != null && job.getJobId() != null && job.getTitle() != null) {
                        // Create a unique signature based on content
                        String jobSignature = job.getTitle() + "|" + job.getSalary() + "|" +
                                            job.getLocation() + "|" + job.getPostedBy();

                        // Exclude jobs posted by current user, already applied jobs, and duplicates (by ID or content)
                        if (!job.getPostedBy().equals(currentUserId)
                            && !appliedJobIds.contains(job.getJobId())
                            && !addedJobIds.contains(job.getJobId())
                            && !addedJobSignatures.contains(jobSignature)) {
                            jobList.add(job);
                            addedJobIds.add(job.getJobId());
                            addedJobSignatures.add(jobSignature);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (jobList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    jobsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    jobsRecyclerView.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error filtering jobs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid salary format", Toast.LENGTH_SHORT).show();
        }
    }

    private void onJobClick(Job job) {
        Intent intent = new Intent(this, JobDetailsActivity.class);
        intent.putExtra("JOB_ID", job.getJobId());
        intent.putExtra("IS_OWNER", false);
        startActivity(intent);
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
        loadAppliedJobs();
    }
}

