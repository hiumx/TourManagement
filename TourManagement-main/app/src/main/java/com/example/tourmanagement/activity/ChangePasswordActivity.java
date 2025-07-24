package com.example.tourmanagement.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tourmanagement.R;
import com.example.tourmanagement.database.TourManagementDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for mandatory password change after password reset.
 * Forces users to create a new password when logging in with temporary password.
 *
 * Features:
 * - Secure password validation
 * - Password confirmation
 * - Update user password in database
 * - Clear password change requirement
 * - Navigate to dashboard after successful change
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword;
    private TourManagementDatabase database;
    private ExecutorService executorService;
    private SharedPreferences sharedPreferences;
    private int userId;

    // SharedPreferences constants
    private static final String PREF_NAME = "TourManagementPrefs";
    private static final String KEY_USER_ID = "user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize components
        initializeViews();
        database = TourManagementDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Get user ID from preferences
        userId = sharedPreferences.getInt(KEY_USER_ID, -1);
        if (userId == -1) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        setupEventListeners();

        // Handle back button press - prevent going back to login screen
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show a message indicating password change is mandatory
                Toast.makeText(ChangePasswordActivity.this,
                        "You must change your password to continue", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePasswordChange();
            }
        });
    }

    /**
     * Handle password change request
     */
    private void handlePasswordChange() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate input
        if (!validatePasswordInput(currentPassword, newPassword, confirmPassword)) {
            return;
        }

        // Disable button during processing
        btnChangePassword.setEnabled(false);

        // Process password change
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                processPasswordChange(currentPassword, newPassword);
            }
        });
    }

    /**
     * Validate password input
     */
    private boolean validatePasswordInput(String currentPassword, String newPassword, String confirmPassword) {
        // Check if fields are empty
        if (currentPassword.isEmpty()) {
            etCurrentPassword.setError("Current password is required");
            etCurrentPassword.requestFocus();
            return false;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("New password is required");
            etNewPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm your new password");
            etConfirmPassword.requestFocus();
            return false;
        }

        // Check password strength
        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters long");
            etNewPassword.requestFocus();
            return false;
        }

        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        // Check if new password is different from current
        if (newPassword.equals(currentPassword)) {
            etNewPassword.setError("New password must be different from current password");
            etNewPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Process the password change
     */
    private void processPasswordChange(String currentPassword, String newPassword) {
        try {
            // Verify current password
            com.example.tourmanagement.model.User user = database.userDao().getUserById(userId);

            if (user == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChangePasswordActivity.this,
                                "User not found. Please login again.", Toast.LENGTH_LONG).show();
                        redirectToLogin();
                    }
                });
                return;
            }

            if (!user.getPassword().equals(currentPassword)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnChangePassword.setEnabled(true);
                        etCurrentPassword.setError("Current password is incorrect");
                        etCurrentPassword.requestFocus();
                    }
                });
                return;
            }

            // Update password and clear change requirement
            database.userDao().updateUserPassword(userId, newPassword, false, System.currentTimeMillis());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Password changed successfully!", Toast.LENGTH_LONG).show();

                    // Navigate to dashboard
                    Intent intent = new Intent(ChangePasswordActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });

        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnChangePassword.setEnabled(true);
                    Toast.makeText(ChangePasswordActivity.this,
                            "An error occurred while changing password. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Redirect to login activity
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
