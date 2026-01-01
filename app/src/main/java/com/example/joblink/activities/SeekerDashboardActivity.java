package com.example.joblink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.R;
import com.example.joblink.models.User;
import com.example.joblink.repositories.UserRepository;
import com.example.joblink.utils.FirebaseHelper;
import com.google.android.material.card.MaterialCardView;

public class SeekerDashboardActivity extends AppCompatActivity {
    private TextView welcomeText, emailText;
    private MaterialCardView browseJobsCard, myApplicationsCard;

    private FirebaseHelper firebaseHelper;
    private UserRepository userRepository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeker_dashboard);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViews();
        firebaseHelper = FirebaseHelper.getInstance();
        userRepository = new UserRepository();

        loadUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        emailText = findViewById(R.id.emailText);
        browseJobsCard = findViewById(R.id.browseJobsCard);
        myApplicationsCard = findViewById(R.id.myApplicationsCard);
    }

    private void loadUserData() {
        String uid = firebaseHelper.getCurrentUserId();
        userRepository.getUserById(uid).addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {
                    welcomeText.setText("Welcome, " + currentUser.getName() + "!");
                    emailText.setText(currentUser.getEmail());
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners() {
        browseJobsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, BrowseJobsActivity.class);
            startActivity(intent);
        });

        myApplicationsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyApplicationsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    firebaseHelper.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}

