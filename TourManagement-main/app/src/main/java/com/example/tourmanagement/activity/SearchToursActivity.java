package com.example.tourmanagement.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tourmanagement.R;
import com.example.tourmanagement.adapter.TourAdapter;
import com.example.tourmanagement.dao.TourDao;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Tour;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for searching and filtering tours by name and location.
 * Implements live search functionality with real-time filtering.
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class SearchToursActivity extends AppCompatActivity {

    private TextInputEditText etSearchQuery;
    private RecyclerView recyclerViewSearchResults;
    private TextView tvNoResults;
    private TourAdapter tourAdapter;
    private TourDao tourDAO;
    private ExecutorService executorService;

    private final List<Tour> allTours = new ArrayList<>();
    private final List<Tour> filteredTours = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tours);

        initializeViews();
        setupToolbar();
        setupDatabase();
        setupRecyclerView();
        setupSearchFunctionality();
        loadAllTours();
    }

    private void initializeViews() {
        etSearchQuery = findViewById(R.id.et_search_query);
        recyclerViewSearchResults = findViewById(R.id.recycler_view_search_results);
        tvNoResults = findViewById(R.id.tv_no_results);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Tours");
        }
    }

    private void setupDatabase() {
        TourManagementDatabase database = TourManagementDatabase.getDatabase(this);
        tourDAO = database.tourDao();
        executorService = Executors.newFixedThreadPool(2);
    }

    private void setupRecyclerView() {
        // Create the click listener implementing all required methods
        TourAdapter.OnTourClickListener clickListener = new TourAdapter.OnTourClickListener() {
            @Override
            public void onTourClick(Tour tour) {
                // Navigate to tour details
                Intent intent = new Intent(SearchToursActivity.this, TourDetailsActivity.class);
                intent.putExtra("TOUR_ID", tour.getId());
                startActivity(intent);
            }

            @Override
            public void onBookTourClick(Tour tour) {
                // Navigate to booking page
                Intent intent = new Intent(SearchToursActivity.this, BookTourActivity.class);
                intent.putExtra("TOUR_ID", tour.getId());
                intent.putExtra("USER_ID", getSharedPreferences("TourManagementPrefs", MODE_PRIVATE)
                        .getInt("user_id", -1));
                startActivity(intent);
            }

            @Override
            public void onEditTourClick(Tour tour) {
                // Check if user is admin and navigate to edit tour (admin only)
                boolean isAdmin = getSharedPreferences("TourManagementPrefs", MODE_PRIVATE)
                        .getBoolean("is_admin", false);
                if (isAdmin) {
                    Intent intent = new Intent(SearchToursActivity.this, AddEditTourActivity.class);
                    intent.putExtra("tour_id", tour.getId());
                    startActivity(intent);
                } else {
                    Toast.makeText(SearchToursActivity.this, "Edit tour feature (Admin only)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDeleteTourClick(Tour tour) {
                // Show confirmation dialog before deleting (admin only)
                showDeleteConfirmationDialog(tour);
            }
        };

        tourAdapter = new TourAdapter(this, clickListener);
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSearchResults.setAdapter(tourAdapter);
    }

    private void setupSearchFunctionality() {
        // Live search as user types
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Perform search with slight delay to avoid excessive filtering
                performSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void loadAllTours() {
        executorService.execute(() -> {
            List<Tour> tours = tourDAO.getAllTours();
            runOnUiThread(() -> {
                allTours.clear();
                allTours.addAll(tours);

                // Update the adapter's tour list
                tourAdapter.updateTours(allTours);

                // Initially show all tours
                filteredTours.clear();
                filteredTours.addAll(allTours);
                updateResultsVisibility();
            });
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            // Show all tours when search is empty
            filteredTours.clear();
            filteredTours.addAll(allTours);
        } else {
            // Filter tours by name or location
            filteredTours.clear();
            String lowercaseQuery = query.toLowerCase();

            for (Tour tour : allTours) {
                boolean matchesName = tour.getTourName() != null &&
                    tour.getTourName().toLowerCase().contains(lowercaseQuery);
                boolean matchesLocation = tour.getTourLocation() != null &&
                    tour.getTourLocation().toLowerCase().contains(lowercaseQuery);

                if (matchesName || matchesLocation) {
                    filteredTours.add(tour);
                }
            }
        }

        // Update the adapter with filtered results
        tourAdapter.updateTours(filteredTours);
        updateResultsVisibility();
    }

    private void updateResultsVisibility() {
        if (filteredTours.isEmpty()) {
            recyclerViewSearchResults.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);

            String searchQuery = etSearchQuery.getText() != null ?
                etSearchQuery.getText().toString().trim() : "";
            if (searchQuery.isEmpty()) {
                tvNoResults.setText(R.string.no_tours_available);
            } else {
                tvNoResults.setText(getString(R.string.no_tours_found, searchQuery));
            }
        } else {
            recyclerViewSearchResults.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.GONE);
        }
    }

    private void showDeleteConfirmationDialog(Tour tour) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Tour")
            .setMessage("Are you sure you want to delete this tour?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteTour(tour);
                }
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void deleteTour(Tour tour) {
        executorService.execute(() -> {
            tourDAO.deleteTour(tour);
            runOnUiThread(() -> {
                // Refresh tour list after deletion
                loadAllTours();
                Toast.makeText(SearchToursActivity.this, "Tour deleted successfully", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
