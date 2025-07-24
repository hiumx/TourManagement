package com.example.tourmanagement.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * User entity class representing user data in the tour management system.
 * This class defines the structure for user information stored in SQLite database.
 *
 * Features:
 * - User authentication (username, password)
 * - Contact information (email, phone)
 * - Personal details (full name, address)
 * - Account creation tracking
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
@Entity(tableName = "users")
public class User {
    /**
     * Unique identifier for the user (auto-generated primary key)
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * Username for user authentication (must be unique)
     * Used for login functionality
     */
    private String username;

    /**
     * User's password for authentication
     * Note: In production, this should be hashed
     */
    private String password;

    /**
     * User's email address for communication and notifications
     */
    private String email;

    /**
     * User's phone number for contact and booking confirmations
     */
    private String phoneNumber;

    /**
     * User's full name for display and booking records
     */
    private String fullName;

    /**
     * User's address for billing and shipping information
     */
    private String address;

    /**
     * Timestamp when user account was created (in milliseconds)
     */
    private long createdAt;

    /**
     * Path to user's profile image file
     * Stores the local file path of the uploaded profile image
     */
    private String profileImagePath;

    /**
     * User role - determines if user has admin privileges
     * Default: false (normal user), true (admin user)
     */
    private boolean isAdmin;

    /**
     * Indicates if user must change password on next login
     * Used for forgot password functionality
     */
    private boolean mustChangePassword = false;

    /**
     * Timestamp when password was last reset (in milliseconds)
     * Used to track password reset requests
     */
    private long passwordResetAt = 0;

    /**
     * Default constructor
     * Automatically sets creation timestamp to current time
     */
    public User() {
        this.createdAt = System.currentTimeMillis();
        this.isAdmin = false; // Default to normal user
    }

    /**
     * Constructor with essential user information
     *
     * @param username User's unique username
     * @param password User's password
     * @param email User's email address
     * @param phoneNumber User's phone number
     * @param fullName User's full name
     */
    public User(String username, String password, String email, String phoneNumber, String fullName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
        this.createdAt = System.currentTimeMillis();
        this.isAdmin = false; // Default to normal user
    }

    // Getter and Setter methods with documentation

    /**
     * Gets the user's unique ID
     * @return User ID
     */
    public int getId() { return id; }

    /**
     * Sets the user's unique ID
     * @param id User ID
     */
    public void setId(int id) { this.id = id; }

    /**
     * Gets the username
     * @return Username string
     */
    public String getUsername() { return username; }

    /**
     * Sets the username
     * @param username Username string
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Gets the user's password
     * @return Password string
     */
    public String getPassword() { return password; }

    /**
     * Sets the user's password
     * @param password Password string
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Gets the user's email address
     * @return Email address string
     */
    public String getEmail() { return email; }

    /**
     * Sets the user's email address
     * @param email Email address string
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Gets the user's phone number
     * @return Phone number string
     */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * Sets the user's phone number
     * @param phoneNumber Phone number string
     */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /**
     * Gets the user's full name
     * @return Full name string
     */
    public String getFullName() { return fullName; }

    /**
     * Sets the user's full name
     * @param fullName Full name string
     */
    public void setFullName(String fullName) { this.fullName = fullName; }

    /**
     * Gets the user's address
     * @return Address string
     */
    public String getAddress() { return address; }

    /**
     * Sets the user's address
     * @param address Address string
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * Gets the account creation timestamp
     * @return Creation timestamp in milliseconds
     */
    public long getCreatedAt() { return createdAt; }

    /**
     * Sets the account creation timestamp
     * @param createdAt Creation timestamp in milliseconds
     */
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /**
     * Gets the path to the user's profile image
     * @return Profile image file path string
     */
    public String getProfileImagePath() { return profileImagePath; }

    /**
     * Sets the path to the user's profile image
     * @param profileImagePath Profile image file path string
     */
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }

    /**
     * Checks if the user is an admin
     * @return true if admin, false otherwise
     */
    public boolean isAdmin() { return isAdmin; }

    /**
     * Sets the user's admin status
     * @param admin true to grant admin privileges, false to revoke
     */
    public void setAdmin(boolean admin) { isAdmin = admin; }

    /**
     * Checks if the user must change password on next login
     * @return true if password change is required, false otherwise
     */
    public boolean isMustChangePassword() { return mustChangePassword; }

    /**
     * Sets the user's password change requirement
     * @param mustChangePassword true to require password change, false otherwise
     */
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

    /**
     * Gets the timestamp when the password was last reset
     * @return Password reset timestamp in milliseconds
     */
    public long getPasswordResetAt() { return passwordResetAt; }

    /**
     * Sets the timestamp when the password was last reset
     * @param passwordResetAt Password reset timestamp in milliseconds
     */
    public void setPasswordResetAt(long passwordResetAt) { this.passwordResetAt = passwordResetAt; }
}
