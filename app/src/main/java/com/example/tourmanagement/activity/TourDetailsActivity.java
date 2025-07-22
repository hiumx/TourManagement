package com.example.tourmanagement.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.tourmanagement.R;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Tour;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for displaying detailed tour information.
 * Shows complete tour details and provides booking option.
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class TourDetailsActivity extends AppCompatActivity {

    private ImageView ivTourImage;
    private TextView tvTourName, tvTourLocation, tvTourDate, tvTourDescription;
    private TextView tvTourCost, tvDuration, tvAvailableSlots;
    private Button btnBookTour;

    private TourManagementDatabase database;
    private Tour currentTour;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_details);

        database = TourManagementDatabase.getDatabase(this);
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        dateFormatter = new SimpleDateFormat("EEEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

        initializeViews();
        loadTourData();
        setupEventListeners();
    }

    private void initializeViews() {
        ivTourImage = findViewById(R.id.iv_tour_image);
        tvTourName = findViewById(R.id.tv_tour_name);
        tvTourLocation = findViewById(R.id.tv_tour_location);
        tvTourDate = findViewById(R.id.tv_tour_date);
        tvTourDescription = findViewById(R.id.tv_tour_description);
        tvTourCost = findViewById(R.id.tv_tour_cost);
        tvDuration = findViewById(R.id.tv_duration);
        tvAvailableSlots = findViewById(R.id.tv_available_slots);
        btnBookTour = findViewById(R.id.btn_book_tour);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tour Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadTourData() {
        int tourId = getIntent().getIntExtra("TOUR_ID", -1);
        if (tourId != -1) {
            currentTour = database.tourDao().getTourById(tourId);
            if (currentTour != null) {
                displayTourInfo();
            } else {
                showToast("Tour not found");
                finish();
            }
        } else {
            showToast("Invalid tour ID");
            finish();
        }
    }

    private void displayTourInfo() {
        tvTourName.setText(currentTour.getTourName());
        tvTourLocation.setText(currentTour.getTourLocation());
        tvTourDescription.setText(currentTour.getTourDescription());
        tvTourCost.setText("Price: " + currencyFormatter.format(currentTour.getTourCost()) + " per person");
        tvDuration.setText("Duration: " + currentTour.getDuration() + " days");

        Date tourDate = new Date(currentTour.getTourTime());
        tvTourDate.setText("Date: " + dateFormatter.format(tourDate));

        int availableSlots = currentTour.getAvailableSlots();
        tvAvailableSlots.setText("Available slots: " + availableSlots);

        if (currentTour.getTourImage() != null && !currentTour.getTourImage().isEmpty()) {
            Glide.with(this)
                .load(currentTour.getTourImage())
                .placeholder(R.drawable.placeholder_tour)
                .error(R.drawable.placeholder_tour)
                .into(ivTourImage);
        } else {
            ivTourImage.setImageResource(R.drawable.placeholder_tour);
        }

        if (availableSlots > 0) {
            btnBookTour.setEnabled(true);
            btnBookTour.setText("Book This Tour");
        } else {
            btnBookTour.setEnabled(false);
            btnBookTour.setText("Fully Booked");
        }
    }

    private void setupEventListeners() {
        btnBookTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTour.hasAvailableSlots()) {
                    Intent intent = new Intent(TourDetailsActivity.this, BookTourActivity.class);
                    intent.putExtra("TOUR_ID", currentTour.getId());
                    intent.putExtra("USER_ID", getSharedPreferences("TourManagementPrefs", MODE_PRIVATE)
                            .getInt("user_id", -1));
                    startActivity(intent);
                } else {
                    showToast("Sorry, this tour is fully booked");
                }
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
