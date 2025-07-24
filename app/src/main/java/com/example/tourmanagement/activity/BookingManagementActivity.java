package com.example.tourmanagement.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tourmanagement.R;
import com.example.tourmanagement.adapter.BookingManagementAdapter;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Booking;
import com.example.tourmanagement.model.Tour;
import com.example.tourmanagement.model.User;
import com.example.tourmanagement.utils.EmailService;
import java.util.List;

/**
 * Admin activity for managing all bookings in the system.
 *
 * Features:
 * - View all pending bookings awaiting confirmation
 * - Approve or reject booking requests
 * - View confirmed and cancelled bookings
 * - Send notification emails on booking status changes
 * - Booking statistics and management
 *
 * Admin Functions:
 * - Confirm pending bookings (changes status to CONFIRMED)
 * - Reject pending bookings (changes status to CANCELLED)
 * - View booking details and customer information
 * - Filter bookings by status
 * - Bulk actions for multiple bookings
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-24
 */
public class BookingManagementActivity extends AppCompatActivity implements BookingManagementAdapter.OnBookingActionListener {

    /**
     * UI components
     */
    private TextView tvPendingCount, tvConfirmedCount, tvCancelledCount, tvEmptyMessage;
    private RecyclerView recyclerViewBookings;

    /**
     * Adapter for booking management
     */
    private BookingManagementAdapter bookingAdapter;

    /**
     * Database instance
     */
    private TourManagementDatabase database;

    /**
     * Current filter status
     */
    private String currentFilter = "ALL"; // ALL, PENDING, CONFIRMED, CANCELLED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_management);

        // Initialize database
        database = TourManagementDatabase.getDatabase(this);

        // Initialize UI components
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load bookings
        loadBookings();

        // Load statistics
        loadBookingStatistics();
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        tvPendingCount = findViewById(R.id.tv_pending_count);
        tvConfirmedCount = findViewById(R.id.tv_confirmed_count);
        tvCancelledCount = findViewById(R.id.tv_cancelled_count);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        recyclerViewBookings = findViewById(R.id.recycler_view_bookings);

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Booking Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup filter buttons
        findViewById(R.id.btn_filter_all).setOnClickListener(v -> filterBookings("ALL"));
        findViewById(R.id.btn_filter_pending).setOnClickListener(v -> filterBookings("PENDING"));
        findViewById(R.id.btn_filter_confirmed).setOnClickListener(v -> filterBookings("CONFIRMED"));
        findViewById(R.id.btn_filter_cancelled).setOnClickListener(v -> filterBookings("CANCELLED"));
    }

    /**
     * Sets up the RecyclerView for booking management
     */
    private void setupRecyclerView() {
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new BookingManagementAdapter(this, this);
        recyclerViewBookings.setAdapter(bookingAdapter);
    }

    /**
     * Loads bookings based on current filter
     */
    private void loadBookings() {
        new Thread(() -> {
            try {
                List<Booking> bookings;

                switch (currentFilter) {
                    case "PENDING":
                        bookings = database.bookingDao().getBookingsByStatus("PENDING");
                        break;
                    case "CONFIRMED":
                        bookings = database.bookingDao().getBookingsByStatus("CONFIRMED");
                        break;
                    case "CANCELLED":
                        bookings = database.bookingDao().getBookingsByStatus("CANCELLED");
                        break;
                    default:
                        bookings = database.bookingDao().getAllBookings();
                        break;
                }

                runOnUiThread(() -> {
                    if (bookings.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        bookingAdapter.updateBookings(bookings);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showToast("Error loading bookings: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Loads and displays booking statistics
     */
    private void loadBookingStatistics() {
        new Thread(() -> {
            try {
                int pendingCount = database.bookingDao().getBookingCountByStatus("PENDING");
                int confirmedCount = database.bookingDao().getBookingCountByStatus("CONFIRMED");
                int cancelledCount = database.bookingDao().getBookingCountByStatus("CANCELLED");

                runOnUiThread(() -> {
                    tvPendingCount.setText("Pending: " + pendingCount);
                    tvConfirmedCount.setText("Confirmed: " + confirmedCount);
                    tvCancelledCount.setText("Cancelled: " + cancelledCount);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showToast("Error loading statistics: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Filters bookings by status
     */
    private void filterBookings(String status) {
        currentFilter = status;
        loadBookings();

        // Update button states (you can add visual feedback here)
        updateFilterButtonStates();
    }

    /**
     * Updates filter button visual states
     */
    private void updateFilterButtonStates() {
        // Reset all buttons to default state
        findViewById(R.id.btn_filter_all).setSelected(false);
        findViewById(R.id.btn_filter_pending).setSelected(false);
        findViewById(R.id.btn_filter_confirmed).setSelected(false);
        findViewById(R.id.btn_filter_cancelled).setSelected(false);

        // Set selected button
        switch (currentFilter) {
            case "ALL":
                findViewById(R.id.btn_filter_all).setSelected(true);
                break;
            case "PENDING":
                findViewById(R.id.btn_filter_pending).setSelected(true);
                break;
            case "CONFIRMED":
                findViewById(R.id.btn_filter_confirmed).setSelected(true);
                break;
            case "CANCELLED":
                findViewById(R.id.btn_filter_cancelled).setSelected(true);
                break;
        }
    }

    /**
     * Shows empty state when no bookings are available
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
     * Handles booking approval
     */
    @Override
    public void onApproveBooking(Booking booking) {
        showConfirmationDialog(
            "Approve Booking",
            "Are you sure you want to approve this booking?\n\n" +
            "This will confirm the booking and notify the customer.",
            () -> approveBooking(booking)
        );
    }

    /**
     * Handles booking rejection
     */
    @Override
    public void onRejectBooking(Booking booking) {
        showConfirmationDialog(
            "Reject Booking",
            "Are you sure you want to reject this booking?\n\n" +
            "This will cancel the booking and notify the customer.",
            () -> rejectBooking(booking)
        );
    }

    /**
     * Handles viewing booking details
     */
    @Override
    public void onViewBookingDetails(Booking booking) {
        showBookingDetailsDialog(booking);
    }

    /**
     * Approves a booking and updates status to CONFIRMED
     */
    private void approveBooking(Booking booking) {
        new Thread(() -> {
            try {
                // Update booking status
                booking.setBookingStatus("CONFIRMED");
                booking.setPaymentStatus("PAID");
                database.bookingDao().updateBooking(booking);

                // Get user and tour information for email
                User user = database.userDao().getUserById(booking.getUserId());
                Tour tour = database.tourDao().getTourById(booking.getTourId());

                runOnUiThread(() -> {
                    showToast("Booking approved successfully!");
                    loadBookings();
                    loadBookingStatistics();

                    // Send confirmation email
                    if (user != null && tour != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                        sendBookingApprovalEmail(user, tour, booking);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showToast("Error approving booking: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Rejects a booking and updates status to CANCELLED
     */
    private void rejectBooking(Booking booking) {
        new Thread(() -> {
            try {
                // Update booking status
                booking.setBookingStatus("CANCELLED");
                booking.setPaymentStatus("CANCELLED");
                database.bookingDao().updateBooking(booking);

                // Release tour slots
                database.tourDao().updateBookingCount(booking.getTourId(), -booking.getNumberOfPeople());

                // Get user and tour information for email
                User user = database.userDao().getUserById(booking.getUserId());
                Tour tour = database.tourDao().getTourById(booking.getTourId());

                runOnUiThread(() -> {
                    showToast("Booking rejected successfully!");
                    loadBookings();
                    loadBookingStatistics();

                    // Send rejection email
                    if (user != null && tour != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                        sendBookingRejectionEmail(user, tour, booking);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showToast("Error rejecting booking: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Shows confirmation dialog for booking actions
     */
    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Confirm", (dialog, which) -> onConfirm.run())
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Shows detailed booking information dialog
     */
    private void showBookingDetailsDialog(Booking booking) {
        new Thread(() -> {
            try {
                User user = database.userDao().getUserById(booking.getUserId());
                Tour tour = database.tourDao().getTourById(booking.getTourId());

                runOnUiThread(() -> {
                    String details = buildBookingDetailsText(booking, user, tour);

                    new AlertDialog.Builder(this)
                        .setTitle("Booking Details")
                        .setMessage(details)
                        .setPositiveButton("Close", null)
                        .show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showToast("Error loading booking details: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Builds detailed booking information text
     */
    private String buildBookingDetailsText(Booking booking, User user, Tour tour) {
        StringBuilder details = new StringBuilder();

        details.append("Booking Reference: ").append(booking.getBookingReference()).append("\n\n");

        if (user != null) {
            details.append("Customer Information:\n");
            details.append("Name: ").append(user.getFullName()).append("\n");
            details.append("Email: ").append(user.getEmail()).append("\n");
            details.append("Phone: ").append(user.getPhoneNumber()).append("\n\n");
        }

        if (tour != null) {
            details.append("Tour Information:\n");
            details.append("Tour: ").append(tour.getTourName()).append("\n");
            details.append("Location: ").append(tour.getTourLocation()).append("\n");
            details.append("Cost per person: $").append(String.format("%.2f", tour.getTourCost())).append("\n\n");
        }

        details.append("Booking Information:\n");
        details.append("Number of people: ").append(booking.getNumberOfPeople()).append("\n");
        details.append("Total amount: $").append(String.format("%.2f", booking.getTotalAmount())).append("\n");
        details.append("Status: ").append(booking.getBookingStatus()).append("\n");
        details.append("Payment: ").append(booking.getPaymentStatus()).append("\n");

        if (booking.getNotes() != null && !booking.getNotes().trim().isEmpty()) {
            details.append("Notes: ").append(booking.getNotes()).append("\n");
        }

        return details.toString();
    }

    /**
     * Sends booking approval email to customer
     */
    private void sendBookingApprovalEmail(User user, Tour tour, Booking booking) {
        EmailService.sendBookingConfirmationEmail(user, tour, booking, new EmailService.EmailCallback() {
            @Override
            public void onSuccess() {
                android.util.Log.d("BookingManagement", "Approval email sent successfully");
            }

            @Override
            public void onFailure(String error) {
                android.util.Log.e("BookingManagement", "Failed to send approval email: " + error);
            }
        });
    }

    /**
     * Sends booking rejection email to customer
     */
    private void sendBookingRejectionEmail(User user, Tour tour, Booking booking) {
        EmailService.sendBookingCancellationEmail(user, tour, booking, new EmailService.EmailCallback() {
            @Override
            public void onSuccess() {
                android.util.Log.d("BookingManagement", "Rejection email sent successfully");
            }

            @Override
            public void onFailure(String error) {
                android.util.Log.e("BookingManagement", "Failed to send rejection email: " + error);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
        loadBookingStatistics();
    }

    /**
     * Shows a toast message to the user
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
