package com.example.tourmanagement.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.tourmanagement.model.Booking;
import java.util.List;

/**
 * Data Access Object (DAO) for Booking entity operations.
 * Provides methods for booking management, payment tracking, and booking history.
 *
 * Features:
 * - Complete booking lifecycle management
 * - Payment status tracking
 * - Customer booking history
 * - Booking analytics and reporting
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
@Dao
public interface BookingDao {

    /**
     * Inserts a new booking into the database
     * Used when customer makes a tour booking
     *
     * @param booking Booking object to insert
     * @return The ID of the inserted booking
     */
    @Insert
    long insertBooking(Booking booking);

    /**
     * Updates an existing booking's information
     * Used for updating booking status, payment status, etc.
     *
     * @param booking Booking object with updated information
     */
    @Update
    void updateBooking(Booking booking);

    /**
     * Deletes a booking from the database
     * Used for booking cancellation
     *
     * @param booking Booking object to delete
     */
    @Delete
    void deleteBooking(Booking booking);

    /**
     * Retrieves all bookings from the database
     * Used for admin booking management
     *
     * @return List of all bookings
     */
    @Query("SELECT * FROM bookings ORDER BY bookingDate DESC")
    List<Booking> getAllBookings();

    /**
     * Gets all bookings for a specific user
     * Used for customer booking history
     *
     * @param userId User ID to get bookings for
     * @return List of user's bookings
     */
    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY bookingDate DESC")
    List<Booking> getBookingsByUserId(int userId);

    /**
     * Gets all bookings for a specific tour
     * Used for tour booking management
     *
     * @param tourId Tour ID to get bookings for
     * @return List of tour bookings
     */
    @Query("SELECT * FROM bookings WHERE tourId = :tourId ORDER BY bookingDate DESC")
    List<Booking> getBookingsByTourId(int tourId);

    /**
     * Finds a booking by its unique ID
     * Used for booking details and ticket display
     *
     * @param id Booking ID to search for
     * @return Booking object if found, null otherwise
     */
    @Query("SELECT * FROM bookings WHERE id = :id")
    Booking getBookingById(int id);

    /**
     * Finds a booking by its reference number
     * Used for booking lookup by customers
     *
     * @param reference Booking reference to search for
     * @return Booking object if found, null otherwise
     */
    @Query("SELECT * FROM bookings WHERE bookingReference = :reference")
    Booking getBookingByReference(String reference);

    /**
     * Gets bookings by payment status
     * Used for payment management
     *
     * @param paymentStatus Payment status to filter by
     * @return List of bookings with specified payment status
     */
    @Query("SELECT * FROM bookings WHERE paymentStatus = :paymentStatus ORDER BY bookingDate DESC")
    List<Booking> getBookingsByPaymentStatus(String paymentStatus);

    /**
     * Gets bookings by booking status
     * Used for booking status management
     *
     * @param bookingStatus Booking status to filter by
     * @return List of bookings with specified status
     */
    @Query("SELECT * FROM bookings WHERE bookingStatus = :bookingStatus ORDER BY bookingDate DESC")
    List<Booking> getBookingsByStatus(String bookingStatus);

    /**
     * Updates booking payment status
     * Used when payment is processed
     *
     * @param bookingId Booking ID
     * @param paymentStatus New payment status
     */
    @Query("UPDATE bookings SET paymentStatus = :paymentStatus WHERE id = :bookingId")
    void updatePaymentStatus(int bookingId, String paymentStatus);

    /**
     * Updates booking status
     * Used for booking confirmation/cancellation
     *
     * @param bookingId Booking ID
     * @param bookingStatus New booking status
     */
    @Query("UPDATE bookings SET bookingStatus = :bookingStatus WHERE id = :bookingId")
    void updateBookingStatus(int bookingId, String bookingStatus);

    /**
     * Sets QR code for a booking
     * Used for payment QR code generation
     *
     * @param bookingId Booking ID
     * @param qrCode QR code string
     */
    @Query("UPDATE bookings SET qrCode = :qrCode WHERE id = :bookingId")
    void setBookingQrCode(int bookingId, String qrCode);

    /**
     * Gets confirmed bookings for a specific user
     * Used for showing valid tickets
     *
     * @param userId User ID
     * @return List of confirmed bookings
     */
    @Query("SELECT * FROM bookings WHERE userId = :userId AND bookingStatus = 'CONFIRMED' ORDER BY bookingDate DESC")
    List<Booking> getConfirmedBookings(int userId);

    /**
     * Gets bookings within a date range
     * Used for analytics and reporting
     *
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return List of bookings in the date range
     */
    @Query("SELECT * FROM bookings WHERE bookingDate BETWEEN :startTime AND :endTime ORDER BY bookingDate DESC")
    List<Booking> getBookingsByDateRange(long startTime, long endTime);

    /**
     * Calculates total revenue from paid bookings
     * Used for revenue analytics
     *
     * @return Total revenue amount
     */
    @Query("SELECT SUM(totalAmount) FROM bookings WHERE paymentStatus = 'PAID'")
    Double getTotalRevenue();

    /**
     * Gets pending payment bookings
     * Used for payment reminder notifications
     *
     * @return List of bookings with pending payments
     */
    @Query("SELECT * FROM bookings WHERE paymentStatus = 'PENDING' ORDER BY bookingDate ASC")
    List<Booking> getPendingPaymentBookings();

    /**
     * Gets count of bookings for a specific user
     * Used for dashboard statistics
     *
     * @param userId User ID to count bookings for
     * @return Number of bookings for the user
     */
    @Query("SELECT COUNT(*) FROM bookings WHERE userId = :userId")
    int getUserBookingsCount(int userId);

    /**
     * Gets total count of all bookings in the system
     * Used for admin dashboard statistics
     *
     * @return Total number of bookings
     */
    @Query("SELECT COUNT(*) FROM bookings")
    int getTotalBookingsCount();

    /**
     * Gets count of active bookings for a user (excluding cancelled)
     * Used for user dashboard statistics
     *
     * @param userId User ID to count active bookings for
     * @return Number of active bookings for the user
     */
    @Query("SELECT COUNT(*) FROM bookings WHERE userId = :userId AND bookingStatus != 'CANCELLED'")
    int getUserActiveBookingsCount(int userId);

    /**
     * Gets count of pending bookings (awaiting payment)
     * Used for admin dashboard
     *
     * @return Number of pending bookings
     */
    @Query("SELECT COUNT(*) FROM bookings WHERE paymentStatus = 'PENDING'")
    int getPendingBookingsCount();

    /**
     * Gets count of completed bookings
     * Used for admin dashboard
     *
     * @return Number of completed bookings
     */
    @Query("SELECT COUNT(*) FROM bookings WHERE bookingStatus = 'CONFIRMED' AND paymentStatus = 'PAID'")
    int getCompletedBookingsCount();

    /**
     * Gets all confirmed bookings for revenue calculations
     *
     * @return List of confirmed bookings
     */
    @Query("SELECT * FROM bookings WHERE bookingStatus = 'CONFIRMED' ORDER BY bookingDate DESC")
    List<Booking> getConfirmedBookings();

    /**
     * Gets total revenue from all confirmed bookings
     *
     * @return Total revenue amount
     */
    @Query("SELECT SUM(totalAmount) FROM bookings WHERE bookingStatus = 'CONFIRMED'")
    double getTotalConfirmedRevenue();

    /**
     * Gets monthly revenue for current month
     *
     * @param monthStart Start timestamp of the month
     * @return Revenue for the month
     */
    @Query("SELECT SUM(totalAmount) FROM bookings WHERE bookingStatus = 'CONFIRMED' AND bookingDate >= :monthStart")
    double getMonthlyRevenue(long monthStart);

    /**
     * Gets revenue data for the last 12 months for chart visualization
     *
     * @return List of monthly revenue amounts for the past 12 months
     */
    @Query("SELECT " +
           "COALESCE(SUM(totalAmount), 0.0) as revenue " +
           "FROM bookings " +
           "WHERE bookingStatus = 'CONFIRMED' " +
           "AND bookingDate >= :startDate " +
           "GROUP BY strftime('%Y-%m', datetime(bookingDate/1000, 'unixepoch')) " +
           "ORDER BY bookingDate ASC")
    List<Double> getMonthlyRevenueData(long startDate);

    /**
     * Gets count of bookings by status for admin dashboard statistics
     * Used for showing booking status distribution
     *
     * @param status Booking status to count
     * @return Number of bookings with the specified status
     */
    @Query("SELECT COUNT(*) FROM bookings WHERE bookingStatus = :status")
    int getBookingCountByStatus(String status);

    /**
     * Gets all pending bookings for admin approval
     * Used in admin booking management interface
     *
     * @return List of pending bookings ordered by booking date
     */
    @Query("SELECT * FROM bookings WHERE bookingStatus = 'PENDING' ORDER BY bookingDate ASC")
    List<Booking> getPendingBookingsForApproval();

    /**
     * Gets recent bookings for admin dashboard
     * Used to show latest booking activity
     *
     * @param limit Number of recent bookings to retrieve
     * @return List of recent bookings
     */
    @Query("SELECT * FROM bookings ORDER BY bookingDate DESC LIMIT :limit")
    List<Booking> getRecentBookings(int limit);

    /**
     * Gets bookings that need admin attention (pending status)
     * Used for admin notification system
     *
     * @return Count of bookings requiring admin action
     */
    @Query("SELECT COUNT(*) FROM bookings WHERE bookingStatus = 'PENDING'")
    int getBookingsRequiringAttentionCount();
}
