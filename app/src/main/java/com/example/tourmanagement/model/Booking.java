package com.example.tourmanagement.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

/**
 * Booking entity class representing tour bookings in the tour management system.
 * This class defines the structure for booking information stored in SQLite database.
 *
 * Features:
 * - Links users to tours they've booked
 * - Tracks booking details and payment status
 * - Manages booking history and tickets
 * - Handles payment information and QR codes
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
@Entity(tableName = "bookings",
        foreignKeys = {
            @ForeignKey(entity = User.class,
                       parentColumns = "id",
                       childColumns = "userId",
                       onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Tour.class,
                       parentColumns = "id",
                       childColumns = "tourId",
                       onDelete = ForeignKey.CASCADE)
        })
public class Booking {
    /**
     * Unique identifier for the booking (auto-generated primary key)
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * Foreign key reference to the user who made the booking
     */
    private int userId;

    /**
     * Foreign key reference to the booked tour
     */
    private int tourId;

    /**
     * Number of people included in this booking
     */
    private int numberOfPeople;

    /**
     * Total amount paid for this booking
     */
    private double totalAmount;

    /**
     * Current status of the booking (PENDING, CONFIRMED, CANCELLED, COMPLETED)
     */
    private String bookingStatus;

    /**
     * Payment status (PENDING, PAID, REFUNDED)
     */
    private String paymentStatus;

    /**
     * QR code string for payment verification
     */
    private String qrCode;

    /**
     * Unique booking reference number for customer reference
     */
    private String bookingReference;

    /**
     * Timestamp when booking was created (in milliseconds)
     */
    private long bookingDate;

    /**
     * Additional notes or special requests from customer
     */
    private String notes;

    /**
     * Default constructor
     * Initializes booking with default values and current timestamp
     */
    public Booking() {
        this.bookingDate = System.currentTimeMillis();
        this.bookingStatus = "PENDING";
        this.paymentStatus = "PENDING";
        this.generateBookingReference();
    }

    /**
     * Constructor with essential booking information
     *
     * @param userId ID of the user making the booking
     * @param tourId ID of the tour being booked
     * @param numberOfPeople Number of people in the booking
     * @param totalAmount Total cost of the booking
     */
    public Booking(int userId, int tourId, int numberOfPeople, double totalAmount) {
        this.userId = userId;
        this.tourId = tourId;
        this.numberOfPeople = numberOfPeople;
        this.totalAmount = totalAmount;
        this.bookingDate = System.currentTimeMillis();
        this.bookingStatus = "PENDING";
        this.paymentStatus = "PENDING";
        this.generateBookingReference();
    }

    /**
     * Generates a unique booking reference number
     * Format: BK + timestamp + random number
     */
    private void generateBookingReference() {
        this.bookingReference = "BK" + System.currentTimeMillis() +
                               String.valueOf((int)(Math.random() * 1000));
    }

    // Getter and Setter methods with documentation

    /**
     * Gets the booking's unique ID
     * @return Booking ID
     */
    public int getId() { return id; }

    /**
     * Sets the booking's unique ID
     * @param id Booking ID
     */
    public void setId(int id) { this.id = id; }

    /**
     * Gets the user ID who made the booking
     * @return User ID
     */
    public int getUserId() { return userId; }

    /**
     * Sets the user ID who made the booking
     * @param userId User ID
     */
    public void setUserId(int userId) { this.userId = userId; }

    /**
     * Gets the tour ID that was booked
     * @return Tour ID
     */
    public int getTourId() { return tourId; }

    /**
     * Sets the tour ID that was booked
     * @param tourId Tour ID
     */
    public void setTourId(int tourId) { this.tourId = tourId; }

    /**
     * Gets the number of people in the booking
     * @return Number of people
     */
    public int getNumberOfPeople() { return numberOfPeople; }

    /**
     * Sets the number of people in the booking
     * @param numberOfPeople Number of people
     */
    public void setNumberOfPeople(int numberOfPeople) { this.numberOfPeople = numberOfPeople; }

    /**
     * Gets the total amount for the booking
     * @return Total amount
     */
    public double getTotalAmount() { return totalAmount; }

    /**
     * Sets the total amount for the booking
     * @param totalAmount Total amount
     */
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    /**
     * Gets the booking status
     * @return Booking status string
     */
    public String getBookingStatus() { return bookingStatus; }

    /**
     * Sets the booking status
     * @param bookingStatus Booking status string
     */
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    /**
     * Gets the payment status
     * @return Payment status string
     */
    public String getPaymentStatus() { return paymentStatus; }

    /**
     * Sets the payment status
     * @param paymentStatus Payment status string
     */
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    /**
     * Gets the QR code for payment
     * @return QR code string
     */
    public String getQrCode() { return qrCode; }

    /**
     * Sets the QR code for payment
     * @param qrCode QR code string
     */
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    /**
     * Gets the booking reference number
     * @return Booking reference string
     */
    public String getBookingReference() { return bookingReference; }

    /**
     * Sets the booking reference number
     * @param bookingReference Booking reference string
     */
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    /**
     * Gets the booking date timestamp
     * @return Booking date in milliseconds
     */
    public long getBookingDate() { return bookingDate; }

    /**
     * Sets the booking date timestamp
     * @param bookingDate Booking date in milliseconds
     */
    public void setBookingDate(long bookingDate) { this.bookingDate = bookingDate; }

    /**
     * Gets additional notes for the booking
     * @return Notes string
     */
    public String getNotes() { return notes; }

    /**
     * Sets additional notes for the booking
     * @param notes Notes string
     */
    public void setNotes(String notes) { this.notes = notes; }

    /**
     * Checks if booking is confirmed
     * @return true if booking status is CONFIRMED
     */
    public boolean isConfirmed() {
        return "CONFIRMED".equals(bookingStatus);
    }

    /**
     * Checks if payment is completed
     * @return true if payment status is PAID
     */
    public boolean isPaid() {
        return "PAID".equals(paymentStatus);
    }

    /**
     * Checks if booking can be cancelled within 24 hours of creation
     * @return true if booking is within 24 hours and can be cancelled
     */
    public boolean canBeCancelledWithin24Hours() {
        long currentTime = System.currentTimeMillis();
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
        long timeSinceBooking = currentTime - this.bookingDate;

        // Can be cancelled if it's within 24 hours and status is CONFIRMED or PENDING
        return timeSinceBooking <= twentyFourHoursInMillis &&
               ("CONFIRMED".equals(bookingStatus) || "PENDING".equals(bookingStatus));
    }

    /**
     * Gets the remaining time in hours for cancellation eligibility
     * @return remaining hours for cancellation, 0 if expired
     */
    public long getRemainingCancellationHours() {
        long currentTime = System.currentTimeMillis();
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        long timeSinceBooking = currentTime - this.bookingDate;
        long remainingTime = twentyFourHoursInMillis - timeSinceBooking;

        if (remainingTime <= 0) {
            return 0;
        }

        return remainingTime / (60 * 60 * 1000); // Convert to hours
    }
}
