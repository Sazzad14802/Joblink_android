package com.example.joblink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.R;
import com.example.joblink.models.User;
import com.example.joblink.repositories.UserRepository;
import com.example.joblink.utils.FirebaseHelper;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        firebaseHelper = FirebaseHelper.getInstance();
        userRepository = new UserRepository();

        loginButton.setOnClickListener(v -> loginUser());
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        // Get user by email from Realtime Database
        userRepository.getUserByEmail(email).addOnSuccessListener(dataSnapshot -> {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);

            if (dataSnapshot.exists()) {
                // Get the first (and should be only) user with this email
                User user = null;
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    user = snapshot.getValue(User.class);
                    break;
                }

                if (user != null) {
                    // Check password (plain text comparison)
                    if (user.getPassword() != null && user.getPassword().equals(password)) {
                        // Login successful
                        Toast.makeText(LoginActivity.this,
                                "Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
                        
                        // Set current user
                        firebaseHelper.setCurrentUserId(user.getUid());
                        navigateToDashboard(user.getAccountType());
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Invalid password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                            "User not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this,
                        "No account found with this email", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            Toast.makeText(LoginActivity.this,
                    "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void navigateToDashboard(String accountType) {
        // Additional safety check
        if (accountType == null || accountType.isEmpty()) {
            Log.e("LoginActivity", "navigateToDashboard called with null/empty accountType");
            Toast.makeText(this, "Error: Invalid account type", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent;
        Log.d("LoginActivity", "Navigating to dashboard for accountType: " + accountType);

        switch (accountType) {
            case User.ACCOUNT_TYPE_ADMIN:
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case User.ACCOUNT_TYPE_EMPLOYER:
                intent = new Intent(this, EmployerDashboardActivity.class);
                break;
            case User.ACCOUNT_TYPE_SEEKER:
                intent = new Intent(this, SeekerDashboardActivity.class);
                break;
            default:
                Log.e("LoginActivity", "Unknown account type: " + accountType);
                Toast.makeText(this, "Error: Unknown account type - " + accountType, Toast.LENGTH_LONG).show();
                return;
        }
        startActivity(intent);
        finish();
    }
}

