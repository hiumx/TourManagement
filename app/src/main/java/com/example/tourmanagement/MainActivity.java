package com.example.tourmanagement;

import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourmanagement.activity.BookTourActivity;
import com.example.tourmanagement.activity.AddTourActivity;
import com.example.tourmanagement.adapter.TourAdapter;
import com.example.tourmanagement.dao.TourDao;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Tour;
import com.example.tourmanagement.model.User;
import com.example.tourmanagement.utils.SampleDataUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MainActivity with live search functionality for tours.
 * Implements real-time search as user types in the search bar.
 */
public class MainActivity extends AppCompatActivity {

    // UI Components
    private EditText etSearch;
    private ImageView ivClearSearch;
    private RecyclerView rvTours;
    private LinearLayout layoutEmptyState;
    private TextView tvEmptyTitle, tvEmptyMessage;
    private ChipGroup chipGroupFilters;
    private FloatingActionButton fabAddTour;

    // Data and Adapter
    private TourAdapter tourAdapter;
    private TourDao tourDao;
    private List<Tour> allTours;
    private List<Tour> filteredTours;

    // Search functionality
    private Handler searchHandler;
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY_MS = 300; // Delay for live search

    // Filter states
    private boolean showAllTours = true;
    private boolean showAvailableOnly = false;
    private boolean showPopularOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeDatabase();
        setupRecyclerView();
        setupSearchFunctionality();
        setupFilterChips();
        setupFloatingActionButton();
        checkAdminStatusAndConfigureUI();  // Check admin status and configure UI
        loadTours();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        etSearch = findViewById(R.id.et_search);
        ivClearSearch = findViewById(R.id.iv_clear_search);
        rvTours = findViewById(R.id.rv_tours);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        tvEmptyTitle = findViewById(R.id.tv_empty_title);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        fabAddTour = findViewById(R.id.fab_add_tour);

        // Initialize search handler
        searchHandler = new Handler(Looper.getMainLooper());

        // Initialize lists
        allTours = new ArrayList<>();
        filteredTours = new ArrayList<>();
    }

    /**
     * Initialize database and DAO
     */
    private void initializeDatabase() {
        TourManagementDatabase database = TourManagementDatabase.getDatabase(this);
        tourDao = database.tourDao();

        // Populate sample data if database is empty
        SampleDataUtil.populateSampleData(this);
    }

    /**
     * Setup RecyclerView with adapter
     */
    private void setupRecyclerView() {
        // Create the click listener implementing all required methods
        TourAdapter.OnTourClickListener clickListener = new TourAdapter.OnTourClickListener() {
            @Override
            public void onTourClick(Tour tour) {
                // Navigate to tour details - implement this
                // Intent intent = new Intent(MainActivity.this, TourDetailsActivity.class);
                // intent.putExtra("TOUR_ID", tour.getId());
                // startActivity(intent);
            }

            @Override
            public void onBookTourClick(Tour tour) {
                // Navigate to booking page
                Intent intent = new Intent(MainActivity.this, BookTourActivity.class);
                intent.putExtra("TOUR_ID", tour.getId());
                intent.putExtra("USER_ID", getSharedPreferences("TourManagementPrefs", MODE_PRIVATE)
                        .getInt("user_id", -1));

                // Debug logging to see what tour ID we're passing
                android.util.Log.d("MainActivity", "Booking tour with ID: " + tour.getId());
                android.util.Log.d("MainActivity", "Tour name: " + tour.getTourName());

                startActivity(intent);
            }

            @Override
            public void onDeleteTourClick(Tour tour) {
                // Show confirmation dialog before deleting
                showDeleteConfirmationDialog(tour);
            }
        };

        tourAdapter = new TourAdapter(this, clickListener);
        rvTours.setLayoutManager(new LinearLayoutManager(this));
        rvTours.setAdapter(tourAdapter);
    }

    /**
     * Setup live search functionality
     */
    private void setupSearchFunctionality() {
        // Text watcher for live search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button
                if (s.length() > 0) {
                    ivClearSearch.setVisibility(View.VISIBLE);
                } else {
                    ivClearSearch.setVisibility(View.GONE);
                }

                // Cancel previous search if still pending
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Create new search runnable with delay
                searchRunnable = () -> performLiveSearch(s.toString().trim());

                // Execute search after delay
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Clear search button click listener
        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.clearFocus();
        });
    }

    /**
     * Perform live search with the given query
     * @param query Search query entered by user
     */
    private void performLiveSearch(String query) {
        if (query.isEmpty()) {
            // Show all tours if search is empty
            applyFiltersToTours(allTours);
        } else {
            // Use the live search DAO method
            tourDao.searchToursLive(query).observe(this, new Observer<List<Tour>>() {
                @Override
                public void onChanged(List<Tour> tours) {
                    if (tours != null) {
                        applyFiltersToTours(tours);
                    }
                }
            });
        }
    }

    /**
     * Setup filter chips functionality
     */
    private void setupFilterChips() {
        Chip chipAll = findViewById(R.id.chip_all);
        Chip chipAvailable = findViewById(R.id.chip_available);
        Chip chipPopular = findViewById(R.id.chip_popular);

        chipAll.setOnClickListener(v -> {
            showAllTours = chipAll.isChecked();
            refreshFilteredResults();
        });

        chipAvailable.setOnClickListener(v -> {
            showAvailableOnly = chipAvailable.isChecked();
            refreshFilteredResults();
        });

        chipPopular.setOnClickListener(v -> {
            showPopularOnly = chipPopular.isChecked();
            refreshFilteredResults();
        });
    }

    /**
     * Setup floating action button for adding new tours
     */
    private void setupFloatingActionButton() {
        fabAddTour.setOnClickListener(v -> {
            if (isCurrentUserAdmin()) {
                // Navigate to add tour activity
                Intent intent = new Intent(MainActivity.this, AddTourActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Only admin users can add tours", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load all tours from database
     */
    private void loadTours() {
        tourDao.getAllToursLive().observe(this, new Observer<List<Tour>>() {
            @Override
            public void onChanged(List<Tour> tours) {
                if (tours != null) {
                    allTours.clear();
                    allTours.addAll(tours);

                    // Apply current search query if any
                    String currentQuery = etSearch.getText().toString().trim();
                    if (currentQuery.isEmpty()) {
                        applyFiltersToTours(allTours);
                    } else {
                        performLiveSearch(currentQuery);
                    }
                }
            }
        });
    }

    /**
     * Apply filters to the tour list based on selected filter chips
     * @param tours List of tours to filter
     */
    private void applyFiltersToTours(List<Tour> tours) {
        filteredTours.clear();

        for (Tour tour : tours) {
            boolean shouldInclude = true;

            // Apply filters based on chip selections
            if (showAvailableOnly) {
                // Assuming Tour has methods to check availability
                // shouldInclude = shouldInclude && tour.isAvailable();
            }

            if (showPopularOnly) {
                // Assuming Tour has methods to check popularity
                // shouldInclude = shouldInclude && tour.isPopular();
            }

            if (shouldInclude) {
                filteredTours.add(tour);
            }
        }

        // Update UI
        updateUI();
    }

    /**
     * Refresh filtered results based on current filters
     */
    private void refreshFilteredResults() {
        String currentQuery = etSearch.getText().toString().trim();
        if (currentQuery.isEmpty()) {
            applyFiltersToTours(allTours);
        } else {
            performLiveSearch(currentQuery);
        }
    }

    /**
     * Update UI based on filtered results
     */
    private void updateUI() {
        runOnUiThread(() -> {
            if (filteredTours.isEmpty()) {
                // Show empty state
                rvTours.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);

                // Update empty state message based on search query
                String searchQuery = etSearch.getText().toString().trim();
                if (!searchQuery.isEmpty()) {
                    tvEmptyTitle.setText("No tours found");
                    tvEmptyMessage.setText("No tours match your search for \"" + searchQuery + "\"");
                } else {
                    tvEmptyTitle.setText("No tours available");
                    tvEmptyMessage.setText("Add some tours to get started");
                }
            } else {
                // Show tours list
                rvTours.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
            }

            // Notify adapter of data changes
            tourAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Show confirmation dialog before deleting a tour
     * @param tour Tour object to be deleted
     */
    private void showDeleteConfirmationDialog(Tour tour) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tour")
                .setMessage("Are you sure you want to delete this tour?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete tour in background
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            tourDao.deleteTour(tour);

                            // Refresh tour list
                            runOnUiThread(() -> {
                                allTours.remove(tour);
                                refreshFilteredResults();
                                Toast.makeText(MainActivity.this, "Tour deleted", Toast.LENGTH_SHORT).show();
                            });
                        });
                    }
                })
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Check if the current user is an admin
     * @return true if current user has admin privileges, false otherwise
     */
    private boolean isCurrentUserAdmin() {
        // Get current user ID from SharedPreferences
        int currentUserId = getSharedPreferences("TourManagementPrefs", MODE_PRIVATE)
                .getInt("user_id", -1);

        if (currentUserId == -1) {
            return false; // No user logged in
        }

        // Check if this is the admin user (username: admin, password: admin)
        // Query the database to check if the current user is the admin
        TourManagementDatabase database = TourManagementDatabase.getDatabase(this);
        try {
            User currentUser = database.userDao().getUserById(currentUserId);
            if (currentUser != null && "admin".equals(currentUser.getUsername())) {
                return true; // This is the admin user
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error checking admin status: " + e.getMessage());
        }

        return false; // Not admin
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
     * Setup event listeners for UI components
     */
    private void setupEventListeners() {
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button
                if (s.length() > 0) {
                    ivClearSearch.setVisibility(View.VISIBLE);
                } else {
                    ivClearSearch.setVisibility(View.GONE);
                }

                // Cancel previous search if still pending
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Create new search runnable with delay
                searchRunnable = () -> performLiveSearch(s.toString().trim());

                // Execute search after delay
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Clear search button
        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.clearFocus();
        });

        // FAB click listener - only for admin users
        fabAddTour.setOnClickListener(v -> {
            if (isCurrentUserAdmin()) {
                // Navigate to add tour activity
                Intent intent = new Intent(MainActivity.this, AddTourActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Only admin users can add tours", Toast.LENGTH_SHORT).show();
            }
        });

        // Check admin status and show/hide FAB accordingly
        checkAdminStatusAndConfigureUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up search handler
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}
