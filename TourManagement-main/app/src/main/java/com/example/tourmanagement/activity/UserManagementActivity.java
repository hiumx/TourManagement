package com.example.tourmanagement.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tourmanagement.R;
import com.example.tourmanagement.adapter.UserManagementAdapter;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User Management Activity for admin users.
 * Allows admins to view, edit, delete, and manage user accounts.
 *
 * Features:
 * - View all users in the system
 * - Edit user details
 * - Delete user accounts
 * - Promote/demote admin privileges
 * - View user statistics
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-23
 */
public class UserManagementActivity extends AppCompatActivity implements UserManagementAdapter.OnUserActionListener {

    private RecyclerView recyclerViewUsers;
    private UserManagementAdapter userAdapter;
    private FloatingActionButton fabAddUser;
    private TourManagementDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        initializeViews();
        setupToolbar();
        setupDatabase();
        setupRecyclerView();
        setupEventListeners();
        loadUsers();
    }

    private void initializeViews() {
        recyclerViewUsers = findViewById(R.id.recycler_view_users);
        fabAddUser = findViewById(R.id.fab_add_user);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Management");
            getSupportActionBar().setSubtitle("Manage system users");
        }
    }

    private void setupDatabase() {
        database = TourManagementDatabase.getDatabase(this);
        executorService = Executors.newFixedThreadPool(2);
    }

    private void setupRecyclerView() {
        userAdapter = new UserManagementAdapter(this, this);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);
    }

    private void setupEventListeners() {
        fabAddUser.setOnClickListener(v -> {
            // Navigate to add user activity
            Intent intent = new Intent(this, AddEditUserActivity.class);
            startActivity(intent);
        });
    }

    private void loadUsers() {
        executorService.execute(() -> {
            List<User> users = database.userDao().getAllUsers();
            runOnUiThread(() -> {
                userAdapter.updateUsers(users);
            });
        });
    }

    @Override
    public void onEditUser(User user) {
        Intent intent = new Intent(this, AddEditUserActivity.class);
        intent.putExtra("user_id", user.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteUser(User user) {
        new AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete user '" + user.getUsername() + "'?")
            .setPositiveButton("Delete", (dialog, which) -> {
                executorService.execute(() -> {
                    database.userDao().deleteUser(user);
                    runOnUiThread(() -> {
                        loadUsers();
                        Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    });
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onToggleAdminStatus(User user) {
        String action = user.isAdmin() ? "remove admin privileges from" : "grant admin privileges to";
        new AlertDialog.Builder(this)
            .setTitle("Change Admin Status")
            .setMessage("Are you sure you want to " + action + " user '" + user.getUsername() + "'?")
            .setPositiveButton("Confirm", (dialog, which) -> {
                executorService.execute(() -> {
                    user.setAdmin(!user.isAdmin());
                    database.userDao().updateUser(user);
                    runOnUiThread(() -> {
                        loadUsers();
                        String message = user.isAdmin() ? "Admin privileges granted" : "Admin privileges removed";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    });
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onViewUserDetails(User user) {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra("user_id", user.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers(); // Refresh user list when returning from other activities
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
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
