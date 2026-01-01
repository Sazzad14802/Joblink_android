package com.example.joblink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblink.R;
import com.example.joblink.models.User;
import com.example.joblink.repositories.ApplicationRepository;
import com.example.joblink.repositories.JobRepository;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {
    private List<User> userList;
    private OnDeleteUserListener deleteListener;
    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;

    public interface OnDeleteUserListener {
        void onDeleteUser(User user);
    }

    public UserManagementAdapter(List<User> userList, OnDeleteUserListener deleteListener) {
        this.userList = userList;
        this.deleteListener = deleteListener;
        this.jobRepository = new JobRepository();
        this.applicationRepository = new ApplicationRepository();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, accountTypeText, statsText;
        Button deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);
            accountTypeText = itemView.findViewById(R.id.accountTypeText);
            statsText = itemView.findViewById(R.id.statsText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(User user) {
            nameText.setText(user.getName());
            emailText.setText(user.getEmail());
            accountTypeText.setText("Type: " + user.getAccountType());

            // Fetch and display stats based on account type
            if (User.ACCOUNT_TYPE_EMPLOYER.equals(user.getAccountType())) {
                // Show job post count for employers
                statsText.setText("Loading...");
                jobRepository.getJobsByEmployer(user.getUid()).addOnSuccessListener(dataSnapshot -> {
                    int jobCount = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        jobCount++;
                    }
                    statsText.setText(jobCount + " job posts");
                }).addOnFailureListener(e -> {
                    statsText.setText("0 job posts");
                });
            } else if (User.ACCOUNT_TYPE_SEEKER.equals(user.getAccountType())) {
                // Show application count for seekers
                statsText.setText("Loading...");
                applicationRepository.getApplicationsByUser(user.getUid()).addOnSuccessListener(dataSnapshot -> {
                    int appCount = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        appCount++;
                    }
                    statsText.setText(appCount + " applications");
                }).addOnFailureListener(e -> {
                    statsText.setText("0 applications");
                });
            } else {
                // Admin or other types
                statsText.setText("Admin Account");
            }

            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteUser(user);
                }
            });
        }
    }
}

