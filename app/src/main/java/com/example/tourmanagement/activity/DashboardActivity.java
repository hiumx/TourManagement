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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.card.MaterialCardView;
import com.example.tourmanagement.R;
import com.example.tourmanagement.adapter.TourAdapter;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Tour;
import com.example.tourmanagement.model.User;
import com.example.tourmanagement.utils.SlidingPopupMenu;
import java.util.List;

/**
 * Dashboard Activity serving as the main hub for the tour management system.
 * Enhanced with modern Material Design UI/UX components.
 */
public class DashboardActivity extends AppCompatActivity implements TourAdapter.OnTourClickListener {

    /**
     * UI components - Enhanced with new modern elements
     */
    private TextView tvWelcome;
    private TextView tvTotalTours;
    private TextView tvUserBookings;
    private TextView tvSearchHint;
    private TextView tvViewAll;
    private RecyclerView recyclerViewTours;
    private ExtendedFloatingActionButton fabAddTour;
    private MaterialCardView searchCard;

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
     * Custom sliding popup menu
     */
    private SlidingPopupMenu slidingPopupMenu;

    /**
     * Called when the activity is first created.
     * Initializes UI components, loads user data, and displays tours.
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

        // Load dashboard statistics
        loadDashboardStats();

        // Initialize custom sliding popup menu
        initializeSlidingMenu();

        // Update admin menu visibility after menu is initialized
        updateAdminMenuVisibility();
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
     * Initializes all UI components including new modern elements
     */
    private void initializeViews() {
        // Existing components
        tvWelcome = findViewById(R.id.tv_welcome);
        recyclerViewTours = findViewById(R.id.recycler_view_tours);
        fabAddTour = findViewById(R.id.fab_add_tour);

        // New modern UI components
        tvTotalTours = findViewById(R.id.tv_total_tours);
        tvUserBookings = findViewById(R.id.tv_user_bookings);
        tvSearchHint = findViewById(R.id.tv_search_hint);
        tvViewAll = findViewById(R.id.tv_view_all);
        searchCard = findViewById(R.id.search_card);

        // Set welcome message with enhanced styling
        if (currentUser != null) {
            String welcomeMessage = isAdmin ?
                "Welcome back, Administrator!" :
                "Welcome, " + currentUser.getFullName() + "!";
            tvWelcome.setText(welcomeMessage);
        }

        // Setup toolbar with enhanced styling
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tour Dashboard");
            getSupportActionBar().setSubtitle(isAdmin ? "Admin Panel" : "Explore & Book Tours");
        }

        // Show/hide admin-only elements
        if (isCurrentUserAdmin()) {
            fabAddTour.setVisibility(View.VISIBLE);
        } else {
            fabAddTour.setVisibility(View.GONE);
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
     * Enhanced event listeners for new UI components
     */
    private void setupEventListeners() {
        // Enhanced FAB click listener
        fabAddTour.setOnClickListener(v -> {
            if (isCurrentUserAdmin()) {
                Intent intent = new Intent(DashboardActivity.this, AddEditTourActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Add tour feature (Admin only)", Toast.LENGTH_SHORT).show();
            }
        });

        // Search card click listener
        tvSearchHint.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SearchToursActivity.class);
            startActivity(intent);
        });

        // View all tours click listener
        tvViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SearchToursActivity.class);
            intent.putExtra("show_all", true);
            startActivity(intent);
        });

        // Filter icon click listener (placeholder for future filter functionality)
        findViewById(R.id.iv_filter).setOnClickListener(v -> {
            // TODO: Implement filter functionality
            Toast.makeText(this, "Filter coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Loads dashboard statistics for the stats cards
     */
    private void loadDashboardStats() {
        try {
            // Load total tours count
            List<Tour> allTours = database.tourDao().getActiveTours();
            tvTotalTours.setText(String.valueOf(allTours.size()));

            // Load user bookings count
            if (currentUser != null && !isAdmin) {
                int bookingsCount = database.bookingDao().getUserBookingsCount(currentUser.getId());
                tvUserBookings.setText(String.valueOf(bookingsCount));
            } else {
                // For admin, show total bookings in system
                int totalBookings = database.bookingDao().getTotalBookingsCount();
                tvUserBookings.setText(String.valueOf(totalBookings));
            }
        } catch (Exception e) {
            // Handle error gracefully
            tvTotalTours.setText("0");
            tvUserBookings.setText("0");
        }
    }

    /**
     * Initializes the custom sliding popup menu
     */
    private void initializeSlidingMenu() {
        slidingPopupMenu = new SlidingPopupMenu(this, new SlidingPopupMenu.OnMenuItemClickListener() {
            @Override
            public void onProfileClick() {
                Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
            }

            @Override
            public void onBookingHistoryClick() {
                Intent intent = new Intent(DashboardActivity.this, BookingHistoryActivity.class);
                if (currentUser != null) {
                    intent.putExtra("user_id", currentUser.getId());
                }
                startActivity(intent);
            }

            @Override
            public void onSettingsClick() {
                Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                startActivity(intent);
            }

            @Override
            public void onUserManagementClick() {
                if (isCurrentUserAdmin()) {
                    Intent intent = new Intent(DashboardActivity.this, UserManagementActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(DashboardActivity.this, "Admin access required", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRevenueManagementClick() {
                if (isCurrentUserAdmin()) {
                    Intent intent = new Intent(DashboardActivity.this, RevenueManagementActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(DashboardActivity.this, "Admin access required", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLogoutClick() {
                // Show confirmation dialog for logout
                new androidx.appcompat.app.AlertDialog.Builder(DashboardActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> logout())
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });

        // Configure menu for admin users
        configureAdminMenu();
    }

    /**
     * Configures the sliding menu to show/hide admin options based on user permissions
     */
    private void configureAdminMenu() {
        if (slidingPopupMenu != null && isCurrentUserAdmin()) {
            // Show admin menu items for admin users
            // This will be handled in the SlidingPopupMenu class to show admin sections
            android.util.Log.d("DashboardActivity", "Configuring admin menu for admin user");
        }
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
     * Enhanced menu item selection - fixed to prevent multiple dropdowns
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            // Search action - navigate to search activity
            Intent intent = new Intent(this, SearchToursActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_menu) {
            // Show the beautiful sliding popup menu only once
            if (slidingPopupMenu != null && !slidingPopupMenu.isShowing()) {
                View toolbarView = findViewById(R.id.toolbar);
                if (toolbarView != null) {
                    slidingPopupMenu.show(toolbarView);
                }
            }
            return true;
        } else if (id == android.R.id.home) {
            // Handle up navigation
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
     * Handles book tour button clicks from RecyclerView
     *
     * @param tour Selected tour object
     */
    @Override
    public void onBookTourClick(Tour tour) {
        navigateToBookTour(tour);
    }

    /**
     * Handles edit tour button clicks from RecyclerView (Admin only)
     *
     * @param tour Selected tour object
     */
    @Override
    public void onEditTourClick(Tour tour) {
        if (isCurrentUserAdmin()) {
            navigateToEditTour(tour);
        } else {
            Toast.makeText(this, "Edit tour feature (Admin only)", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles delete tour button clicks from RecyclerView (Admin only)
     *
     * @param tour Selected tour object
     */
    @Override
    public void onDeleteTourClick(Tour tour) {
        if (isCurrentUserAdmin()) {
            showDeleteTourConfirmation(tour);
        } else {
            Toast.makeText(this, "Delete tour feature (Admin only)", Toast.LENGTH_SHORT).show();
        }
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
     * Navigates to edit tour activity (Admin only)
     *
     * @param tour Tour to edit
     */
    private void navigateToEditTour(Tour tour) {
        Intent intent = new Intent(this, AddEditTourActivity.class);
        intent.putExtra("tour_id", tour.getId());
        startActivityForResult(intent, 1001);
    }

    /**
     * Shows confirmation dialog for tour deletion (Admin only)
     *
     * @param tour Tour to delete
     */
    private void showDeleteTourConfirmation(Tour tour) {
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
     * Navigates to add new tour activity (admin feature)
     */
    private void navigateToAddTour() {
        Intent intent = new Intent(this, AddEditTourActivity.class);
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
        loadTours();
        loadDashboardStats();
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
     * Updates admin menu visibility based on current user's admin status
     */
    private void updateAdminMenuVisibility() {
        if (slidingPopupMenu != null) {
            slidingPopupMenu.setAdminMenuVisibility(isCurrentUserAdmin());
            android.util.Log.d("DashboardActivity", "Admin menu visibility updated: " + isCurrentUserAdmin());
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
