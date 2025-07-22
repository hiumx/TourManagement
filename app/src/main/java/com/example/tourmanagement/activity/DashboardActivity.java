package com.example.tourmanagement.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.tourmanagement.R;
import com.example.tourmanagement.adapter.TourAdapter;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Tour;
import com.example.tourmanagement.model.User;
import java.util.List;

/**
 * Dashboard Activity serving as the main hub for the tour management system.
 *
 * Features:
 * - Display all available tours in a RecyclerView
 * - Quick access to tour booking and details
 * - User profile and booking history access
 * - Tour search and filtering capabilities
 * - Admin tour management (CRUD operations)
 * - User logout functionality
 * - Navigation to various app sections
 *
 * UI Components:
 * - Welcome message with user name
 * - RecyclerView for tour listings
 * - Floating Action Button for adding new tours (admin)
 * - Menu bar with logout and profile options
 * - Search functionality
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class DashboardActivity extends AppCompatActivity implements TourAdapter.OnTourClickListener {

    /**
     * UI components
     */
    private TextView tvWelcome;
    private RecyclerView recyclerViewTours;
    private FloatingActionButton fabAddTour;

    /**
     * Adapter for tour listings
     */
    private TourAdapter tourAdapter;

    /**
     * Database and user session management
     */
    private TourManagementDatabase database;
    private SharedPreferences sharedPreferences;
    private User currentUser;
    private boolean isAdmin;
    private static final String PREF_NAME = "TourManagementPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final int ADMIN_USER_ID = -999;

    /**
     * Called when the activity is first created.
     * Initializes UI components, loads user data, and displays tours.
     *
     * @param savedInstanceState Bundle containing saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize database and preferences
        database = TourManagementDatabase.getDatabase(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Load current user
        loadCurrentUser();

        // Initialize UI components
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load and display tours
        loadTours();

        // Setup event listeners
        setupEventListeners();
    }

    /**
     * Loads current user information from session
     */
    private void loadCurrentUser() {
        int userId = sharedPreferences.getInt(KEY_USER_ID, -1);
        isAdmin = sharedPreferences.getBoolean(KEY_IS_ADMIN, false);

        // Debug logging
        android.util.Log.d("DashboardActivity", "Loading user - userId: " + userId + ", isAdmin: " + isAdmin);

        // Check if this is an admin session (either by userId or isAdmin flag)
        if (isAdmin || userId == ADMIN_USER_ID) {
            // Handle admin user - create a virtual admin user object
            android.util.Log.d("DashboardActivity", "Creating admin user object");
            currentUser = new User();
            currentUser.setId(ADMIN_USER_ID);
            currentUser.setUsername("admin");
            currentUser.setFullName("Administrator");
            currentUser.setEmail("admin@tourmanagement.com");
            isAdmin = true;
            android.util.Log.d("DashboardActivity", "Admin user created successfully");
        } else if (userId != -1) {
            // Handle regular user
            android.util.Log.d("DashboardActivity", "Loading regular user from database");
            currentUser = database.userDao().getUserById(userId);
            if (currentUser == null) {
                android.util.Log.e("DashboardActivity", "User not found in database, logging out");
                // User not found, logout
                logout();
                return;
            }
            isAdmin = false;
        } else {
            android.util.Log.e("DashboardActivity", "No user session found, redirecting to login");
            // No user session, redirect to login
            redirectToLogin();
        }
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        recyclerViewTours = findViewById(R.id.recycler_view_tours);
        fabAddTour = findViewById(R.id.fab_add_tour);

        // Set welcome message
        if (currentUser != null) {
            tvWelcome.setText("Welcome, " + currentUser.getFullName() + "!");
        }

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tour Dashboard");
        }
    }

    /**
     * Sets up the RecyclerView for tour listings
     */
    private void setupRecyclerView() {
        recyclerViewTours.setLayoutManager(new LinearLayoutManager(this));
        tourAdapter = new TourAdapter(this, this);
        recyclerViewTours.setAdapter(tourAdapter);
    }

    /**
     * Loads all active tours from database and displays them
     */
    private void loadTours() {
        try {
            List<Tour> tours = database.tourDao().getActiveTours();
            tourAdapter.updateTours(tours);

            if (tours.isEmpty()) {
                showToast("No tours available at the moment");
            }
        } catch (Exception e) {
            showToast("Error loading tours: " + e.getMessage());
        }
    }

    /**
     * Sets up event listeners for UI components
     */
    private void setupEventListeners() {
        // FAB click listener - only for admin users
        fabAddTour.setOnClickListener(v -> {
            if (isCurrentUserAdmin()) {
                // Navigate to add tour activity
                Intent intent = new Intent(DashboardActivity.this, AddTourActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Only admin users can add tours", Toast.LENGTH_SHORT).show();
            }
        });

        // Check admin status and configure UI
        checkAdminStatusAndConfigureUI();
    }

    /**
     * Creates options menu in the action bar
     *
     * @param menu Menu to inflate
     * @return true if menu created successfully
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    /**
     * Handles menu item selections
     *
     * @param item Selected menu item
     * @return true if item handled successfully
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            navigateToProfile();
            return true;
        } else if (id == R.id.action_booking_history) {
            navigateToBookingHistory();
            return true;
        } else if (id == R.id.action_search) {
            navigateToSearch();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles tour item clicks from RecyclerView
     *
     * @param tour Selected tour object
     */
    @Override
    public void onTourClick(Tour tour) {
        navigateToTourDetails(tour);
    }

    /**
     * Handles tour booking button clicks
     *
     * @param tour Tour to book
     */
    @Override
    public void onBookTourClick(Tour tour) {
        if (tour.hasAvailableSlots()) {
            navigateToBookTour(tour);
        } else {
            showToast("Sorry, this tour is fully booked");
        }
    }

    /**
     * Handles edit tour button clicks (admin feature)
     *
     * @param tour Tour to edit
     */
//    @Override
    public void onEditTourClick(Tour tour) {
        Intent intent = new Intent(this, AddEditTourActivity.class);
        intent.putExtra("tour_id", tour.getId());
        startActivityForResult(intent, 1001);
    }

    /**
     * Handles delete tour button clicks (admin feature)
     *
     * @param tour Tour to delete
     */
    @Override
    public void onDeleteTourClick(Tour tour) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Tour")
            .setMessage("Are you sure you want to delete '" + tour.getTourName() + "'?")
            .setPositiveButton("Delete", (dialog, which) -> {
                try {
                    database.tourDao().deleteTour(tour);
                    showToast("Tour deleted successfully");
                    loadTours(); // Refresh the list
                } catch (Exception e) {
                    showToast("Error deleting tour: " + e.getMessage());
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Navigates to tour details activity
     *
     * @param tour Tour to show details for
     */
    private void navigateToTourDetails(Tour tour) {
        Intent intent = new Intent(this, TourDetailsActivity.class);
        intent.putExtra("TOUR_ID", tour.getId());
        startActivity(intent);
    }

    /**
     * Navigates to tour booking activity
     *
     * @param tour Tour to book
     */
    private void navigateToBookTour(Tour tour) {
        Intent intent = new Intent(this, BookTourActivity.class);
        intent.putExtra("TOUR_ID", tour.getId());
        intent.putExtra("USER_ID", currentUser.getId());
        startActivity(intent);
    }

    /**
     * Navigates to add new tour activity (admin feature)
     */
    private void navigateToAddTour() {
        Intent intent = new Intent(this, AddEditTourActivity.class);
        startActivity(intent);
    }

    /**
     * Navigates to user profile activity
     */
    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user_id", currentUser.getId());
        startActivity(intent);
    }

    /**
     * Navigates to booking history activity
     */
    private void navigateToBookingHistory() {
        Intent intent = new Intent(this, BookingHistoryActivity.class);
        intent.putExtra("user_id", currentUser.getId());
        startActivity(intent);
    }

    /**
     * Navigates to tour search activity
     */
    private void navigateToSearch() {
        Intent intent = new Intent(this, SearchToursActivity.class);
        startActivity(intent);
    }

    /**
     * Performs user logout
     * Clears session data and redirects to login
     */
    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        showToast("Logged out successfully");
        redirectToLogin();
    }

    /**
     * Redirects to login activity
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Refreshes tour list when returning from other activities
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadTours(); // Refresh tours when returning to dashboard
    }

    /**
     * Check if the current user is an admin
     * @return true if current user has admin privileges, false otherwise
     */
    private boolean isCurrentUserAdmin() {
        // Check if current user is admin by username
        if (currentUser != null && "admin".equals(currentUser.getUsername())) {
            return true; // This is the admin user (username: admin, password: admin)
        }

        // Also check the admin flag or admin user ID for backward compatibility
        return isAdmin || (currentUser != null && currentUser.getId() == ADMIN_USER_ID);
    }

    /**
     * Check admin status and configure UI accordingly
     */
    private void checkAdminStatusAndConfigureUI() {
        if (isCurrentUserAdmin()) {
            fabAddTour.setVisibility(View.VISIBLE);
        } else {
            fabAddTour.setVisibility(View.GONE);
        }
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
