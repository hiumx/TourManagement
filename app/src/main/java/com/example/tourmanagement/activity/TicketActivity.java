package com.example.tourmanagement.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.example.tourmanagement.R;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Booking;
import com.example.tourmanagement.model.Tour;
import com.example.tourmanagement.model.User;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for displaying booking tickets with QR codes and booking details.
 *
 * Features:
 * - Complete booking information display
 * - QR code generation for ticket verification
 * - Tour details and customer information
 * - Booking reference and status
 * - Share ticket functionality
 * - Download/Save ticket option
 *
 * UI Components:
 * - Booking reference number
 * - Tour information (name, location, date)
 * - Customer details
 * - QR code for verification
 * - Booking status indicator
 * - Action buttons (share, save, back to dashboard)
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class TicketActivity extends AppCompatActivity {

    /**
     * UI components
     */
    private TextView tvBookingReference, tvTourName, tvTourLocation, tvTourDate;
    private TextView tvCustomerName, tvCustomerEmail, tvCustomerPhone;
    private TextView tvNumberOfPeople, tvTotalAmount, tvBookingStatus;
    private ImageView ivTicketQRCode;
    private Button btnBackToDashboard, btnShareTicket;

    /**
     * Database and data objects
     */
    private TourManagementDatabase database;
    private Booking currentBooking;
    private Tour bookedTour;
    private User customer;

    /**
     * Formatters
     */
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;

    /**
     * Called when the activity is first created.
     * Initializes UI components and loads booking data.
     *
     * @param savedInstanceState Bundle containing saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        // Initialize database and formatters
        database = TourManagementDatabase.getDatabase(this);
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        dateFormatter = new SimpleDateFormat("EEEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

        // Load booking data from intent - if this fails, activity will finish
        if (!loadBookingData()) {
            return; // Don't continue if booking data couldn't be loaded
        }

        // Initialize UI components
        initializeViews();

        // Display ticket information
        displayTicketInfo();

        // Generate QR code for ticket
        generateTicketQRCode();

        // Setup event listeners
        setupEventListeners();
    }

    /**
     * Loads booking data from intent and database
     * @return true if data loaded successfully, false otherwise
     */
    private boolean loadBookingData() {
        // Debug: Check all intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            android.util.Log.d("TicketActivity", "Intent extras:");
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                android.util.Log.d("TicketActivity", "  " + key + " = " + value + " (type: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");
            }
        } else {
            android.util.Log.d("TicketActivity", "No intent extras found");
        }

        // Fix: Use getLongExtra instead of getIntExtra since the ID is passed as Long
        long bookingId = getIntent().getLongExtra("booking_id", -1L);
        android.util.Log.d("TicketActivity", "Received booking ID: " + bookingId);

        if (bookingId == -1L) {
            android.util.Log.e("TicketActivity", "Invalid booking ID received");
            showToast("Invalid booking ID");
            finish();
            return false;
        }

        try {
            // Convert long to int for database query (assuming your DAO expects int)
            currentBooking = database.bookingDao().getBookingById((int) bookingId);
            android.util.Log.d("TicketActivity", "Loaded booking: " + (currentBooking != null ? currentBooking.getBookingReference() : "null"));

            if (currentBooking != null) {
                bookedTour = database.tourDao().getTourById(currentBooking.getTourId());
                android.util.Log.d("TicketActivity", "Loaded tour: " + (bookedTour != null ? bookedTour.getTourName() : "null"));

                customer = database.userDao().getUserById(currentBooking.getUserId());
                android.util.Log.d("TicketActivity", "Loaded customer: " + (customer != null ? customer.getFullName() : "null"));
            }
        } catch (Exception e) {
            android.util.Log.e("TicketActivity", "Error loading booking data: " + e.getMessage());
            showToast("Error loading ticket information: " + e.getMessage());
            finish();
            return false;
        }

        if (currentBooking == null) {
            android.util.Log.e("TicketActivity", "Booking not found in database");
            showToast("Booking not found");
            finish();
            return false;
        }

        if (bookedTour == null) {
            android.util.Log.e("TicketActivity", "Tour not found for booking");
            showToast("Tour information not found");
            finish();
            return false;
        }

        if (customer == null) {
            android.util.Log.e("TicketActivity", "Customer not found for booking");
            showToast("Customer information not found");
            finish();
            return false;
        }

        return true; // All data loaded successfully
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        tvBookingReference = findViewById(R.id.tv_booking_reference);
        tvTourName = findViewById(R.id.tv_tour_name);
        tvTourLocation = findViewById(R.id.tv_tour_location);
        tvTourDate = findViewById(R.id.tv_tour_date);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvCustomerEmail = findViewById(R.id.tv_customer_email);
        tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        tvNumberOfPeople = findViewById(R.id.tv_number_of_people);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvBookingStatus = findViewById(R.id.tv_booking_status);
        ivTicketQRCode = findViewById(R.id.iv_ticket_qr_code);
        btnBackToDashboard = findViewById(R.id.btn_back_to_dashboard);
        btnShareTicket = findViewById(R.id.btn_share_ticket);

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Your Ticket");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Displays all ticket information in the UI
     */
    private void displayTicketInfo() {
        if (currentBooking != null && bookedTour != null && customer != null) {
            // Booking reference
            tvBookingReference.setText("Ref: " + currentBooking.getBookingReference());

            // Tour information
            tvTourName.setText(bookedTour.getTourName());
            tvTourLocation.setText(bookedTour.getTourLocation());

            // Format and display tour date
            Date tourDate = new Date(bookedTour.getTourTime());
            tvTourDate.setText(dateFormatter.format(tourDate));

            // Customer information
            tvCustomerName.setText(customer.getFullName());
            tvCustomerEmail.setText(customer.getEmail());
            tvCustomerPhone.setText(customer.getPhoneNumber());

            // Booking details
            tvNumberOfPeople.setText(String.valueOf(currentBooking.getNumberOfPeople()) + " people");
            tvTotalAmount.setText(currencyFormatter.format(currentBooking.getTotalAmount()));

            // Booking status with color coding
            String status = currentBooking.getBookingStatus();
            tvBookingStatus.setText(status);
            setStatusColor(status);
        }
    }

    /**
     * Sets the color of booking status based on status value
     *
     * @param status Booking status string
     */
    private void setStatusColor(String status) {
        int colorResId;
        switch (status.toUpperCase()) {
            case "CONFIRMED":
                colorResId = R.color.success_color;
                break;
            case "PENDING":
                colorResId = R.color.warning_color;
                break;
            case "CANCELLED":
                colorResId = R.color.error_color;
                break;
            default:
                colorResId = R.color.primary_color;
                break;
        }
        tvBookingStatus.setTextColor(ContextCompat.getColor(this, colorResId));
    }

    /**
     * Generates QR code for ticket verification
     */
    private void generateTicketQRCode() {
        try {
            // Create QR code content with ticket verification data
            String qrContent = createTicketQRContent();

            // Generate QR code bitmap
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrContent, BarcodeFormat.QR_CODE, 300, 300);

            // Display QR code
            ivTicketQRCode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            showToast("Error generating ticket QR code: " + e.getMessage());
        }
    }

    /**
     * Creates QR code content for ticket verification
     *
     * @return QR code content string
     */
    private String createTicketQRContent() {
        StringBuilder content = new StringBuilder();
        content.append("TICKET_VERIFICATION\n");
        content.append("BookingRef: ").append(currentBooking.getBookingReference()).append("\n");
        content.append("TourID: ").append(bookedTour.getId()).append("\n");
        content.append("CustomerID: ").append(customer.getId()).append("\n");
        content.append("People: ").append(currentBooking.getNumberOfPeople()).append("\n");
        content.append("Amount: ").append(String.format("%.2f", currentBooking.getTotalAmount())).append("\n");
        content.append("Status: ").append(currentBooking.getBookingStatus()).append("\n");
        content.append("BookingDate: ").append(currentBooking.getBookingDate()).append("\n");
        content.append("TourDate: ").append(bookedTour.getTourTime());
        return content.toString();
    }

    /**
     * Sets up event listeners for UI components
     */
    private void setupEventListeners() {
        // Back to dashboard button
        btnBackToDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBackToDashboard();
            }
        });

        // Share ticket button
        btnShareTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTicket();
            }
        });
    }

    /**
     * Navigates back to dashboard activity
     */
    private void navigateBackToDashboard() {
        finish(); // This will return to the previous activity
    }

    /**
     * Shares ticket information via Android share intent
     */
    private void shareTicket() {
        String shareText = createShareText();

        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tour Booking Ticket");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);

        startActivity(android.content.Intent.createChooser(shareIntent, "Share Ticket"));
    }

    /**
     * Creates formatted text for sharing ticket information
     *
     * @return Formatted ticket information string
     */
    private String createShareText() {
        StringBuilder shareText = new StringBuilder();
        shareText.append("🎫 TOUR BOOKING TICKET 🎫\n\n");
        shareText.append("Booking Reference: ").append(currentBooking.getBookingReference()).append("\n");
        shareText.append("Tour: ").append(bookedTour.getTourName()).append("\n");
        shareText.append("Location: ").append(bookedTour.getTourLocation()).append("\n");
        shareText.append("Date: ").append(dateFormatter.format(new Date(bookedTour.getTourTime()))).append("\n");
        shareText.append("Duration: ").append(bookedTour.getDuration()).append(" days\n");
        shareText.append("Number of People: ").append(currentBooking.getNumberOfPeople()).append("\n");
        shareText.append("Total Amount: ").append(currencyFormatter.format(currentBooking.getTotalAmount())).append("\n");
        shareText.append("Status: ").append(currentBooking.getBookingStatus()).append("\n\n");
        shareText.append("Customer: ").append(customer.getFullName()).append("\n");
        shareText.append("Email: ").append(customer.getEmail()).append("\n");
        shareText.append("Phone: ").append(customer.getPhoneNumber()).append("\n\n");
        shareText.append("Thank you for choosing our tour service!");
        return shareText.toString();
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
