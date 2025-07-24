package com.example.tourmanagement.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tourmanagement.R;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Tour;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for adding new tours or editing existing tours.
 * Provides a comprehensive form for tour management with date/time pickers.
 *
 * Features:
 * - Add new tour with complete information
 * - Edit existing tour details
 * - Date and time picker for tour scheduling
 * - Image URL input for tour visuals
 * - Input validation and error handling
 * - Active/inactive tour status management
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class AddEditTourActivity extends AppCompatActivity {

    /**
     * UI components for tour form
     */
    private EditText etTourName, etTourLocation, etTourDescription, etTourCost;
    private EditText etNumberOfPeople, etDuration, etTourImage;
    private Button btnSelectDateTime, btnSaveTour, btnCancel;
    private Switch switchActive;
    private ImageView ivPreviewImage;

    /**
     * Database and data management
     */
    private TourManagementDatabase database;
    private Tour editingTour = null;
    private long selectedDateTime = 0;
    private SimpleDateFormat dateTimeFormatter;
    private boolean isEditMode = false;

    /**
     * Called when the activity is first created.
     * Initializes UI components and determines if in add or edit mode.
     *
     * @param savedInstanceState Bundle containing saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_tour);

        // Initialize database and formatter
        database = TourManagementDatabase.getDatabase(this);
        dateTimeFormatter = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

        // Check if editing existing tour
        checkEditMode();

        // Initialize UI components
        initializeViews();

        // Load tour data if editing
        if (isEditMode && editingTour != null) {
            populateFormWithTourData();
        }

        // Setup event listeners
        setupEventListeners();
    }

    /**
     * Checks if activity was launched in edit mode
     */
    private void checkEditMode() {
        int tourId = getIntent().getIntExtra("tour_id", -1);
        if (tourId != -1) {
            isEditMode = true;
            editingTour = database.tourDao().getTourById(tourId);
            if (editingTour == null) {
                showToast("Tour not found");
                finish();
                return;
            }
        }
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        etTourName = findViewById(R.id.et_tour_name);
        etTourLocation = findViewById(R.id.et_tour_location);
        etTourDescription = findViewById(R.id.et_tour_description);
        etTourCost = findViewById(R.id.et_tour_cost);
        etNumberOfPeople = findViewById(R.id.et_number_of_people);
        etDuration = findViewById(R.id.et_duration);
        etTourImage = findViewById(R.id.et_tour_image);
        btnSelectDateTime = findViewById(R.id.btn_select_date_time);
        btnSaveTour = findViewById(R.id.btn_save_tour);
        btnCancel = findViewById(R.id.btn_cancel);
        switchActive = findViewById(R.id.switch_active);
        ivPreviewImage = findViewById(R.id.iv_preview_image);

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Edit Tour" : "Add New Tour");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set button text based on mode
        btnSaveTour.setText(isEditMode ? "Update Tour" : "Create Tour");
    }

    /**
     * Populates form with existing tour data when editing
     */
    private void populateFormWithTourData() {
        if (editingTour != null) {
            etTourName.setText(editingTour.getTourName());
            etTourLocation.setText(editingTour.getTourLocation());
            etTourDescription.setText(editingTour.getTourDescription());
            etTourCost.setText(String.valueOf(editingTour.getTourCost()));
            etNumberOfPeople.setText(String.valueOf(editingTour.getNumberOfPeoples()));
            etDuration.setText(String.valueOf(editingTour.getDuration()));
            etTourImage.setText(editingTour.getTourImage());
            switchActive.setChecked(editingTour.isActive());

            selectedDateTime = editingTour.getTourTime();
            updateDateTimeButton();
        }
    }

    /**
     * Sets up event listeners for UI components
     */
    private void setupEventListeners() {
        // Date/Time picker button
        btnSelectDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        // Save tour button
        btnSaveTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTour();
            }
        });

        // Cancel button
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Image URL preview (optional feature)
        etTourImage.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // Could implement image preview here
                updateImagePreview();
            }
        });
    }

    /**
     * Shows date and time picker dialogs
     */
    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        // If editing and has existing date, use that as default
        if (selectedDateTime > 0) {
            calendar.setTimeInMillis(selectedDateTime);
        }

        // Show date picker first
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                // After date is selected, show time picker
                showTimePicker(year, month, dayOfMonth);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Shows time picker dialog after date is selected
     *
     * @param year Selected year
     * @param month Selected month
     * @param dayOfMonth Selected day
     */
    private void showTimePicker(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                // Create calendar with selected date and time
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                selectedDateTime = selectedCalendar.getTimeInMillis();

                updateDateTimeButton();
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        );

        timePickerDialog.show();
    }

    /**
     * Updates the date/time button text with selected date
     */
    private void updateDateTimeButton() {
        if (selectedDateTime > 0) {
            Date date = new Date(selectedDateTime);
            btnSelectDateTime.setText(dateTimeFormatter.format(date));
        }
    }

    /**
     * Updates image preview (placeholder implementation)
     */
    private void updateImagePreview() {
        String imageUrl = etTourImage.getText().toString().trim();
        if (!imageUrl.isEmpty()) {
            // Could use Glide here to load image preview
            // For now, just show a placeholder
            ivPreviewImage.setImageResource(R.drawable.placeholder_tour);
            ivPreviewImage.setVisibility(View.VISIBLE);
        } else {
            ivPreviewImage.setVisibility(View.GONE);
        }
    }

    /**
     * Validates and saves the tour
     */
    private void saveTour() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        try {
            // Create or update tour object
            Tour tour = isEditMode ? editingTour : new Tour();

            // Set tour properties from form
            tour.setTourName(etTourName.getText().toString().trim());
            tour.setTourLocation(etTourLocation.getText().toString().trim());
            tour.setTourDescription(etTourDescription.getText().toString().trim());
            tour.setTourCost(Double.parseDouble(etTourCost.getText().toString().trim()));
            tour.setNumberOfPeoples(Integer.parseInt(etNumberOfPeople.getText().toString().trim()));
            tour.setDuration(Integer.parseInt(etDuration.getText().toString().trim()));
            tour.setTourImage(etTourImage.getText().toString().trim());
            tour.setTourTime(selectedDateTime);
            tour.setActive(switchActive.isChecked());

            // Save to database
            if (isEditMode) {
                database.tourDao().updateTour(tour);
                showToast("Tour updated successfully!");
            } else {
                long tourId = database.tourDao().insertTour(tour);
                if (tourId > 0) {
                    showToast("Tour created successfully!");
                } else {
                    showToast("Failed to create tour");
                    return;
                }
            }

            // Return to previous screen
            setResult(RESULT_OK);
            finish();

        } catch (Exception e) {
            showToast("Error saving tour: " + e.getMessage());
        }
    }

    /**
     * Validates all form inputs
     *
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInput() {
        // Tour name validation
        String tourName = etTourName.getText().toString().trim();
        if (tourName.isEmpty()) {
            etTourName.setError("Tour name is required");
            etTourName.requestFocus();
            return false;
        }

        // Location validation
        String location = etTourLocation.getText().toString().trim();
        if (location.isEmpty()) {
            etTourLocation.setError("Tour location is required");
            etTourLocation.requestFocus();
            return false;
        }

        // Description validation
        String description = etTourDescription.getText().toString().trim();
        if (description.isEmpty()) {
            etTourDescription.setError("Tour description is required");
            etTourDescription.requestFocus();
            return false;
        }

        // Cost validation
        String costStr = etTourCost.getText().toString().trim();
        if (costStr.isEmpty()) {
            etTourCost.setError("Tour cost is required");
            etTourCost.requestFocus();
            return false;
        }
        try {
            double cost = Double.parseDouble(costStr);
            if (cost <= 0) {
                etTourCost.setError("Cost must be greater than 0");
                etTourCost.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etTourCost.setError("Invalid cost format");
            etTourCost.requestFocus();
            return false;
        }

        // Number of people validation
        String peopleStr = etNumberOfPeople.getText().toString().trim();
        if (peopleStr.isEmpty()) {
            etNumberOfPeople.setError("Number of people is required");
            etNumberOfPeople.requestFocus();
            return false;
        }
        try {
            int people = Integer.parseInt(peopleStr);
            if (people <= 0) {
                etNumberOfPeople.setError("Must allow at least 1 person");
                etNumberOfPeople.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etNumberOfPeople.setError("Invalid number format");
            etNumberOfPeople.requestFocus();
            return false;
        }

        // Duration validation
        String durationStr = etDuration.getText().toString().trim();
        if (durationStr.isEmpty()) {
            etDuration.setError("Duration is required");
            etDuration.requestFocus();
            return false;
        }
        try {
            int duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                etDuration.setError("Duration must be at least 1 day");
                etDuration.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etDuration.setError("Invalid duration format");
            etDuration.requestFocus();
            return false;
        }

        // Date/time validation
        if (selectedDateTime <= 0) {
            showToast("Please select tour date and time");
            btnSelectDateTime.requestFocus();
            return false;
        }

        // Check if selected date is in the future
        if (selectedDateTime <= System.currentTimeMillis()) {
            showToast("Tour date must be in the future");
            btnSelectDateTime.requestFocus();
            return false;
        }

        return true;
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
