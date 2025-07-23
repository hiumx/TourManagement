package com.example.tourmanagement;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Custom Application class for Tour Management app.
 * Handles app-wide initialization including theme setup.
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-23
 */
public class TourManagementApplication extends Application {

    private static final String PREF_NAME = "TourManagementPrefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize theme based on saved preference
        initializeTheme();
    }

    /**
     * Initializes the app theme based on user preferences
     */
    private void initializeTheme() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean(KEY_DARK_MODE, false);

        // Apply the saved theme preference
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
