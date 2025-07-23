package com.example.tourmanagement.utils;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Helper class for managing app themes and theme transitions.
 * Provides centralized theme management across the application.
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-23
 */
public class ThemeHelper {

    /**
     * Applies the selected theme to the application
     *
     * @param isDarkMode true for dark theme, false for light theme
     */
    public static void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Applies the theme based on system settings
     */
    public static void applySystemTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    /**
     * Gets the current night mode setting
     *
     * @return Current night mode constant
     */
    public static int getCurrentNightMode() {
        return AppCompatDelegate.getDefaultNightMode();
    }

    /**
     * Checks if dark mode is currently active
     *
     * @return true if dark mode is active, false otherwise
     */
    public static boolean isDarkModeActive() {
        return getCurrentNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
    }
}
