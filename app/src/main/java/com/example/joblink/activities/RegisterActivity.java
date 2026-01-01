package com.example.joblink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.R;
import com.example.joblink.models.User;
import com.example.joblink.repositories.UserRepository;
import com.example.joblink.utils.FirebaseHelper;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private TextInputLayout nameInputLayout;
    private Spinner accountTypeSpinner;
    private Button registerButton;
    private TextView loginLink;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupSpinner();

        firebaseHelper = FirebaseHelper.getInstance();
        userRepository = new UserRepository();

        registerButton.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        accountTypeSpinner = findViewById(R.id.accountTypeSpinner);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinner() {
        String[] accountTypes = {"Job Seeker", "Employer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, accountTypes);
        accountTypeSpinner.setAdapter(adapter);

        // Change hint based on account type selection
        accountTypeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedType = accountTypes[position];
                if (selectedType.equals("Employer")) {
                    nameInputLayout.setHint("Company Name");
                } else {
                    nameInputLayout.setHint("Full Name");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                nameInputLayout.setHint("Full Name");
            }
        });
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String accountTypeDisplay = accountTypeSpinner.getSelectedItem().toString();

        Log.d(TAG, "Starting registration for: " + email);

        // Validation
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        // Convert display account type to database format
        String accountType = accountTypeDisplay.equals("Job Seeker") ?
                User.ACCOUNT_TYPE_SEEKER : User.ACCOUNT_TYPE_EMPLOYER;

        Log.d(TAG, "Account type: " + accountType);

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        // Check if email already exists
        userRepository.getUserByEmail(email).addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                // Email already registered
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this,
                        "Email already registered. Please login.", Toast.LENGTH_LONG).show();
            } else {
                // Create new user
                String uid = email.replace(".", "_").replace("@", "_at_"); // Simple UID from email
                User user = new User(uid, name, email, password, accountType, System.currentTimeMillis());

                userRepository.saveUser(user).addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User registered successfully");
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                            "Registration successful!", Toast.LENGTH_SHORT).show();
                    
                    // Set current user
                    firebaseHelper.setCurrentUserId(uid);
                    navigateToDashboard(accountType);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to register user", e);
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                            "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to check email", e);
            progressBar.setVisibility(View.GONE);
            registerButton.setEnabled(true);
            Toast.makeText(RegisterActivity.this,
                    "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void navigateToDashboard(String accountType) {
        Intent intent;
        if (accountType.equals(User.ACCOUNT_TYPE_EMPLOYER)) {
            intent = new Intent(this, EmployerDashboardActivity.class);
        } else {
            intent = new Intent(this, SeekerDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}

