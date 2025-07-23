package com.example.tourmanagement.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tourmanagement.R;
import com.example.tourmanagement.adapter.BookingHistoryAdapter;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Booking;
import java.util.List;

/**
 * Activity for displaying user's booking history and managing bookings.
 *
 * Features:
 * - Display all user bookings in chronological order
 * - Show booking status and payment information
 * - Navigate to ticket details for confirmed bookings
 * - Filter bookings by status
 * - Cancel pending bookings
 * - Booking statistics and summary
 *
 * UI Components:
 * - RecyclerView for booking list
 * - Booking summary statistics
 * - Filter options for booking status
 * - Empty state message when no bookings
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class BookingHistoryActivity extends AppCompatActivity implements BookingHistoryAdapter.OnBookingClickListener {

    /**
     * UI components
     */
    private TextView tvBookingCount, tvTotalSpent, tvEmptyMessage;
    private RecyclerView recyclerViewBookings;

    /**
     * Adapter for booking history
     */
    private BookingHistoryAdapter bookingAdapter;

    /**
     * Database and user data
     */
    private TourManagementDatabase database;
    private int currentUserId;

    /**
     * Called when the activity is first created.
     * Initializes UI components and loads booking history.
     *
     * @param savedInstanceState Bundle containing saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        // Initialize database
        database = TourManagementDatabase.getDatabase(this);

        // Get user ID from intent or SharedPreferences as fallback
        currentUserId = getIntent().getIntExtra("user_id", -1);

        // If no user_id passed via intent, try to get it from SharedPreferences
        if (currentUserId == -1) {
            android.content.SharedPreferences sharedPreferences = getSharedPreferences("TourManagementPrefs", MODE_PRIVATE);
            currentUserId = sharedPreferences.getInt("user_id", -1);

            // Log for debugging
            android.util.Log.d("BookingHistoryActivity", "No user_id in intent, retrieved from SharedPreferences: " + currentUserId);
        } else {
            android.util.Log.d("BookingHistoryActivity", "Received user_id from intent: " + currentUserId);
        }

        if (currentUserId == -1) {
            android.util.Log.e("BookingHistoryActivity", "No valid user ID found");
            showToast("Error: User session not found. Please login again.");
            finish();
            return;
        }

        // Initialize UI components
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load booking history
        loadBookingHistory();

        // Load booking statistics
        loadBookingStatistics();
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        tvBookingCount = findViewById(R.id.tv_booking_count);
        tvTotalSpent = findViewById(R.id.tv_total_spent);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        recyclerViewBookings = findViewById(R.id.recycler_view_bookings);

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Booking History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Sets up the RecyclerView for booking history
     */
    private void setupRecyclerView() {
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new BookingHistoryAdapter(this, this);
        recyclerViewBookings.setAdapter(bookingAdapter);
    }

    /**
     * Loads user's booking history from database
     */
    private void loadBookingHistory() {
        try {
            List<Booking> bookings = database.bookingDao().getBookingsByUserId(currentUserId);

            // Add debugging to see what booking IDs we're getting from database
            android.util.Log.d("BookingHistoryActivity", "Loaded " + bookings.size() + " bookings for user " + currentUserId);
            for (int i = 0; i < bookings.size(); i++) {
                Booking booking = bookings.get(i);
                android.util.Log.d("BookingHistoryActivity", "Booking " + i + " - ID: " + booking.getId() +
                                  ", Reference: " + booking.getBookingReference() +
                                  ", Status: " + booking.getBookingStatus());
            }

            if (bookings.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
                bookingAdapter.updateBookings(bookings);
            }
        } catch (Exception e) {
            android.util.Log.e("BookingHistoryActivity", "Error loading booking history", e);
            showToast("Error loading booking history: " + e.getMessage());
        }
    }

    /**
     * Loads and displays booking statistics
     */
    private void loadBookingStatistics() {
        try {
            List<Booking> userBookings = database.bookingDao().getBookingsByUserId(currentUserId);
            int bookingCount = userBookings.size();

            double totalSpent = 0.0;
            for (Booking booking : userBookings) {
                if ("PAID".equals(booking.getPaymentStatus())) {
                    totalSpent += booking.getTotalAmount();
                }
            }

            tvBookingCount.setText("Total Bookings: " + bookingCount);
            tvTotalSpent.setText("Total Spent: $" + String.format("%.2f", totalSpent));

        } catch (Exception e) {
            showToast("Error loading statistics: " + e.getMessage());
        }
    }

    /**
     * Shows empty state when user has no bookings
     */
    private void showEmptyState() {
        tvEmptyMessage.setVisibility(View.VISIBLE);
        recyclerViewBookings.setVisibility(View.GONE);
    }

    /**
     * Hides empty state when bookings are available
     */
    private void hideEmptyState() {
        tvEmptyMessage.setVisibility(View.GONE);
        recyclerViewBookings.setVisibility(View.VISIBLE);
    }

    /**
     * Handles booking item clicks
     *
     * @param booking Selected booking
     */
    @Override
    public void onBookingClick(Booking booking) {
        // Add debugging to see what booking ID we're getting
        android.util.Log.d("BookingHistoryActivity", "Booking clicked - ID: " + booking.getId() +
                          ", Status: " + booking.getBookingStatus() +
                          ", Reference: " + booking.getBookingReference());

        if ("CONFIRMED".equals(booking.getBookingStatus())) {
            // Navigate to ticket view
            android.content.Intent intent = new android.content.Intent(this, TicketActivity.class);
            intent.putExtra("booking_id", booking.getId());
            android.util.Log.d("BookingHistoryActivity", "Passing booking ID to TicketActivity: " + booking.getId());
            startActivity(intent);
        } else {
            showToast("Ticket only available for confirmed bookings");
        }
    }

    /**
     * Handles booking cancellation requests
     *
     * @param booking Booking to cancel
     */
    @Override
    public void onCancelBooking(Booking booking) {
        if ("PENDING".equals(booking.getBookingStatus())) {
            cancelBooking(booking);
        } else {
            showToast("Only pending bookings can be cancelled");
        }
    }

    /**
     * Cancels a booking and updates the database
     *
     * @param booking Booking to cancel
     */
    private void cancelBooking(Booking booking) {
        try {
            // Update booking status
            booking.setBookingStatus("CANCELLED");
            database.bookingDao().updateBooking(booking);

            // Update tour booking count (decrease)
            database.tourDao().updateBookingCount(booking.getTourId(), -booking.getNumberOfPeople());

            showToast("Booking cancelled successfully");

            // Refresh booking history
            loadBookingHistory();
            loadBookingStatistics();

        } catch (Exception e) {
            showToast("Error cancelling booking: " + e.getMessage());
        }
    }

    /**
     * Refreshes booking history when returning from other activities
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadBookingHistory();
        loadBookingStatistics();
    }

    /**
     * Shows a toast message to the user
     *
     * @param message Message to display
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles back button press in toolbar
     *
     * @return true if handled
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
