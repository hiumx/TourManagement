package com.example.tourmanagement.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.tourmanagement.R;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for adding new users or editing existing users.
 * Provides form fields for user information and validation.
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-23
 */
public class AddEditUserActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextFullName;
    private CheckBox checkBoxIsAdmin;
    private Button buttonSave;

    private TourManagementDatabase database;
    private ExecutorService executorService;
    private User currentUser;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Temporary: use a simple layout until proper layout is created
        setContentView(android.R.layout.activity_list_item);

        // initializeViews();
        setupToolbar();
        setupDatabase();
        checkEditMode();
        // setupEventListeners();
    }

    private void initializeViews() {
        // TODO: Implement when proper layout is created
        // editTextUsername = findViewById(R.id.edit_text_username);
        // editTextEmail = findViewById(R.id.edit_text_email);
        // editTextPassword = findViewById(R.id.edit_text_password);
        // editTextFullName = findViewById(R.id.edit_text_full_name);
        // checkBoxIsAdmin = findViewById(R.id.checkbox_is_admin);
        // buttonSave = findViewById(R.id.button_save);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit User" : "Add User");
        }
    }

    private void setupDatabase() {
        database = TourManagementDatabase.getDatabase(this);
        executorService = Executors.newFixedThreadPool(2);
    }

    private void checkEditMode() {
        long userId = getIntent().getLongExtra("user_id", -1);
        if (userId != -1) {
            isEditMode = true;
            loadUserData(userId);
        }
    }

    private void loadUserData(long userId) {
        executorService.execute(() -> {
            currentUser = database.userDao().getUserById((int) userId);
            if (currentUser != null) {
                runOnUiThread(() -> {
                    // TODO: Populate fields when layout is implemented
                    // editTextUsername.setText(currentUser.getUsername());
                    // editTextEmail.setText(currentUser.getEmail());
                    // editTextFullName.setText(currentUser.getFullName());
                    // checkBoxIsAdmin.setChecked(currentUser.isAdmin());
                });
            }
        });
    }

    private void setupEventListeners() {
        // TODO: Implement when layout is created
        // buttonSave.setOnClickListener(v -> saveUser());
    }

    private void saveUser() {
        // TODO: Implement when layout is created
        /*
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        boolean isAdmin = checkBoxIsAdmin.isChecked();

        if (!validateInput(username, email, password, fullName)) {
            return;
        }

        executorService.execute(() -> {
            if (isEditMode && currentUser != null) {
                // Update existing user
                currentUser.setUsername(username);
                currentUser.setEmail(email);
                if (!password.isEmpty()) {
                    currentUser.setPassword(password);
                }
                currentUser.setFullName(fullName);
                currentUser.setAdmin(isAdmin);
                database.userDao().updateUser(currentUser);
            } else {
                // Create new user
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setEmail(email);
                newUser.setPassword(password);
                newUser.setFullName(fullName);
                newUser.setAdmin(isAdmin);
                database.userDao().insertUser(newUser);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "User saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
        */
    }

    private boolean validateInput(String username, String email, String password, String fullName) {
        // TODO: Implement when layout is created
        return true;
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
