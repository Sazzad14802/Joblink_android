package com.example.joblink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblink.R;
import com.example.joblink.models.Application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
    private List<Application> applicationList;
    private boolean isEmployerView;
    private OnStatusChangeListener statusChangeListener;

    public interface OnStatusChangeListener {
        void onStatusChange(Application application, String newStatus);
    }

    public ApplicationAdapter(List<Application> applicationList, boolean isEmployerView,
                             OnStatusChangeListener statusChangeListener) {
        this.applicationList = applicationList;
        this.isEmployerView = isEmployerView;
        this.statusChangeListener = statusChangeListener;
    }

    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        Application application = applicationList.get(position);
        holder.bind(application);
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    class ApplicationViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitleText, userNameText, userEmailText, statusText, dateText, experienceText;
        Button acceptButton, rejectButton;

        public ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitleText = itemView.findViewById(R.id.jobTitleText);
            userNameText = itemView.findViewById(R.id.userNameText);
            userEmailText = itemView.findViewById(R.id.userEmailText);
            statusText = itemView.findViewById(R.id.statusText);
            dateText = itemView.findViewById(R.id.dateText);
            experienceText = itemView.findViewById(R.id.experienceText);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }

        public void bind(Application application) {
            jobTitleText.setText(application.getJobTitle());
            userNameText.setText(application.getUserName());
            userEmailText.setText(application.getUserEmail());
            experienceText.setText("Experience: " + application.getExperience());

            // Format date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            dateText.setText("Applied on: " + sdf.format(new Date(application.getAppliedDate())));

            // Set status with color
            String status = application.getStatus();
            statusText.setText("Status: " + status.toUpperCase());

            switch (status) {
                case Application.STATUS_PENDING:
                    statusText.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                    break;
                case Application.STATUS_ACCEPTED:
                    statusText.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                    break;
                case Application.STATUS_REJECTED:
                    statusText.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                    break;
            }

            // Show action buttons only for employer and pending applications
            if (isEmployerView && status.equals(Application.STATUS_PENDING)) {
                acceptButton.setVisibility(View.VISIBLE);
                rejectButton.setVisibility(View.VISIBLE);

                acceptButton.setOnClickListener(v -> {
                    if (statusChangeListener != null) {
                        statusChangeListener.onStatusChange(application, Application.STATUS_ACCEPTED);
                    }
                });

                rejectButton.setOnClickListener(v -> {
                    if (statusChangeListener != null) {
                        statusChangeListener.onStatusChange(application, Application.STATUS_REJECTED);
                    }
                });
            } else {
                acceptButton.setVisibility(View.GONE);
                rejectButton.setVisibility(View.GONE);
            }
        }
    }
}

