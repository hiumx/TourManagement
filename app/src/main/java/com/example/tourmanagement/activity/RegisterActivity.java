package com.example.tourmanagement.activity;

import android.content.Intent;
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
 * Registration Activity for new user account creation in the tour management system.
 *
 * Features:
 * - Complete user registration form
 * - Input validation and error handling
 * - Duplicate username/email checking
 * - Automatic login after successful registration
 * - Navigation back to login screen
 *
 * UI Components:
 * - Full name input field
 * - Username input field
 * - Email input field
 * - Phone number input field
 * - Password input field
 * - Confirm password field
 * - Address input field
 * - Register button
 * - Back to login link
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * UI components for registration form
     */
    private EditText etFullName, etUsername, etEmail, etPhone, etPassword, etConfirmPassword, etAddress;
    private Button btnRegister;
    private TextView tvLogin;

    /**
     * Database instance for user registration
     */
    private TourManagementDatabase database;

    /**
     * Called when the activity is first created.
     * Initializes UI components and sets up event listeners.
     *
     * @param savedInstanceState Bundle containing saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database
        database = TourManagementDatabase.getDatabase(this);

        // Initialize UI components
        initializeViews();

        // Set up event listeners
        setupEventListeners();
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        etFullName = findViewById(R.id.et_full_name);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etAddress = findViewById(R.id.et_address);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
    }

    /**
     * Sets up click listeners for UI components
     */
    private void setupEventListeners() {
        // Register button click listener
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        // Login link click listener
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    /**
     * Performs user registration
     * Validates input and creates new user account
     */
    private void performRegistration() {
        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validate input
        if (!validateInput(fullName, username, email, phone, password, confirmPassword)) {
            return;
        }

        // Check for existing username and email
        if (!checkUniqueCredentials(username, email)) {
            return;
        }

        // Create new user
        try {
            User newUser = new User(username, password, email, phone, fullName);
            newUser.setAddress(address);

            long userId = database.userDao().insertUser(newUser);

            if (userId > 0) {
                showToast("Registration successful! Welcome " + fullName);
                navigateToLoginWithCredentials(username);
            } else {
                showToast("Registration failed. Please try again.");
            }
        } catch (Exception e) {
            showToast("Registration error: " + e.getMessage());
        }
    }

    /**
     * Validates all user input fields
     *
     * @param fullName User's full name
     * @param username User's username
     * @param email User's email
     * @param phone User's phone number
     * @param password User's password
     * @param confirmPassword Password confirmation
     * @return true if all input is valid, false otherwise
     */
    private boolean validateInput(String fullName, String username, String email,
                                 String phone, String password, String confirmPassword) {

        // Full name validation
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }

        // Username validation
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }
        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return false;
        }

        // Email validation
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        // Phone validation
        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }
        if (phone.length() < 10) {
            etPhone.setError("Please enter a valid phone number");
            etPhone.requestFocus();
            return false;
        }

        // Password validation
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        // Confirm password validation
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Checks if username and email are unique
     *
     * @param username Username to check
     * @param email Email to check
     * @return true if both are unique, false otherwise
     */
    private boolean checkUniqueCredentials(String username, String email) {
        try {
            // Check username
            User existingUser = database.userDao().getUserByUsername(username);
            if (existingUser != null) {
                etUsername.setError("Username already exists");
                etUsername.requestFocus();
                return false;
            }

            // Check email
            User existingEmail = database.userDao().getUserByEmail(email);
            if (existingEmail != null) {
                etEmail.setError("Email already registered");
                etEmail.requestFocus();
                return false;
            }

            return true;
        } catch (Exception e) {
            showToast("Error checking credentials: " + e.getMessage());
            return false;
        }
    }

    /**
     * Navigates back to login activity
     */
    private void navigateToLogin() {
        finish(); // This will return to the previous activity (LoginActivity)
    }

    /**
     * Navigates to login activity with pre-filled username
     *
     * @param username Username to pre-fill in login form
     */
    private void navigateToLoginWithCredentials(String username) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("username", username);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Shows a toast message to the user
     *
     * @param message Message to display
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
