package com.example.tourmanagement.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tourmanagement.R;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Tour;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity for adding new tours (Admin only)
 * Allows admin users to create new tour packages
 */
public class AddTourActivity extends AppCompatActivity {

    private EditText etTourName, etTourDescription, etTourLocation, etTourCost, etAvailableSlots, etDuration, etTourImage, etTourTime;
    private Button btnSaveTour, btnCancel;
    private TourManagementDatabase database;

    // Calendar instance to store selected date and time
    private Calendar selectedDateTime;
    private SimpleDateFormat dateTimeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tour);

        // Initialize database
        database = TourManagementDatabase.getDatabase(this);

        // Initialize views
        initializeViews();

        // Setup event listeners
        setupEventListeners();

        // Setup toolbar
        setupToolbar();

        // Initialize calendar and date/time format
        selectedDateTime = Calendar.getInstance();
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    private void initializeViews() {
        etTourName = findViewById(R.id.et_tour_name);
        etTourDescription = findViewById(R.id.et_tour_description);
        etTourLocation = findViewById(R.id.et_tour_location);
        etTourCost = findViewById(R.id.et_tour_cost);
        etAvailableSlots = findViewById(R.id.et_available_slots);
        etDuration = findViewById(R.id.et_duration);
        etTourImage = findViewById(R.id.et_tour_image);
        etTourTime = findViewById(R.id.et_tour_time);
        btnSaveTour = findViewById(R.id.btn_save_tour);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupEventListeners() {
        btnSaveTour.setOnClickListener(v -> saveTour());
        btnCancel.setOnClickListener(v -> finish());

        // Setup date and time picker for tour time field
        etTourTime.setOnClickListener(v -> showDateTimePicker());
        etTourTime.setFocusable(false); // Prevent keyboard from showing
        etTourTime.setClickable(true);
    }

    /**
     * Shows date picker followed by time picker
     */
    private void showDateTimePicker() {
        Calendar currentDate = Calendar.getInstance();

        // Show date picker first
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // After date is selected, show time picker
                    showTimePicker();
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH));

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Shows time picker and updates the tour time field
     */
    private void showTimePicker() {
        Calendar currentTime = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);

                    // Update the EditText with formatted date and time
                    etTourTime.setText(dateTimeFormat.format(selectedDateTime.getTime()));
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                true); // Use 24-hour format

        timePickerDialog.show();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Tour");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void saveTour() {
        // Get input values
        String tourName = etTourName.getText().toString().trim();
        String tourDescription = etTourDescription.getText().toString().trim();
        String tourLocation = etTourLocation.getText().toString().trim();
        String tourCostStr = etTourCost.getText().toString().trim();
        String availableSlotsStr = etAvailableSlots.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        String tourImage = etTourImage.getText().toString().trim();
        String tourTimeStr = etTourTime.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(tourName, tourDescription, tourLocation, tourCostStr, availableSlotsStr, duration, tourImage, tourTimeStr)) {
            return;
        }

        try {
            // Parse numeric values
            double tourCost = Double.parseDouble(tourCostStr);
            int availableSlots = Integer.parseInt(availableSlotsStr);

            // Use selected date/time from calendar
            long tourTime = selectedDateTime.getTimeInMillis();

            // Create new tour with complete information
            Tour newTour = new Tour();
            newTour.setTourName(tourName);
            newTour.setTourDescription(tourDescription);
            newTour.setTourLocation(tourLocation);
            newTour.setTourCost(tourCost);
            newTour.setNumberOfPeoples(availableSlots);
            newTour.setDuration(Integer.parseInt(duration.replaceAll("[^0-9]", "1"))); // Extract number or default to 1
            newTour.setTourImage(tourImage);
            newTour.setTourTime(tourTime);
            newTour.setCreatedAt(System.currentTimeMillis());
            newTour.setActive(true);

            // Save to database
            long tourId = database.tourDao().insertTour(newTour);

            if (tourId > 0) {
                Toast.makeText(this, "Tour created successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Close activity and return to previous screen
            } else {
                Toast.makeText(this, "Failed to create tour", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for cost and slots", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating tour: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String tourName, String tourDescription, String tourLocation,
                                   String tourCost, String availableSlots, String duration, String tourImage, String tourTime) {
        if (tourName.isEmpty()) {
            etTourName.setError("Tour name is required");
            etTourName.requestFocus();
            return false;
        }

        if (tourDescription.isEmpty()) {
            etTourDescription.setError("Tour description is required");
            etTourDescription.requestFocus();
            return false;
        }

        if (tourLocation.isEmpty()) {
            etTourLocation.setError("Tour location is required");
            etTourLocation.requestFocus();
            return false;
        }

        if (tourCost.isEmpty()) {
            etTourCost.setError("Tour cost is required");
            etTourCost.requestFocus();
            return false;
        }

        if (availableSlots.isEmpty()) {
            etAvailableSlots.setError("Available slots is required");
            etAvailableSlots.requestFocus();
            return false;
        }

        if (duration.isEmpty()) {
            etDuration.setError("Duration is required");
            etDuration.requestFocus();
            return false;
        }

        if (tourImage.isEmpty()) {
            etTourImage.setError("Tour image URL is required");
            etTourImage.requestFocus();
            return false;
        }

        // Validate that a date and time has been selected
        if (tourTime.isEmpty()) {
            etTourTime.setError("Please select tour date and time");
            etTourTime.requestFocus();
            return false;
        }

        // Validate that selected date/time is in the future
        if (selectedDateTime.getTimeInMillis() <= System.currentTimeMillis()) {
            etTourTime.setError("Tour time must be in the future");
            etTourTime.requestFocus();
            return false;
        }

        try {
            double cost = Double.parseDouble(tourCost);
            if (cost <= 0) {
                etTourCost.setError("Cost must be greater than 0");
                etTourCost.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etTourCost.setError("Please enter a valid cost");
            etTourCost.requestFocus();
            return false;
        }

        try {
            int slots = Integer.parseInt(availableSlots);
            if (slots <= 0) {
                etAvailableSlots.setError("Slots must be greater than 0");
                etAvailableSlots.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etAvailableSlots.setError("Please enter a valid number");
            etAvailableSlots.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
