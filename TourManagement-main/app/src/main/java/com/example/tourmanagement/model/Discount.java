package com.example.tourmanagement.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

/**
 * Discount entity class for managing tour discounts in the tour management system.
 * This class defines the structure for discount information stored in SQLite database.
 *
 * Features:
 * - Percentage and fixed amount discounts
 * - Date-based validity periods
 * - Tour-specific or global discounts
 * - Discount codes and promotional campaigns
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-24
 */
@Entity(tableName = "discounts",
        foreignKeys = {
            @ForeignKey(entity = Tour.class,
                       parentColumns = "id",
                       childColumns = "tourId",
                       onDelete = ForeignKey.CASCADE)
        })
public class Discount {
    /**
     * Unique identifier for the discount (auto-generated primary key)
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * Foreign key reference to the tour (null for global discounts)
     * Links discount to a specific tour
     */
    private Integer tourId;

    /**
     * Name/title of the discount promotion
     * Displayed in UI and for admin management
     */
    private String discountName;

    /**
     * Description of the discount
     * Details about the promotion
     */
    private String description;

    /**
     * Type of discount: PERCENTAGE or FIXED_AMOUNT
     * Determines how the discount value is applied
     */
    private String discountType;

    /**
     * Discount value (percentage 0-100 or fixed amount)
     * The actual discount amount
     */
    private double discountValue;

    /**
     * Maximum discount amount (for percentage discounts)
     * Caps the maximum savings
     */
    private double maxDiscountAmount;

    /**
     * Minimum order amount to qualify for discount
     * Threshold for discount eligibility
     */
    private double minOrderAmount;

    /**
     * Discount code for promotional discounts
     * Optional code users can enter
     */
    private String discountCode;

    /**
     * Start date of the discount period (timestamp in milliseconds)
     * When the discount becomes active
     */
    private long startDate;

    /**
     * End date of the discount period (timestamp in milliseconds)
     * When the discount expires
     */
    private long endDate;

    /**
     * Whether the discount is currently active
     * Admin can enable/disable discounts
     */
    private boolean isActive;

    /**
     * Maximum number of times this discount can be used
     * Usage limit for promotional discounts
     */
    private int usageLimit;

    /**
     * Current number of times this discount has been used
     * Tracks usage for limits
     */
    private int currentUsage;

    /**
     * Timestamp when discount was created (in milliseconds)
     * For tracking and administrative purposes
     */
    private long createdAt;

    /**
     * Discount types enumeration
     */
    public static class DiscountType {
        public static final String PERCENTAGE = "PERCENTAGE";
        public static final String FIXED_AMOUNT = "FIXED_AMOUNT";
    }

    /**
     * Default constructor
     * Initializes discount with default values and current timestamp
     */
    public Discount() {
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
        this.currentUsage = 0;
        this.discountType = DiscountType.PERCENTAGE;
    }

    /**
     * Constructor for creating a new discount
     *
     * @param tourId Tour ID (null for global discounts)
     * @param discountName Name of the discount
     * @param discountType Type of discount (PERCENTAGE or FIXED_AMOUNT)
     * @param discountValue Discount value
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     */
    public Discount(Integer tourId, String discountName, String discountType,
                   double discountValue, long startDate, long endDate) {
        this.tourId = tourId;
        this.discountName = discountName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
        this.currentUsage = 0;
    }

    // Getter and Setter methods

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getTourId() { return tourId; }
    public void setTourId(Integer tourId) { this.tourId = tourId; }

    public String getDiscountName() { return discountName; }
    public void setDiscountName(String discountName) { this.discountName = discountName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }

    public double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(double minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }

    public int getCurrentUsage() { return currentUsage; }
    public void setCurrentUsage(int currentUsage) { this.currentUsage = currentUsage; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Business logic methods

    /**
     * Checks if the discount is currently valid
     * @return true if discount is active and within date range
     */
    public boolean isValid() {
        if (!isActive) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        boolean withinDateRange = currentTime >= startDate && currentTime <= endDate;
        boolean withinUsageLimit = usageLimit <= 0 || currentUsage < usageLimit;

        return withinDateRange && withinUsageLimit;
    }

    /**
     * Calculates discount amount for a given order total
     * @param orderTotal Total order amount
     * @return Discount amount to apply
     */
    public double calculateDiscountAmount(double orderTotal) {
        if (!isValid() || orderTotal < minOrderAmount) {
            return 0.0;
        }

        double discountAmount = 0.0;

        if (DiscountType.PERCENTAGE.equals(discountType)) {
            discountAmount = orderTotal * (discountValue / 100.0);
            // Apply maximum discount limit if set
            if (maxDiscountAmount > 0 && discountAmount > maxDiscountAmount) {
                discountAmount = maxDiscountAmount;
            }
        } else if (DiscountType.FIXED_AMOUNT.equals(discountType)) {
            discountAmount = Math.min(discountValue, orderTotal);
        }

        return discountAmount;
    }

    /**
     * Calculates final price after applying discount
     * @param originalPrice Original price
     * @return Final price after discount
     */
    public double applyDiscount(double originalPrice) {
        double discountAmount = calculateDiscountAmount(originalPrice);
        return originalPrice - discountAmount;
    }

    /**
     * Checks if discount is applicable to a specific order amount
     * @param orderAmount Order amount to check
     * @return true if discount can be applied
     */
    public boolean isApplicable(double orderAmount) {
        return isValid() && orderAmount >= minOrderAmount;
    }

    /**
     * Increments the usage count
     */
    public void incrementUsage() {
        this.currentUsage++;
    }

    /**
     * Checks if discount has usage limit and is within limit
     * @return true if within usage limit or no limit set
     */
    public boolean isWithinUsageLimit() {
        return usageLimit <= 0 || currentUsage < usageLimit;
    }

    /**
     * Gets remaining uses for limited discounts
     * @return Number of remaining uses (-1 for unlimited)
     */
    public int getRemainingUses() {
        if (usageLimit <= 0) {
            return -1; // Unlimited
        }
        return Math.max(0, usageLimit - currentUsage);
    }
}
