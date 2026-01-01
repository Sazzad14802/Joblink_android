package com.example.joblink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblink.R;
import com.example.joblink.models.Job;
import com.example.joblink.models.User;
import com.example.joblink.repositories.UserRepository;

import java.util.List;
import java.util.Locale;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {
    private List<Job> jobList;
    private boolean isOwner;
    private OnJobClickListener clickListener;
    private OnJobEditListener editListener;
    private OnJobDeleteListener deleteListener;
    private UserRepository userRepository;

    public interface OnJobClickListener {
        void onJobClick(Job job);
    }

    public interface OnJobEditListener {
        void onEditJob(Job job);
    }

    public interface OnJobDeleteListener {
        void onDeleteJob(Job job);
    }

    public JobAdapter(List<Job> jobList, boolean isOwner, OnJobClickListener clickListener,
                      OnJobEditListener editListener, OnJobDeleteListener deleteListener) {
        this.jobList = jobList;
        this.isOwner = isOwner;
        this.clickListener = clickListener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.userRepository = new UserRepository();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.bind(job);
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    class JobViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, companyText, salaryText, locationText, applicationsText;
        Button editButton, deleteButton;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            companyText = itemView.findViewById(R.id.companyText);
            salaryText = itemView.findViewById(R.id.salaryText);
            locationText = itemView.findViewById(R.id.locationText);
            applicationsText = itemView.findViewById(R.id.applicationsText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Job job) {
            titleText.setText(job.getTitle());

            // Handle null postedByName by fetching from database
            if (job.getPostedByName() != null && !job.getPostedByName().isEmpty()) {
                companyText.setText(job.getPostedByName());
            } else {
                companyText.setText("Loading...");
                // Fetch employer name from database
                if (job.getPostedBy() != null) {
                    userRepository.getUserById(job.getPostedBy()).addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null && user.getName() != null) {
                                companyText.setText(user.getName());
                                job.setPostedByName(user.getName()); // Cache it for future use
                            } else {
                                companyText.setText("Unknown Employer");
                            }
                        } else {
                            companyText.setText("Unknown Employer");
                        }
                    }).addOnFailureListener(e -> {
                        companyText.setText("Unknown Employer");
                    });
                } else {
                    companyText.setText("Unknown Employer");
                }
            }

            salaryText.setText("$" + String.format(Locale.US, "%,.2f", job.getSalary()) + "/year");
            locationText.setText(job.getLocation());

            if (isOwner) {
                applicationsText.setVisibility(View.VISIBLE);
                applicationsText.setText(job.getApplicationCount() + " applications");
                editButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);

                editButton.setOnClickListener(v -> {
                    if (editListener != null) {
                        editListener.onEditJob(job);
                    }
                });

                deleteButton.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteJob(job);
                    }
                });
            } else {
                applicationsText.setVisibility(View.GONE);
                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onJobClick(job);
                }
            });
        }
    }
}

