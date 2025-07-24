package com.example.tourmanagement.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.tourmanagement.R;
import com.example.tourmanagement.adapter.DiscountManagementAdapter;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Discount;
import java.util.List;

/**
 * Activity for managing discounts (Admin only)
 * Allows admins to view, create, edit, and delete discounts
 */
public class DiscountManagementActivity extends AppCompatActivity
    implements DiscountManagementAdapter.OnDiscountActionListener {

    private RecyclerView recyclerViewDiscounts;
    private DiscountManagementAdapter discountAdapter;
    private FloatingActionButton fabAddDiscount;
    private TextView tvNoDiscounts;
    private TourManagementDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_management);

        // Initialize database
        database = TourManagementDatabase.getDatabase(this);

        // Setup toolbar
        setupToolbar();

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load discounts
        loadDiscounts();

        // Setup event listeners
        setupEventListeners();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Discount Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeViews() {
        recyclerViewDiscounts = findViewById(R.id.recycler_view_discounts);
        fabAddDiscount = findViewById(R.id.fab_add_discount);
        tvNoDiscounts = findViewById(R.id.tv_no_discounts);
    }

    private void setupRecyclerView() {
        recyclerViewDiscounts.setLayoutManager(new LinearLayoutManager(this));
        discountAdapter = new DiscountManagementAdapter(this, this);
        recyclerViewDiscounts.setAdapter(discountAdapter);
    }

    private void loadDiscounts() {
        try {
            List<Discount> discounts = database.discountDao().getAllDiscounts();
            discountAdapter.updateDiscounts(discounts);

            if (discounts.isEmpty()) {
                tvNoDiscounts.setVisibility(View.VISIBLE);
                recyclerViewDiscounts.setVisibility(View.GONE);
            } else {
                tvNoDiscounts.setVisibility(View.GONE);
                recyclerViewDiscounts.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading discounts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupEventListeners() {
        fabAddDiscount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditDiscountActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onEditDiscount(Discount discount) {
        Intent intent = new Intent(this, AddEditDiscountActivity.class);
        intent.putExtra("discount_id", discount.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteDiscount(Discount discount) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Discount")
            .setMessage("Are you sure you want to delete '" + discount.getDiscountName() + "'?")
            .setPositiveButton("Delete", (dialog, which) -> {
                try {
                    database.discountDao().deleteDiscount(discount);
                    Toast.makeText(this, "Discount deleted successfully", Toast.LENGTH_SHORT).show();
                    loadDiscounts(); // Refresh the list
                } catch (Exception e) {
                    Toast.makeText(this, "Error deleting discount: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onToggleDiscountStatus(Discount discount) {
        try {
            discount.setActive(!discount.isActive());
            database.discountDao().updateDiscount(discount);
            String message = discount.isActive() ? "Discount activated" : "Discount deactivated";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            loadDiscounts(); // Refresh the list
        } catch (Exception e) {
            Toast.makeText(this, "Error updating discount status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDiscounts(); // Refresh when returning from add/edit
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
