package com.example.tourmanagement.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.tourmanagement.model.Tour;
import java.util.List;

/**
 * Data Access Object (DAO) for Tour entity operations.
 * Provides methods for CRUD operations on tours and tour management.
 *
 * Features:
 * - Complete CRUD operations for tours
 * - Tour search and filtering capabilities
 * - Booking availability management
 * - Tour status and capacity tracking
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
@Dao
public interface TourDao {

    /**
     * Inserts a new tour into the database
     * Used for creating new tour packages
     *
     * @param tour Tour object to insert
     * @return The ID of the inserted tour
     */
    @Insert
    long insertTour(Tour tour);

    /**
     * Updates an existing tour's information
     * Used for editing tour details
     *
     * @param tour Tour object with updated information
     */
    @Update
    void updateTour(Tour tour);

    /**
     * Deletes a tour from the database
     * Used for removing tour packages
     *
     * @param tour Tour object to delete
     */
    @Delete
    void deleteTour(Tour tour);

    /**
     * Retrieves all tours from the database
     * Used for displaying all tours in dashboard
     *
     * @return List of all tours
     */
    @Query("SELECT * FROM tours ORDER BY createdAt DESC")
    List<Tour> getAllTours();

    /**
     * Retrieves only active tours
     * Used for displaying available tours to customers
     *
     * @return List of active tours
     */
    @Query("SELECT * FROM tours WHERE isActive = 1 ORDER BY tourTime ASC")
    List<Tour> getActiveTours();

    /**
     * Finds a tour by its unique ID
     * Used for displaying tour details
     *
     * @param id Tour ID to search for
     * @return Tour object if found, null otherwise
     */
    @Query("SELECT * FROM tours WHERE id = :id")
    Tour getTourById(int id);

    /**
     * Searches tours by name (case insensitive)
     * Used for tour search functionality
     *
     * @param name Tour name to search for
     * @return List of tours matching the name
     */
    @Query("SELECT * FROM tours WHERE tourName LIKE '%' || :name || '%' AND isActive = 1")
    List<Tour> searchToursByName(String name);

    /**
     * Searches tours by location (case insensitive)
     * Used for location-based tour filtering
     *
     * @param location Location to search for
     * @return List of tours in the specified location
     */
    @Query("SELECT * FROM tours WHERE tourLocation LIKE '%' || :location || '%' AND isActive = 1")
    List<Tour> searchToursByLocation(String location);

    /**
     * Gets tours within a specific price range
     * Used for price-based filtering
     *
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @return List of tours within price range
     */
    @Query("SELECT * FROM tours WHERE tourCost BETWEEN :minPrice AND :maxPrice AND isActive = 1")
    List<Tour> getToursByPriceRange(double minPrice, double maxPrice);

    /**
     * Gets tours with available booking slots
     * Used for showing bookable tours
     *
     * @return List of tours with available capacity
     */
    @Query("SELECT * FROM tours WHERE currentBookings < numberOfPeoples AND isActive = 1")
    List<Tour> getAvailableTours();

    /**
     * Updates tour booking count when a booking is made
     * Used for managing tour capacity
     *
     * @param tourId Tour ID
     * @param increment Number to add to current bookings
     */
    @Query("UPDATE tours SET currentBookings = currentBookings + :increment WHERE id = :tourId")
    void updateBookingCount(int tourId, int increment);

    /**
     * Gets tours scheduled within a date range
     * Used for date-based filtering
     *
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return List of tours in the date range
     */
    @Query("SELECT * FROM tours WHERE tourTime BETWEEN :startTime AND :endTime AND isActive = 1")
    List<Tour> getToursByDateRange(long startTime, long endTime);

    /**
     * Activates or deactivates a tour
     * Used for tour status management
     *
     * @param tourId Tour ID
     * @param isActive New active status
     */
    @Query("UPDATE tours SET isActive = :isActive WHERE id = :tourId")
    void updateTourStatus(int tourId, boolean isActive);

    /**
     * Gets the most popular tours based on booking count
     * Used for featuring popular destinations
     *
     * @param limit Maximum number of tours to return
     * @return List of most booked tours
     */
    @Query("SELECT * FROM tours WHERE isActive = 1 ORDER BY currentBookings DESC LIMIT :limit")
    List<Tour> getPopularTours(int limit);

    /**
     * Gets upcoming tours (future dates only)
     * Used for filtering out past tours
     *
     * @param currentTime Current timestamp
     * @return List of upcoming tours
     */
    @Query("SELECT * FROM tours WHERE tourTime > :currentTime AND isActive = 1 ORDER BY tourTime ASC")
    List<Tour> getUpcomingTours(long currentTime);

    /**
     * Get all tours from the database
     * @return LiveData list of all tours
     */
    @Query("SELECT * FROM tours ORDER BY tourName ASC")
    LiveData<List<Tour>> getAllToursLive();

    /**
     * Search tours by name or location (live search)
     * Uses LIKE operator with case-insensitive search
     * @param searchQuery The search query to match against tour name or location
     * @return LiveData list of matching tours
     */
    @Query("SELECT * FROM tours WHERE " +
           "tourName LIKE '%' || :searchQuery || '%' OR " +
           "tourLocation LIKE '%' || :searchQuery || '%' " +
           "ORDER BY tourName ASC")
    LiveData<List<Tour>> searchToursLive(String searchQuery);

    /**
     * Get tours by specific location
     * @param location The location to search for
     * @return LiveData list of tours in the specified location
     */
    @Query("SELECT * FROM tours WHERE tourLocation LIKE '%' || :location || '%' ORDER BY tourName ASC")
    LiveData<List<Tour>> getToursByLocationLive(String location);

    /**
     * Get tours by name
     * @param name The name to search for
     * @return LiveData list of tours matching the name
     */
    @Query("SELECT * FROM tours WHERE tourName LIKE '%' || :name || '%' ORDER BY tourName ASC")
    LiveData<List<Tour>> getToursByNameLive(String name);

    /**
     * Get a specific tour by ID
     * @param tourId The ID of the tour
     * @return LiveData of the specific tour
     */
    @Query("SELECT * FROM tours WHERE id = :tourId")
    LiveData<Tour> getTourByIdLive(int tourId);

    /**
     * Delete tour by ID
     * @param tourId The ID of the tour to delete
     */
    @Query("DELETE FROM tours WHERE id = :tourId")
    void deleteTourById(int tourId);
}
