package com.example.tourmanagement.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tourmanagement.R;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.User;

/**
 * Login Activity for user authentication in the tour management system.
 *
 * Features:
 * - User login with username and password
 * - Input validation and error handling
 * - Session management with SharedPreferences
 * - Navigation to registration and main dashboard
 * - Remember user login state
 *
 * UI Components:
 * - Username input field
 * - Password input field
 * - Login button
 * - Register link
 * - Forgot password option
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * UI components for login form
     */
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;

    /**
     * Database instance for user authentication
     */
    private TourManagementDatabase database;

    /**
     * SharedPreferences for session management
     */
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "TourManagementPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_ADMIN = "is_admin";

    // Admin credentials
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    /**
     * Called when the activity is first created.
     * Initializes UI components and sets up event listeners.
     *
     * @param savedInstanceState Bundle containing saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database and shared preferences
        database = TourManagementDatabase.getDatabase(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Check if user is already logged in
        checkLoginStatus();

        // Initialize UI components
        initializeViews();

        // Set up event listeners
        setupEventListeners();
    }

    /**
     * Checks if user is already logged in and redirects to dashboard
     */
    private void checkLoginStatus() {
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (isLoggedIn) {
            navigateToDashboard();
        }
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    /**
     * Sets up click listeners for UI components
     */
    private void setupEventListeners() {
        // Login button click listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        // Register link click listener
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRegister();
            }
        });

        // Forgot password click listener
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleForgotPassword();
            }
        });
    }

    /**
     * Performs user login authentication
     * Validates input and authenticates user against database
     */
    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Input validation
        if (!validateInput(username, password)) {
            return;
        }

        // Check for admin login first
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            // Admin login successful
            saveAdminSession();
            showToast("Admin login successful! Welcome Administrator");
            navigateToDashboard();
            return;
        }

        // Authenticate regular user
        try {
            User user = database.userDao().authenticateUser(username, password);

            if (user != null) {
                // Login successful
                saveUserSession(user.getId());
                showToast("Login successful! Welcome " + user.getFullName());
                navigateToDashboard();
            } else {
                // Login failed
                showToast("Invalid username or password");
                clearPasswordField();
            }
        } catch (Exception e) {
            showToast("Login error: " + e.getMessage());
        }
    }

    /**
     * Validates user input for login
     *
     * @param username User's username
     * @param password User's password
     * @return true if input is valid, false otherwise
     */
    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        // For admin, we allow shorter usernames and passwords
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            return true;
        }

        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return false;
        }

        // Password length validation removed to allow admin password "admin" (5 chars)
        return true;
    }

    /**
     * Saves user session information in SharedPreferences
     *
     * @param userId User ID to save in session
     */
    private void saveUserSession(int userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_IS_ADMIN, false);
        editor.apply();
    }

    /**
     * Saves admin session information in SharedPreferences
     */
    private void saveAdminSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, -999); // Special admin user ID
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_IS_ADMIN, true);
        editor.apply();

        // Debug logging
        android.util.Log.d("LoginActivity", "Admin session saved - userId: -999, isAdmin: true");
    }

    /**
     * Navigates to the main dashboard activity
     */
    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to the registration activity
     */
    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Handles forgot password functionality
     * Shows a simple dialog or navigates to password reset
     */
    private void handleForgotPassword() {
        // For now, show a simple message
        // In a real app, this would navigate to password reset flow
        showToast("Please contact support for password reset");
    }

    /**
     * Clears the password field for security
     */
    private void clearPasswordField() {
        etPassword.setText("");
    }

    /**
     * Shows a toast message to the user
     *
     * @param message Message to display
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when activity is resumed
     * Clears password field for security
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Clear password field when returning to login
        if (etPassword != null) {
            etPassword.setText("");
        }
    }
}
