package com.example.tourmanagement.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.tourmanagement.R;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for displaying detailed user information.
 * Shows comprehensive user profile and statistics.
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-23
 */
public class UserDetailsActivity extends AppCompatActivity {

    private TextView textViewUsername;
    private TextView textViewEmail;
    private TextView textViewFullName;
    private TextView textViewUserType;
    private TextView textViewCreatedDate;
    private TextView textViewLastLogin;

    private TourManagementDatabase database;
    private ExecutorService executorService;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Temporary: use a simple layout until proper layout is created
        setContentView(android.R.layout.activity_list_item);

        // initializeViews();
        setupToolbar();
        setupDatabase();
        loadUserDetails();
    }

    private void initializeViews() {
        // TODO: Implement when proper layout is created
        // textViewUsername = findViewById(R.id.text_view_username);
        // textViewEmail = findViewById(R.id.text_view_email);
        // textViewFullName = findViewById(R.id.text_view_full_name);
        // textViewUserType = findViewById(R.id.text_view_user_type);
        // textViewCreatedDate = findViewById(R.id.text_view_created_date);
        // textViewLastLogin = findViewById(R.id.text_view_last_login);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Details");
        }
    }

    private void setupDatabase() {
        database = TourManagementDatabase.getDatabase(this);
        executorService = Executors.newFixedThreadPool(2);
    }

    private void loadUserDetails() {
        long userId = getIntent().getLongExtra("user_id", -1);
        if (userId != -1) {
            executorService.execute(() -> {
                currentUser = database.userDao().getUserById((int) userId);
                if (currentUser != null) {
                    runOnUiThread(this::displayUserDetails);
                }
            });
        }
    }

    private void displayUserDetails() {
        // TODO: Implement when proper layout is created
        /*
        textViewUsername.setText(currentUser.getUsername());
        textViewEmail.setText(currentUser.getEmail());
        textViewFullName.setText(currentUser.getFullName());
        textViewUserType.setText(currentUser.isAdmin() ? "Administrator" : "Regular User");

        // Format dates - createdAt is a long timestamp
        if (currentUser.getCreatedAt() > 0) {
            textViewCreatedDate.setText(new java.util.Date(currentUser.getCreatedAt()).toString());
        } else {
            textViewCreatedDate.setText("Not available");
        }

        // User model doesn't have lastLogin field, so we'll show a placeholder
        textViewLastLogin.setText("Not tracked");
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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
