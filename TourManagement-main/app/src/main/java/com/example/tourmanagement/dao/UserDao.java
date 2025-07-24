package com.example.tourmanagement.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.tourmanagement.model.User;
import java.util.List;

/**
 * Data Access Object (DAO) for User entity operations.
 * Provides methods for user authentication, registration, and management.
 *
 * Features:
 * - User authentication (login)
 * - User registration and profile management
 * - User information retrieval and updates
 * - User deletion and account management
 * - Password reset functionality
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
@Dao
public interface UserDao {

    /**
     * Inserts a new user into the database
     * Used for user registration
     *
     * @param user User object to insert
     * @return The ID of the inserted user
     */
    @Insert
    long insertUser(User user);

    /**
     * Updates an existing user's information
     * Used for profile updates
     *
     * @param user User object with updated information
     */
    @Update
    void updateUser(User user);

    /**
     * Deletes a user from the database
     * Used for account deletion
     *
     * @param user User object to delete
     */
    @Delete
    void deleteUser(User user);

    /**
     * Authenticates user with username and password
     * Used for login functionality
     *
     * @param username User's username
     * @param password User's password
     * @return User object if authentication successful, null otherwise
     */
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User authenticateUser(String username, String password);

    /**
     * Finds a user by username
     * Used for registration validation and user lookup
     *
     * @param username Username to search for
     * @return User object if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    /**
     * Finds a user by email address
     * Used for forgot password functionality and email validation
     *
     * @param email Email address to search for
     * @return User object if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    /**
     * Finds a user by ID
     * Used for profile retrieval and user management
     *
     * @param id User ID to search for
     * @return User object if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(int id);

    /**
     * Updates user password
     * Used for password change and reset functionality
     *
     * @param userId User ID
     * @param newPassword New password
     * @param mustChangePassword Whether user must change password on next login
     * @param resetTimestamp Timestamp of password reset
     */
    @Query("UPDATE users SET password = :newPassword, mustChangePassword = :mustChangePassword, passwordResetAt = :resetTimestamp WHERE id = :userId")
    void updateUserPassword(int userId, String newPassword, boolean mustChangePassword, long resetTimestamp);

    /**
     * Simple password update method
     * Used for basic password changes without additional flags
     *
     * @param userId User ID
     * @param newPassword New password
     */
    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    void updatePassword(int userId, String newPassword);

    /**
     * Clears the password change requirement
     * Used after user successfully changes their password
     *
     * @param userId User ID
     */
    @Query("UPDATE users SET mustChangePassword = 0 WHERE id = :userId")
    void clearPasswordChangeRequirement(int userId);

    /**
     * Retrieves all users from the database
     * Used for user management and admin functions
     *
     * @return List of all users
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    List<User> getAllUsers();

    /**
     * Gets the count of all users in the system
     *
     * @return Total number of users
     */
    @Query("SELECT COUNT(*) FROM users")
    int getTotalUsersCount();

    /**
     * Gets the count of admin users in the system
     *
     * @return Number of admin users
     */
    @Query("SELECT COUNT(*) FROM users WHERE isAdmin = 1")
    int getAdminUsersCount();

    /**
     * Searches users by username or full name
     *
     * @param searchQuery Search term
     * @return List of matching users
     */
    @Query("SELECT * FROM users WHERE username LIKE '%' || :searchQuery || '%' OR fullName LIKE '%' || :searchQuery || '%'")
    List<User> searchUsers(String searchQuery);

    /**
     * Checks if username already exists
     * Used for registration validation
     *
     * @param username Username to check
     * @return Count of users with this username (should be 0 or 1)
     */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int isUsernameExists(String username);

    /**
     * Checks if email already exists
     * Used for registration validation
     *
     * @param email Email to check
     * @return Count of users with this email (should be 0 or 1)
     */
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int isEmailExists(String email);
}
