package com.example.tourmanagement.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Tour entity class representing tour packages in the tour management system.
 * This class defines the structure for tour information stored in SQLite database.
 *
 * Features:
 * - Complete tour information (name, location, description)
 * - Pricing and capacity management
 * - Scheduling and duration tracking
 * - Booking availability management
 * - Tour image handling
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
@Entity(tableName = "tours")
public class Tour {
    /**
     * Unique identifier for the tour (auto-generated primary key)
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * Name/title of the tour package
     * Displayed in tour listings and details
     */
    private String tourName;

    /**
     * Image URL or local path for the tour
     * Used for displaying tour visuals in the app
     */
    private String tourImage;

    /**
     * Location/destination of the tour
     * Geographic location where the tour takes place
     */
    private String tourLocation;

    /**
     * Tour start date and time (timestamp in milliseconds)
     * When the tour is scheduled to begin
     */
    private long tourTime;

    /**
     * Detailed description of the tour
     * Includes itinerary, activities, and other tour details
     */
    private String tourDescription;

    /**
     * Cost/price of the tour per person in local currency
     * Used for payment calculations and QR code generation
     */
    private double tourCost;

    /**
     * Maximum number of people allowed for this tour
     * Capacity limit for booking management
     */
    private int numberOfPeoples;

    /**
     * Current number of confirmed bookings for this tour
     * Used to track available slots
     */
    private int currentBookings;

    /**
     * Duration of the tour in days
     * How long the tour lasts
     */
    private int duration;

    /**
     * Whether the tour is currently active/available for booking
     * Used to enable/disable tour availability
     */
    private boolean isActive;

    /**
     * Timestamp when tour was created (in milliseconds)
     * For tracking and administrative purposes
     */
    private long createdAt;

    /**
     * Default constructor
     * Initializes tour with default values and current timestamp
     */
    public Tour() {
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
        this.currentBookings = 0;
    }

    /**
     * Constructor with essential tour information
     *
     * @param tourName Name of the tour
     * @param tourImage Image URL or path
     * @param tourLocation Tour destination
     * @param tourTime Tour start timestamp
     * @param tourDescription Detailed description
     * @param tourCost Cost per person
     * @param numberOfPeoples Maximum capacity
     * @param duration Tour duration in days
     */
    public Tour(String tourName, String tourImage, String tourLocation, long tourTime,
                String tourDescription, double tourCost, int numberOfPeoples, int duration) {
        this.tourName = tourName;
        this.tourImage = tourImage;
        this.tourLocation = tourLocation;
        this.tourTime = tourTime;
        this.tourDescription = tourDescription;
        this.tourCost = tourCost;
        this.numberOfPeoples = numberOfPeoples;
        this.duration = duration;
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
        this.currentBookings = 0;
    }

    // Getter and Setter methods with documentation

    /**
     * Gets the tour's unique ID
     * @return Tour ID
     */
    public int getId() { return id; }

    /**
     * Sets the tour's unique ID
     * @param id Tour ID
     */
    public void setId(int id) { this.id = id; }

    /**
     * Gets the tour name
     * @return Tour name string
     */
    public String getTourName() { return tourName; }

    /**
     * Sets the tour name
     * @param tourName Tour name string
     */
    public void setTourName(String tourName) { this.tourName = tourName; }

    /**
     * Gets the tour image path/URL
     * @return Tour image string
     */
    public String getTourImage() { return tourImage; }

    /**
     * Sets the tour image path/URL
     * @param tourImage Tour image string
     */
    public void setTourImage(String tourImage) { this.tourImage = tourImage; }

    /**
     * Gets the tour location
     * @return Tour location string
     */
    public String getTourLocation() { return tourLocation; }

    /**
     * Sets the tour location
     * @param tourLocation Tour location string
     */
    public void setTourLocation(String tourLocation) { this.tourLocation = tourLocation; }

    /**
     * Gets the tour start time
     * @return Tour time timestamp
     */
    public long getTourTime() { return tourTime; }

    /**
     * Sets the tour start time
     * @param tourTime Tour time timestamp
     */
    public void setTourTime(long tourTime) { this.tourTime = tourTime; }

    /**
     * Gets the tour description
     * @return Tour description string
     */
    public String getTourDescription() { return tourDescription; }

    /**
     * Sets the tour description
     * @param tourDescription Tour description string
     */
    public void setTourDescription(String tourDescription) { this.tourDescription = tourDescription; }

    /**
     * Gets the tour cost per person
     * @return Tour cost as double
     */
    public double getTourCost() { return tourCost; }

    /**
     * Sets the tour cost per person
     * @param tourCost Tour cost as double
     */
    public void setTourCost(double tourCost) { this.tourCost = tourCost; }

    /**
     * Gets the maximum number of people allowed
     * @return Maximum capacity as integer
     */
    public int getNumberOfPeoples() { return numberOfPeoples; }

    /**
     * Sets the maximum number of people allowed
     * @param numberOfPeoples Maximum capacity as integer
     */
    public void setNumberOfPeoples(int numberOfPeoples) { this.numberOfPeoples = numberOfPeoples; }

    /**
     * Gets the current number of bookings
     * @return Current bookings count
     */
    public int getCurrentBookings() { return currentBookings; }

    /**
     * Sets the current number of bookings
     * @param currentBookings Current bookings count
     */
    public void setCurrentBookings(int currentBookings) { this.currentBookings = currentBookings; }

    /**
     * Gets the tour duration in days
     * @return Duration in days
     */
    public int getDuration() { return duration; }

    /**
     * Sets the tour duration in days
     * @param duration Duration in days
     */
    public void setDuration(int duration) { this.duration = duration; }

    /**
     * Gets the tour active status
     * @return True if tour is active, false otherwise
     */
    public boolean isActive() { return isActive; }

    /**
     * Sets the tour active status
     * @param active True to activate tour, false to deactivate
     */
    public void setActive(boolean active) { isActive = active; }

    /**
     * Gets the tour creation timestamp
     * @return Creation timestamp in milliseconds
     */
    public long getCreatedAt() { return createdAt; }

    /**
     * Sets the tour creation timestamp
     * @param createdAt Creation timestamp in milliseconds
     */
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /**
     * Checks if tour has available slots for booking
     * @return true if tour has available slots, false otherwise
     */
    public boolean hasAvailableSlots() {
        return currentBookings < numberOfPeoples;
    }

    /**
     * Calculates remaining slots available for booking
     * @return number of available slots
     */
    public int getAvailableSlots() {
        return numberOfPeoples - currentBookings;
    }

    /**
     * Calculates total tour cost for multiple people
     * @param numberOfPeople Number of people booking
     * @return Total cost for the group
     */
    public double calculateTotalCost(int numberOfPeople) {
        return tourCost * numberOfPeople;
    }
}
