package com.example.joblink.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblink.R;
import com.example.joblink.adapters.ApplicationAdapter;
import com.example.joblink.models.Application;
import com.example.joblink.repositories.ApplicationRepository;
import com.example.joblink.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewApplicationsActivity extends AppCompatActivity {
    private RecyclerView applicationsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyView, jobTitleText;

    private FirebaseHelper firebaseHelper;
    private ApplicationRepository applicationRepository;
    private ApplicationAdapter adapter;
    private List<Application> applicationList;
    private String jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_applications);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Applications");
        }

        initializeViews();
        firebaseHelper = FirebaseHelper.getInstance();
        applicationRepository = new ApplicationRepository();

        jobId = getIntent().getStringExtra("JOB_ID");
        String jobTitle = getIntent().getStringExtra("JOB_TITLE");

        jobTitleText.setText("Applications for: " + jobTitle);

        applicationList = new ArrayList<>();
        setupRecyclerView();
        loadApplications();
    }

    private void initializeViews() {
        applicationsRecyclerView = findViewById(R.id.applicationsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        jobTitleText = findViewById(R.id.jobTitleText);
    }

    private void setupRecyclerView() {
        applicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicationAdapter(applicationList, true, this::onStatusChange);
        applicationsRecyclerView.setAdapter(adapter);
    }

    private void loadApplications() {
        progressBar.setVisibility(View.VISIBLE);
        applicationList.clear();

        applicationRepository.getApplicationsByJob(jobId).addOnSuccessListener(dataSnapshot -> {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Application application = snapshot.getValue(Application.class);
                if (application != null) {
                    applicationList.add(application);
                }
            }

            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);

            if (applicationList.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                applicationsRecyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                applicationsRecyclerView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error loading applications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void onStatusChange(Application application, String newStatus) {
        progressBar.setVisibility(View.VISIBLE);

        applicationRepository.updateApplicationStatus(application.getApplicationId(), newStatus)
                .addOnSuccessListener(aVoid -> {
                    application.setStatus(newStatus);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Application " + newStatus, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

