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
        loadSettings();

        // Setup event listeners
        setupEventListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize switches and text views
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        tvThemeStatus = findViewById(R.id.tv_theme_status);

        // Set app version safely
        TextView tvAppVersion = findViewById(R.id.tv_app_version);
        if (tvAppVersion != null) {
            try {
                String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                tvAppVersion.setText("Version " + versionName);
            } catch (Exception e) {
                tvAppVersion.setText("Version 1.0.0");
            }
        }
    }

    /**
     * Load current settings from SharedPreferences
     */
    private void loadSettings() {
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(isDarkMode);
        updateThemeStatus(isDarkMode);
    }

    /**
     * Setup event listeners for UI components
     */
    private void setupEventListeners() {
        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save preference
                sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();

                // Update theme status text
                updateThemeStatus(isChecked);

                // Apply theme change
                applyTheme(isChecked);
            }
        });
    }

    /**
     * Update theme status text
     */
    private void updateThemeStatus(boolean isDarkMode) {
        if (isDarkMode) {
            tvThemeStatus.setText("Dark theme is currently active");
        } else {
            tvThemeStatus.setText("Light theme is currently active");
        }
    }

    /**
     * Apply theme change immediately
     */
    private void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Handles back button press in toolbar
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
