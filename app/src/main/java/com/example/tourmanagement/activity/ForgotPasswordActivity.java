package com.example.tourmanagement.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tourmanagement.R;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.User;
import com.example.tourmanagement.utils.EmailService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for handling forgot password functionality.
 * Allows users to reset their password by entering their email address.
 *
 * Features:
 * - Email validation and user lookup
 * - Generate temporary password
 * - Send password reset email
 * - Update user record with temporary password
 * - Progress indication during email sending
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendReset;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;
    private TourManagementDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize components
        initializeViews();
        database = TourManagementDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        setupEventListeners();
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        etEmail = findViewById(R.id.et_email);
        btnSendReset = findViewById(R.id.btn_send_reset);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        btnSendReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleForgotPassword();
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Return to login activity
            }
        });
    }

    /**
     * Handle forgot password request
     */
    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();

        // Validate email input
        if (!validateEmail(email)) {
            return;
        }

        // Show progress
        showProgress(true);

        // Process forgot password request
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                processForgotPasswordRequest(email);
            }
        });
    }

    /**
     * Validate email input
     */
    private boolean validateEmail(String email) {
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

        return true;
    }

    /**
     * Process the forgot password request
     */
    private void processForgotPasswordRequest(String email) {
        try {
            // Find user by email
            User user = database.userDao().getUserByEmail(email);

            if (user == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                        Toast.makeText(ForgotPasswordActivity.this,
                                "No account found with this email address", Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }

            // Generate temporary password
            String temporaryPassword = EmailService.generateRandomPassword(8);

            // Update user with temporary password
            long resetTimestamp = System.currentTimeMillis();
            database.userDao().updateUserPassword(user.getId(), temporaryPassword, true, resetTimestamp);

            // Send email with temporary password
            EmailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFullName(),
                temporaryPassword,
                new EmailService.EmailCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "Password reset email sent successfully! Please check your email.",
                                        Toast.LENGTH_LONG).show();
                                finish(); // Return to login screen
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "Failed to send email: " + error + "\nPlease try again later.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            );

        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgress(false);
                    Toast.makeText(ForgotPasswordActivity.this,
                            "An error occurred. Please try again later.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Show/hide progress indicator
     */
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSendReset.setEnabled(!show);
        etEmail.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
