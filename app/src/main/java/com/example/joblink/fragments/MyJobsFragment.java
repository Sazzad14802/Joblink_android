package com.example.joblink.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblink.R;
import com.example.joblink.activities.EditJobActivity;
import com.example.joblink.activities.JobDetailsActivity;
import com.example.joblink.adapters.JobAdapter;
import com.example.joblink.models.Job;
import com.example.joblink.repositories.JobRepository;
import com.example.joblink.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MyJobsFragment extends Fragment {
    private RecyclerView jobsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;

    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private JobRepository jobRepository;
    private FirebaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_jobs, container, false);

        initializeViews(view);
        jobRepository = new JobRepository();
        firebaseHelper = FirebaseHelper.getInstance();

        setupRecyclerView();
        loadJobs();

        return view;
    }

    private void initializeViews(View view) {
        jobsRecyclerView = view.findViewById(R.id.jobsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
    }

    private void setupRecyclerView() {
        jobList = new ArrayList<>();

        jobAdapter = new JobAdapter(
            jobList,
            true, // isOwner
            job -> {
                // OnJobClickListener - Open job details
                Intent intent = new Intent(getContext(), JobDetailsActivity.class);
                intent.putExtra("JOB_ID", job.getJobId());
                intent.putExtra("IS_OWNER", true);
                startActivity(intent);
            },
            job -> {
                // OnJobEditListener - Open EditJobActivity for editing
                Intent intent = new Intent(getContext(), EditJobActivity.class);
                intent.putExtra("JOB_ID", job.getJobId());
                startActivity(intent);
            },
            job -> {
                // OnJobDeleteListener
                showDeleteConfirmation(job);
            }
        );

        jobsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        jobsRecyclerView.setAdapter(jobAdapter);
    }

    private void showDeleteConfirmation(Job job) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Job")
                .setMessage("Are you sure you want to delete this job posting?")
                .setPositiveButton("Delete", (dialog, which) -> deleteJob(job))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteJob(Job job) {
        jobRepository.deleteJob(job.getJobId())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Job deleted successfully", Toast.LENGTH_SHORT).show();
                    loadJobs();
                })
                .addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Error deleting job: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadJobs() {
        progressBar.setVisibility(View.VISIBLE);
        String employerId = firebaseHelper.getCurrentUserId();
        List<String> addedJobIds = new ArrayList<>(); // Track added job IDs to prevent duplicates
        List<String> addedJobSignatures = new ArrayList<>(); // Track content signatures

        jobRepository.getJobsByEmployer(employerId)
                .addOnSuccessListener(dataSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    jobList.clear();

                    for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Job job = snapshot.getValue(Job.class);
                        if (job != null && job.getJobId() != null) {
                            // Create a unique signature based on content
                            String jobSignature = job.getTitle() + "|" + job.getSalary() + "|" +
                                                job.getLocation() + "|" + job.getPostedDate();

                            // Check if we haven't already added this job (by ID or content)
                            if (!addedJobIds.contains(job.getJobId())
                                && !addedJobSignatures.contains(jobSignature)) {
                                jobList.add(job);
                                addedJobIds.add(job.getJobId());
                                addedJobSignatures.add(jobSignature);
                            }
                        }
                    }

                    if (jobList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        jobsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        jobsRecyclerView.setVisibility(View.VISIBLE);
                        jobAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading jobs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadJobs();
    }
}

