package com.example.joblink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joblink.R;
import com.example.joblink.models.User;
import com.example.joblink.repositories.UserRepository;
import com.example.joblink.utils.FirebaseHelper;


public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> checkAuthState(), SPLASH_DELAY);
    }

    private void checkAuthState() {
        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        String uid = firebaseHelper.getCurrentUserId();

        if (uid != null && firebaseHelper.isUserLoggedIn()) {
            // User is logged in, check their role and redirect accordingly
            UserRepository userRepository = new UserRepository();

            userRepository.getUserById(uid).addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        navigateToDashboard(user.getAccountType());
                    } else {
                        navigateToLogin();
                    }
                } else {
                    navigateToLogin();
                }
            }).addOnFailureListener(e -> navigateToLogin());
        } else {
            navigateToLogin();
        }
    }

    private void navigateToDashboard(String accountType) {
        Intent intent;
        switch (accountType) {
            case User.ACCOUNT_TYPE_ADMIN:
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case User.ACCOUNT_TYPE_EMPLOYER:
                intent = new Intent(this, EmployerDashboardActivity.class);
                break;
            case User.ACCOUNT_TYPE_SEEKER:
            default:
                intent = new Intent(this, SeekerDashboardActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

