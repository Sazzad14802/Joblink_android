package com.example.joblink.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.joblink.R;
import com.example.joblink.models.Job;
import com.example.joblink.models.User;
import com.example.joblink.repositories.JobRepository;
import com.example.joblink.repositories.UserRepository;
import com.example.joblink.utils.FirebaseHelper;

public class PostJobFragment extends Fragment {
    private EditText titleInput, descriptionInput, salaryInput, locationInput;
    private Button postJobButton;
    private ProgressBar progressBar;
    
    private JobRepository jobRepository;
    private UserRepository userRepository;
    private FirebaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_job, container, false);
        
        initializeViews(view);
        jobRepository = new JobRepository();
        userRepository = new UserRepository();
        firebaseHelper = FirebaseHelper.getInstance();
        
        setupClickListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        titleInput = view.findViewById(R.id.titleInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        salaryInput = view.findViewById(R.id.salaryInput);
        locationInput = view.findViewById(R.id.locationInput);
        postJobButton = view.findViewById(R.id.postJobButton);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        postJobButton.setOnClickListener(v -> postJob());
    }

    private void postJob() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String salaryStr = salaryInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || salaryStr.isEmpty() || location.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid salary format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prevent duplicate submissions
        if (!postJobButton.isEnabled()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        postJobButton.setEnabled(false);

        String employerId = firebaseHelper.getCurrentUserId();

        // Fetch employer name first, then create job
        userRepository.getUserById(employerId).addOnSuccessListener(dataSnapshot -> {
            if (getActivity() != null) {
                String employerName = "Unknown";
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getName() != null) {
                        employerName = user.getName();
                    }
                }

                Job job = new Job();
                job.setTitle(title);
                job.setDescription(description);
                job.setSalary(salary);
                job.setLocation(location);
                job.setPostedBy(employerId);
                job.setPostedByName(employerName);
                job.setPostedDate(System.currentTimeMillis());
                job.setApplicationCount(0);

                jobRepository.createJob(job)
                        .addOnSuccessListener(aVoid -> {
                            if (getActivity() != null) {
                                progressBar.setVisibility(View.GONE);
                                postJobButton.setEnabled(true);
                                Toast.makeText(getContext(), "Job posted successfully!", Toast.LENGTH_SHORT).show();
                                clearFields();
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (getActivity() != null) {
                                progressBar.setVisibility(View.GONE);
                                postJobButton.setEnabled(true);
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(e -> {
            if (getActivity() != null) {
                progressBar.setVisibility(View.GONE);
                postJobButton.setEnabled(true);
                Toast.makeText(getContext(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        titleInput.setText("");
        descriptionInput.setText("");
        salaryInput.setText("");
        locationInput.setText("");
    }
}

