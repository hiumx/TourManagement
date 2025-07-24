package com.example.tourmanagement.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.example.tourmanagement.R;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Booking;
import com.example.tourmanagement.model.Tour;
import com.example.tourmanagement.model.User;
import com.example.tourmanagement.model.Discount;
import com.example.tourmanagement.utils.EmailService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Activity for booking tours with QR code payment functionality.
 *
 * Features:
 * - Tour booking form with number of people selection
 * - Real-time total cost calculation
 * - QR code generation for payment
 * - Payment confirmation and booking creation
 * - Booking reference generation
 * - Navigate to ticket display after successful booking
 *
 * UI Components:
 * - Tour information display
 * - Number of people selector
 * - Total cost display
 * - QR code image for payment
 * - Confirm booking button
 * - Notes input field
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class BookTourActivity extends AppCompatActivity {

    /**
     * UI components
     */
    private TextView tvTourName, tvTourCost, tvTotalCost, tvAvailableSlots;
    private EditText etNumberOfPeople, etNotes;
    private ImageView ivQRCode;
    private Button btnConfirmBooking, btnIncrement, btnDecrement, btnOpenMoMo;

    /**
     * Database and data objects
     */
    private TourManagementDatabase database;
    private Tour selectedTour;
    private User currentUser;
    private int numberOfPeople = 1;
    private double totalCost = 0.0;

    /**
     * Formatters
     */
    private NumberFormat currencyFormatter;

    /**
     * Exchange rate constant (USD to VND)
     * In a real app, this should be fetched from an API
     */
    private static final double USD_TO_VND_RATE = 24000.0;

    /**
     * Bank account information for QR code payments
     */
    private static final String BANK_ACCOUNT_NUMBER = "0123456789";
    private static final String BANK_CODE = "970422"; // SCB Bank code
    private static final String BANK_NAME = "SCB";
    private static final String ACCOUNT_HOLDER = "TOUR MANAGEMENT";

    /**
     * Called when the activity is first created.
     * Initializes UI components and loads tour/user data.
     *
     * @param savedInstanceState Bundle containing saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_tour);

        // Initialize database and formatter
        database = TourManagementDatabase.getDatabase(this);
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        // Initialize UI components first
        initializeViews();

        // Setup event listeners
        setupEventListeners();

        // Get tour and user data from intent - this will handle UI updates after data loads
        loadDataFromIntent();
    }

    /**
     * Loads tour and user data from intent extras
     */
    private void loadDataFromIntent() {
        int tourId = getIntent().getIntExtra("TOUR_ID", -1);
        int userId = getIntent().getIntExtra("USER_ID", -1);

        // Debug logging to see what we received
        android.util.Log.d("BookTourActivity", "Received tour ID: " + tourId);
        android.util.Log.d("BookTourActivity", "Received user ID: " + userId);

        if (tourId == -1) {
            showToast("Invalid tour ID");
            finish();
            return;
        }

        // Use background thread for database access
        new Thread(() -> {
            selectedTour = database.tourDao().getTourById(tourId);

            android.util.Log.d("BookTourActivity", "Tour found in database: " + (selectedTour != null));
            if (selectedTour != null) {
                android.util.Log.d("BookTourActivity", "Tour name: " + selectedTour.getTourName());
            }

            runOnUiThread(() -> {
                if (selectedTour == null) {
                    showToast("Tour not found in database. Tour ID: " + tourId);
                    finish();
                    return;
                }

                // Now that we have the tour data, initialize the UI
                displayTourInfo();
                calculateTotalCost();
                generateQRCode();
            });
        }).start();

        // For now, create a mock user if no user ID is provided
        // TODO: Replace with proper user authentication system
        if (userId != -1) {
            currentUser = database.userDao().getUserById(userId);
        } else {
            // Create a temporary user for testing
            currentUser = createMockUser();
        }

        if (currentUser == null) {
            showToast("Error loading user data");
            finish();
        }
    }

    /**
     * Creates a mock user for testing purposes
     * TODO: Replace with proper user authentication
     */
    private User createMockUser() {
        User mockUser = new User();
        mockUser.setId(1); // Mock ID
        mockUser.setFullName("John Doe");
        mockUser.setEmail("john.doe@example.com");
        mockUser.setPhoneNumber("+1234567890");
        mockUser.setUsername("testuser");
        return mockUser;
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        tvTourName = findViewById(R.id.tv_tour_name);
        tvTourCost = findViewById(R.id.tv_tour_cost);
        tvTotalCost = findViewById(R.id.tv_total_cost);
        tvAvailableSlots = findViewById(R.id.tv_available_slots);
        etNumberOfPeople = findViewById(R.id.et_number_of_people);
        etNotes = findViewById(R.id.et_notes);
        ivQRCode = findViewById(R.id.iv_qr_code);
        btnConfirmBooking = findViewById(R.id.btn_confirm_booking);
        btnIncrement = findViewById(R.id.btn_increment);
        btnDecrement = findViewById(R.id.btn_decrement);
        btnOpenMoMo = findViewById(R.id.btn_open_momo);

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Book Tour");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Displays tour information in the UI
     */
    private void displayTourInfo() {
        if (selectedTour != null) {
            tvTourName.setText(selectedTour.getTourName());
            tvTourCost.setText("Cost per person: " + currencyFormatter.format(selectedTour.getTourCost()));
            tvAvailableSlots.setText("Available slots: " + selectedTour.getAvailableSlots());

            // Set initial number of people
            etNumberOfPeople.setText(String.valueOf(numberOfPeople));
        }
    }

    /**
     * Sets up event listeners for UI components
     */
    private void setupEventListeners() {
        // Increment button
        btnIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementPeople();
            }
        });

        // Decrement button
        btnDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementPeople();
            }
        });

        // Confirm booking button
        btnConfirmBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmBooking();
            }
        });

        // Open MoMo button
        btnOpenMoMo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMoMo();
            }
        });
    }

    /**
     * Increments the number of people (with validation)
     */
    private void incrementPeople() {
        if (numberOfPeople < selectedTour.getAvailableSlots()) {
            numberOfPeople++;
            updatePeopleCount();
        } else {
            showToast("Cannot exceed available slots");
        }
    }

    /**
     * Decrements the number of people (with validation)
     */
    private void decrementPeople() {
        if (numberOfPeople > 1) {
            numberOfPeople--;
            updatePeopleCount();
        } else {
            showToast("Minimum 1 person required");
        }
    }

    /**
     * Updates the people count display and recalculates costs
     */
    private void updatePeopleCount() {
        etNumberOfPeople.setText(String.valueOf(numberOfPeople));
        calculateTotalCost();
        generateQRCode();
    }

    /**
     * Calculates and displays the total cost with discount applied
     */
    private void calculateTotalCost() {
        if (selectedTour != null) {
            double originalPrice = selectedTour.getTourCost() * numberOfPeople;

            // Get best available discount for this tour
            Discount bestDiscount = database.discountDao().getBestDiscountForTour(
                selectedTour.getId(),
                originalPrice,
                System.currentTimeMillis()
            );

            if (bestDiscount != null && bestDiscount.isValid()) {
                // Apply discount
                totalCost = bestDiscount.applyDiscount(originalPrice);

                // Show discount information
                double savings = originalPrice - totalCost;
                String costText = "Original: " + currencyFormatter.format(originalPrice) + "\n" +
                                "Discount: " + bestDiscount.getDiscountName() + "\n" +
                                "You save: " + currencyFormatter.format(savings) + "\n" +
                                "Total: " + currencyFormatter.format(totalCost);
                tvTotalCost.setText(costText);
            } else {
                // No discount available
                totalCost = originalPrice;
                tvTotalCost.setText("Total Cost: " + currencyFormatter.format(totalCost));
            }
        }
    }

    /**
     * Generates QR code for payment with tour and cost information
     */
    private void generateQRCode() {
        try {
            // Create QR code content with payment information
            String qrContent = createPaymentQRCodeContent();

            // Generate QR code bitmap
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrContent, BarcodeFormat.QR_CODE, 400, 400);

            // Display QR code
            ivQRCode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            showToast("Error generating QR code: " + e.getMessage());
        }
    }

    /**
     * Creates Vietnamese banking QR code content for payment
     * Compatible with MoMo wallet and Vietnamese banking apps
     *
     * @return QR code content string in VietQR format
     */
    private String createPaymentQRCodeContent() {
        // Use MoMo format for better compatibility
        return createMoMoQRCodeContent();
    }

    /**
     * Creates MoMo-compatible QR code content for payment
     * This format is specifically designed for MoMo wallet scanning with auto-fill amount
     *
     * @return MoMo-compatible QR code content
     */
    private String createMoMoQRCodeContent() {
        // Convert USD to VND with proper formatting
        double amountVND = totalCost * USD_TO_VND_RATE;
        long amountVNDLong = Math.round(amountVND);

        // Debug logging to check the amounts
        android.util.Log.d("BookTourActivity", "USD Total Cost: " + totalCost);
        android.util.Log.d("BookTourActivity", "VND Amount: " + amountVNDLong);
        android.util.Log.d("BookTourActivity", "Exchange Rate: " + USD_TO_VND_RATE);

        // Ensure minimum amount (avoid 0 or very small amounts)
        if (amountVNDLong < 1000) {
            amountVNDLong = 50000; // Default to 50,000 VND if amount is too small
            android.util.Log.d("BookTourActivity", "Amount too small, using default: " + amountVNDLong);
        }

        // Create MoMo QR Pay format (this is the correct format for MoMo scanning)
        // Format: 2|1|phone|name|amount|bank_code|reserved|description|type
//        StringBuilder momoQR = new StringBuilder();
//        momoQR.append("2|1|");  // Version and Payment type
//        momoQR.append("0896210393|");  // Phone number (replace with actual MoMo phone)
//        momoQR.append("MAI XUAN HIEU|");  // Account holder name (replace with actual name)
//        momoQR.append(amountVNDLong).append("|");  // Amount in VND (correct position for auto-fill)
//        momoQR.append("108|");  // Bank code for VietinBank (MoMo compatible)
//        momoQR.append("0|");    // Reserved field
//        momoQR.append(encodeForMoMo("Tour: " + selectedTour.getTourName() + " - " + numberOfPeople + " people")).append("|");  // Description
//        momoQR.append("transfer");  // Transaction type
//
//        String finalQR = momoQR.toString();
//        android.util.Log.d("BookTourActivity", "Generated QR Code: " + finalQR);

        String phoneNumber = "0896210393"; // MoMo phone number
        String recipientName = "MAI XUAN HIEU"; // Account holder name
        long amount = amountVNDLong; // Amount in VND
        String bankCode = "108"; // VietinBank
        String note = "Tour: " + selectedTour.getTourName() + " - " + numberOfPeople + " people";

// URL encode the note to make it QR safe
        String encodedNote = "";
        try {
            encodedNote = URLEncoder.encode(note, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder momoQR = new StringBuilder();
        momoQR.append("2|1|");
        momoQR.append(phoneNumber).append("|");
        momoQR.append(recipientName).append("|");
        momoQR.append(amount).append("|");
        momoQR.append(bankCode).append("|");
        momoQR.append("0|"); // Reserved
        momoQR.append(encodedNote).append("|"); // 💬 Lời nhắn here!
        momoQR.append("transfer");

        String finalQR = momoQR.toString();
        Log.d("BookTourActivity", "Generated MoMo QR Code: " + finalQR);




        return finalQR;
    }

    /**
     * Creates enhanced MoMo deep link format for auto-fill
     * Alternative format that directly opens MoMo with pre-filled amount
     *
     * @return MoMo deep link with auto-fill amount
     */
    private String createMoMoDeepLink() {
        // Convert USD to VND
        double amountVND = totalCost * USD_TO_VND_RATE;
        long amountVNDLong = Math.round(amountVND);

        // Create MoMo deep link that auto-fills amount and description
        StringBuilder deepLink = new StringBuilder();
        deepLink.append("momo://transfer");
        deepLink.append("?receiver=0896210393");
        deepLink.append("&amount=").append(amountVNDLong);
        deepLink.append("&note=").append(encodeForUrl("Tour: " + selectedTour.getTourName() + " - " + numberOfPeople + " people"));
        deepLink.append("&fee=0");

        return deepLink.toString();
    }

    /**
     * Creates banking app compatible QR with auto-fill amount
     * Universal format that works with most Vietnamese banking apps
     *
     * @return Banking QR code with auto-fill amount
     */
    private String createBankingQRCode() {
        // Convert USD to VND
        double amountVND = totalCost * USD_TO_VND_RATE;
        long amountVNDLong = Math.round(amountVND);

        // Create JSON format that banking apps can parse for auto-fill
        StringBuilder bankingQR = new StringBuilder();
        bankingQR.append("{");
        bankingQR.append("\"bankCode\":\"").append(BANK_CODE).append("\",");
        bankingQR.append("\"accountNumber\":\"").append(BANK_ACCOUNT_NUMBER).append("\",");
        bankingQR.append("\"accountName\":\"").append(escapeJson(ACCOUNT_HOLDER)).append("\",");
        bankingQR.append("\"amount\":").append(amountVNDLong).append(",");
        bankingQR.append("\"currency\":\"VND\",");
        bankingQR.append("\"content\":\"").append(escapeJson("Tour: " + selectedTour.getTourName() + " - " + numberOfPeople + " people")).append("\",");
        bankingQR.append("\"format\":\"TRANSFER\"");
        bankingQR.append("}");

        return bankingQR.toString();
    }

    /**
     * Encodes text for MoMo format (removes special characters)
     *
     * @param text Text to encode
     * @return MoMo-safe text
     */
    private String encodeForMoMo(String text) {
        if (text == null) return "";
        // Remove or replace characters that might cause issues in MoMo
        return text.replaceAll("[|\\n\\r]", " ")
                  .replaceAll("\\s+", " ")
                  .trim();
    }

    /**
     * Escapes JSON special characters
     *
     * @param text Text to escape
     * @return JSON-safe text
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"")
                  .replace("\\", "\\\\")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Confirms the booking and creates booking record
     */
    private void confirmBooking() {
        // Validate booking
        if (!validateBooking()) {
            return;
        }

        // Use background thread for database operations
        new Thread(() -> {
            try {
                // Create booking object
                Booking booking = new Booking(
                        currentUser.getId(),
                        selectedTour.getId(),
                        numberOfPeople,
                        totalCost
                );

                // Set additional booking details
                booking.setNotes(etNotes.getText().toString().trim());
                booking.setQrCode(createPaymentQRCodeContent());
                booking.setBookingStatus("PENDING"); // Changed from CONFIRMED to PENDING
                booking.setPaymentStatus("PENDING"); // Changed from PAID to PENDING - wait for admin confirmation

                // Insert booking into database
                long bookingId = database.bookingDao().insertBooking(booking);

                if (bookingId > 0) {
                    // Update tour booking count
                    database.tourDao().updateBookingCount(selectedTour.getId(), numberOfPeople);

                    // Update booking with the generated ID
                    booking.setId((int) bookingId);

                    // Switch back to main thread for UI operations
                    runOnUiThread(() -> {
                        showToast("Booking confirmed successfully!");

                        // Navigate to ticket display immediately
                        navigateToTicket(bookingId);

                        // Send booking confirmation email in background (don't wait for it)
                        sendBookingConfirmationEmail(booking);
                    });
                } else {
                    runOnUiThread(() -> {
                        showToast("Failed to create booking. Please try again.");
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showToast("Booking error: " + e.getMessage());
                    Log.e("BookTourActivity", "Booking error", e);
                });
            }
        }).start();
    }

    /**
     * Validates booking before confirmation
     *
     * @return true if booking is valid, false otherwise
     */
    private boolean validateBooking() {
        if (numberOfPeople > selectedTour.getAvailableSlots()) {
            showToast("Not enough available slots");
            return false;
        }

        if (numberOfPeople < 1) {
            showToast("At least 1 person required");
            return false;
        }

        return true;
    }

    /**
     * Navigates to ticket display activity
     *
     * @param bookingId ID of the created booking
     */
    private void navigateToTicket(long bookingId) {
        Intent intent = new Intent(this, TicketActivity.class);
        // Fix: Convert long to int since our Booking model uses int for ID
        intent.putExtra("booking_id", (int) bookingId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add debug logging
        android.util.Log.d("BookTourActivity", "Navigating to ticket with booking ID: " + bookingId);

        startActivity(intent);
        finish();
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

    /**
     * Calculates CRC16-CCITT checksum for QR code
     *
     * @param data Data to calculate checksum for
     * @return 4-digit hexadecimal CRC
     */
    private String calculateCRC16(String data) {
        int crc = 0xFFFF;
        byte[] bytes = data.getBytes();

        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc = crc << 1;
                }
                crc &= 0xFFFF;
            }
        }

        return String.format("%04X", crc);
    }

    /**
     * URL encodes a string for use in URLs
     *
     * @param text Text to encode
     * @return URL-encoded text
     */
    private String encodeForUrl(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text.replaceAll(" ", "%20");
        }
    }

    /**
     * Opens the MoMo app with pre-filled payment information
     * This uses the deep link format for direct app opening
     */
    private void openMoMo() {
        try {
            String deepLink = createMoMoDeepLink();
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(deepLink));
            startActivity(intent);
        } catch (Exception e) {
            showToast("Error opening MoMo: " + e.getMessage());
        }
    }

    /**
     * Sends a booking confirmation email to the user with detailed ticket information
     *
     * @param booking The booking object containing details
     */
    private void sendBookingConfirmationEmail(Booking booking) {
        // Check if user has a valid email address
        if (currentUser.getEmail() == null || currentUser.getEmail().trim().isEmpty()) {
            Log.w("BookTourActivity", "User email is empty, skipping email notification");
            return;
        }

        // Set the booking ID for the confirmation email
        booking.setId((int) booking.getId());

        // Send email using the enhanced EmailService
        EmailService.sendBookingConfirmationEmail(currentUser, selectedTour, booking, new EmailService.EmailCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Log.d("BookTourActivity", "Booking confirmation email sent successfully");
                    // Optional: Show a subtle notification to user
                    showToast("Confirmation email sent to " + currentUser.getEmail());
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Log.e("BookTourActivity", "Failed to send booking confirmation email: " + error);
                    // Don't show error to user as email is secondary feature
                    // The booking is still successful even if email fails
                });
            }
        });
    }
}
