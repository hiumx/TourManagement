package com.example.tourmanagement.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.tourmanagement.R;

/**
 * Settings Activity for managing app preferences and configurations.
 *
 * Features:
 * - Dark/Light theme toggle
 * - App version information
 * - User preferences management
 * - Theme persistence across app sessions
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-23
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * UI components
     */
    private SwitchMaterial switchDarkMode;
    private TextView tvThemeStatus;

    /**
     * Preferences management
     */
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "TourManagementPrefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize preferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Initialize UI components
        initializeViews();

        // Load current settings
        loadCurrentSettings();

        // Setup event listeners
        setupEventListeners();
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        tvThemeStatus = findViewById(R.id.tv_theme_status);

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set app version
        TextView tvAppVersion = findViewById(R.id.tv_app_version);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText(getString(R.string.version_format, versionName));
        } catch (Exception e) {
            tvAppVersion.setText(getString(R.string.default_version));
        }
    }

    /**
     * Loads current settings from preferences
     */
    private void loadCurrentSettings() {
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(isDarkMode);
        updateThemeStatus(isDarkMode);
    }

    /**
     * Sets up event listeners for UI components
     */
    private void setupEventListeners() {
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_DARK_MODE, isChecked);
            editor.apply();

            // Apply theme change
            applyTheme(isChecked);

            // Update status text
            updateThemeStatus(isChecked);

            // Show feedback to user
            android.widget.Toast.makeText(SettingsActivity.this,
                isChecked ? getString(R.string.dark_theme_enabled) : getString(R.string.light_theme_enabled),
                android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Applies the selected theme to the application
     */
    private void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Updates the theme status text
     */
    private void updateThemeStatus(boolean isDarkMode) {
        if (isDarkMode) {
            tvThemeStatus.setText(getString(R.string.dark_theme_active));
        } else {
            tvThemeStatus.setText(getString(R.string.light_theme_active));
        }
    }

    /**
     * Handles back button press in toolbar
     */
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
