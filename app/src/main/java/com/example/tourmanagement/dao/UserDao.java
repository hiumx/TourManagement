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
     * Retrieves all users from the database
     * Used for admin user management
     *
     * @return List of all users
     */
    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    /**
     * Finds a user by their unique ID
     * Used for user profile retrieval
     *
     * @param id User ID to search for
     * @return User object if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE id = :id")
    User getUserById(int id);

    /**
     * Authenticates user with username and password
     * Used for login functionality
     *
     * @param username User's username
     * @param password User's password
     * @return User object if credentials are valid, null otherwise
     */
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    User authenticateUser(String username, String password);

    /**
     * Checks if a username already exists in the database
     * Used during registration to ensure unique usernames
     *
     * @param username Username to check
     * @return User object if username exists, null otherwise
     */
    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);

    /**
     * Checks if an email already exists in the database
     * Used during registration to ensure unique emails
     *
     * @param email Email to check
     * @return User object if email exists, null otherwise
     */
    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);

    /**
     * Updates user's password
     * Used for password reset functionality
     *
     * @param userId User ID
     * @param newPassword New password
     */
    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    void updatePassword(int userId, String newPassword);

    /**
     * Gets users created within a specific time range
     * Used for admin analytics and reporting
     *
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return List of users created in the time range
     */
    @Query("SELECT * FROM users WHERE createdAt BETWEEN :startTime AND :endTime")
    List<User> getUsersByDateRange(long startTime, long endTime);

    /**
     * Searches users by full name (case insensitive)
     * Used for user search functionality
     *
     * @param name Name to search for
     * @return List of users matching the name
     */
    @Query("SELECT * FROM users WHERE fullName LIKE '%' || :name || '%'")
    List<User> searchUsersByName(String name);
}
