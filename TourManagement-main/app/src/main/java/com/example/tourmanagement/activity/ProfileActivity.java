package com.example.tourmanagement.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tourmanagement.R;
import com.example.tourmanagement.dao.UserDao;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.User;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ProfileActivity for user profile management.
 * Allows users to view and edit their profile information including:
 * - Personal details (name, email, phone, address)
 * - Account information
 * - Password change functionality
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // UI Components
    private ImageButton btnBack, btnEdit;
    private ImageView imgProfilePicture;
    private TextView tvChangePhoto, tvMemberSince, tvUserId;
    private TextInputEditText etFullName, etUsername, etEmail, etPhoneNumber, etAddress;
    private LinearLayout layoutEditMode;
    private Button btnCancel, btnSave, btnChangePassword;

    // Database and utilities
    private TourManagementDatabase database;
    private UserDao userDao;
    private SharedPreferences sharedPreferences;
    private ExecutorService executorService;

    // Session management constants (matching existing login system)
    private static final String PREF_NAME = "TourManagementPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_ADMIN = "is_admin";

    // User data
    private User currentUser;
    private boolean isEditMode = false;

    // Image handling
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeImagePicker();
        initializeComponents();
        loadUserProfile();
    }

    /**
     * Initialize the image picker launcher
     */
    private void initializeImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        handleImageSelection(imageUri);
                    }
                }
            }
        );
    }

    /**
     * Initialize all UI components and database connections
     */
    private void initializeComponents() {
        // Initialize UI components
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        imgProfilePicture = findViewById(R.id.imgProfilePicture);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        tvUserId = findViewById(R.id.tvUserId);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAddress = findViewById(R.id.etAddress);

        layoutEditMode = findViewById(R.id.layoutEditMode);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Initialize database and utilities
        database = TourManagementDatabase.getDatabase(this);
        userDao = database.userDao();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        executorService = Executors.newSingleThreadExecutor();

        // Set click listeners
        setupClickListeners();
    }

    /**
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> toggleEditMode());

        btnCancel.setOnClickListener(v -> {
            toggleEditMode();
            loadUserProfile(); // Reload original data
        });

        btnSave.setOnClickListener(v -> saveProfile());

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        tvChangePhoto.setOnClickListener(v -> {
            // Launch image picker to select photo from gallery
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    /**
     * Load user profile data from database
     */
    private void loadUserProfile() {
        int userId = sharedPreferences.getInt(KEY_USER_ID, -1);

        if (userId == -1) {
            Log.e(TAG, "No user session found");
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        executorService.execute(() -> {
            try {
                currentUser = userDao.getUserById(userId);

                runOnUiThread(() -> {
                    if (currentUser != null) {
                        populateUserData();
                    } else {
                        Log.e(TAG, "User not found in database");
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading user profile", e);
                runOnUiThread(() ->
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * Populate UI with user data
     */
    private void populateUserData() {
        etFullName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
        etUsername.setText(currentUser.getUsername() != null ? currentUser.getUsername() : "");
        etEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        etPhoneNumber.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
        etAddress.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");

        // Format and display member since date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        String memberSince = dateFormat.format(new Date(currentUser.getCreatedAt()));
        tvMemberSince.setText(memberSince);

        // Display user ID
        tvUserId.setText(String.valueOf(currentUser.getId()));

        // Load profile image
        loadProfileImage();
    }

    /**
     * Toggle between view and edit mode
     */
    private void toggleEditMode() {
        isEditMode = !isEditMode;

        // Enable/disable edit texts
        etFullName.setEnabled(isEditMode);
        etEmail.setEnabled(isEditMode);
        etPhoneNumber.setEnabled(isEditMode);
        etAddress.setEnabled(isEditMode);

        // Show/hide edit mode buttons
        layoutEditMode.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        // Show/hide change photo option
        tvChangePhoto.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        // Update edit button icon (you can change this to different icons)
        // btnEdit.setImageResource(isEditMode ? R.drawable.ic_close : R.drawable.ic_edit);
    }

    /**
     * Save profile changes to database
     */
    private void saveProfile() {
        if (!validateInput()) {
            return;
        }

        // Update user object with new data
        currentUser.setFullName(etFullName.getText().toString().trim());
        currentUser.setEmail(etEmail.getText().toString().trim());
        currentUser.setPhoneNumber(etPhoneNumber.getText().toString().trim());
        currentUser.setAddress(etAddress.getText().toString().trim());

        executorService.execute(() -> {
            try {
                // Check if email is already taken by another user
                User existingUser = userDao.getUserByEmail(currentUser.getEmail());
                if (existingUser != null && existingUser.getId() != currentUser.getId()) {
                    runOnUiThread(() ->
                        Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                userDao.updateUser(currentUser);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    toggleEditMode();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error updating profile", e);
                runOnUiThread(() ->
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * Validate user input
     */
    private boolean validateInput() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            etPhoneNumber.setError("Phone number is required");
            etPhoneNumber.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Show change password dialog
     */
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        Button btnCancelPassword = dialogView.findViewById(R.id.btnCancelPassword);
        Button btnUpdatePassword = dialogView.findViewById(R.id.btnUpdatePassword);

        btnCancelPassword.setOnClickListener(v -> dialog.dismiss());

        btnUpdatePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validatePasswordInput(currentPassword, newPassword, confirmPassword)) {
                updatePassword(currentPassword, newPassword, dialog);
            }
        });

        dialog.show();
    }

    /**
     * Validate password input
     */
    private boolean validatePasswordInput(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, "Please enter current password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Please enter new password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!currentPassword.equals(currentUser.getPassword())) {
            Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Update user password in database
     */
    private void updatePassword(String currentPassword, String newPassword, AlertDialog dialog) {
        executorService.execute(() -> {
            try {
                userDao.updatePassword(currentUser.getId(), newPassword);
                currentUser.setPassword(newPassword); // Update local object

                runOnUiThread(() -> {
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error updating password", e);
                runOnUiThread(() ->
                    Toast.makeText(this, "Error updating password", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * Redirect to login activity
     */
    private void redirectToLogin() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Handle the selected image from gallery
     */
    private void handleImageSelection(Uri imageUri) {
        try {
            // Save the image to internal storage
            String savedImagePath = saveImageToInternalStorage(imageUri);

            if (savedImagePath != null) {
                // Update the current user's profile image path
                currentUser.setProfileImagePath(savedImagePath);

                // Load the image into the ImageView
                loadProfileImage();

                Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling image selection", e);
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save selected image to internal storage
     */
    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            // Create a unique filename for the profile image
            String fileName = "profile_" + currentUser.getId() + "_" + System.currentTimeMillis() + ".jpg";

            // Get the internal storage directory for profile images
            File profileImagesDir = new File(getFilesDir(), "profile_images");
            if (!profileImagesDir.exists()) {
                profileImagesDir.mkdirs();
            }

            File imageFile = new File(profileImagesDir, fileName);

            // Copy the image from URI to internal storage
            try (java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);
                 FileOutputStream outputStream = new FileOutputStream(imageFile)) {

                if (inputStream != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }

            return imageFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error saving image to internal storage", e);
            return null;
        }
    }

    /**
     * Load profile image using Glide
     */
    private void loadProfileImage() {
        if (currentUser != null && !TextUtils.isEmpty(currentUser.getProfileImagePath())) {
            File imageFile = new File(currentUser.getProfileImagePath());
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .into(imgProfilePicture);
            } else {
                // Set default image if file doesn't exist
                imgProfilePicture.setImageResource(R.drawable.ic_default_profile);
            }
        } else {
            // Set default image if no profile image path
            imgProfilePicture.setImageResource(R.drawable.ic_default_profile);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
