package com.example.joblink.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

public class MyApplicationsActivity extends AppCompatActivity {
    private RecyclerView applicationsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private Spinner statusFilterSpinner;

    private FirebaseHelper firebaseHelper;
    private ApplicationRepository applicationRepository;
    private ApplicationAdapter adapter;
    private List<Application> applicationList;
    private List<Application> filteredList;
    private String currentFilter = "All";
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_applications);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Applications");
        }

        initializeViews();
        firebaseHelper = FirebaseHelper.getInstance();
        applicationRepository = new ApplicationRepository();

        applicationList = new ArrayList<>();
        filteredList = new ArrayList<>();
        setupRecyclerView();
        setupFilterSpinner();
        loadApplications();
    }

    private void initializeViews() {
        applicationsRecyclerView = findViewById(R.id.applicationsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        statusFilterSpinner = findViewById(R.id.statusFilterSpinner);
    }

    private void setupRecyclerView() {
        applicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicationAdapter(filteredList, false, null);
        applicationsRecyclerView.setAdapter(adapter);
    }

    private void setupFilterSpinner() {
        String[] filters = {"All", "Pending", "Accepted", "Rejected"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, filters);
        statusFilterSpinner.setAdapter(spinnerAdapter);

        statusFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filters[position];
                filterApplications();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadApplications() {
        // Prevent multiple simultaneous loads
        if (isLoading) {
            android.util.Log.d("MyApplications", "Already loading, skipping duplicate call");
            return;
        }

        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        applicationList.clear();

        String userId = firebaseHelper.getCurrentUserId();
        List<String> addedApplicationIds = new ArrayList<>();

        applicationRepository.getApplicationsByUser(userId).addOnSuccessListener(dataSnapshot -> {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Application application = snapshot.getValue(Application.class);
                if (application != null && application.getApplicationId() != null) {
                    if (!addedApplicationIds.contains(application.getApplicationId())) {
                        applicationList.add(application);
                        addedApplicationIds.add(application.getApplicationId());
                    }
                }
            }

            android.util.Log.d("MyApplications", "Applications loaded: " + applicationList.size());
            filterApplications();
            progressBar.setVisibility(View.GONE);
            isLoading = false;
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            isLoading = false;
            Toast.makeText(this, "Error loading applications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void filterApplications() {
        filteredList.clear();

        if (currentFilter.equals("All")) {
            filteredList.addAll(applicationList);
        } else {
            String status = currentFilter.toLowerCase();
            for (Application app : applicationList) {
                if (app.getStatus().equalsIgnoreCase(status)) {
                    filteredList.add(app);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            applicationsRecyclerView.setVisibility(View.GONE);
            emptyView.setText(currentFilter.equals("All") ?
                    "No applications yet" : "No " + currentFilter.toLowerCase() + " applications");
        } else {
            emptyView.setVisibility(View.GONE);
            applicationsRecyclerView.setVisibility(View.VISIBLE);
        }
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
        loadApplications();
    }
}

