package com.example.tourmanagement.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager utility class for managing user session data.
 * Handles user login/logout state and stores user information locally.
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class SessionManager {

    private static final String PREF_NAME = "TourManagementSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_IS_ADMIN = "isAdmin";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    /**
     * Constructor
     * @param context Application context
     */
    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     * Create user login session
     * @param userId User ID
     * @param username Username
     * @param email User email
     * @param isAdmin Whether user is admin
     */
    public void createLoginSession(int userId, String username, String email, boolean isAdmin) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.commit();
    }

    /**
     * Check if user is logged in
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get current user ID
     * @return User ID or -1 if not logged in
     */
    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    /**
     * Get current username
     * @return Username or null if not logged in
     */
    public String getUsername() {
        return preferences.getString(KEY_USERNAME, null);
    }

    /**
     * Get current user email
     * @return User email or null if not logged in
     */
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Check if current user is admin
     * @return true if admin, false otherwise
     */
    public boolean isAdmin() {
        return preferences.getBoolean(KEY_IS_ADMIN, false);
    }

    /**
     * Clear user session (logout)
     */
    public void clearSession() {
        editor.clear();
        editor.commit();
    }

    /**
     * Update user information in session
     * @param username Updated username
     * @param email Updated email
     */
    public void updateUserInfo(String username, String email) {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_EMAIL, email);
        editor.commit();
    }
}
