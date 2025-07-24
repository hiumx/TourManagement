package com.example.tourmanagement.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.tourmanagement.model.Discount;
import java.util.List;

/**
 * Data Access Object (DAO) for Discount entity operations.
 * Provides methods for discount management and application.
 *
 * Features:
 * - Complete discount lifecycle management
 * - Tour-specific and global discount queries
 * - Usage tracking and validation
 * - Discount code management
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-24
 */
@Dao
public interface DiscountDao {

    /**
     * Inserts a new discount into the database
     * @param discount Discount object to insert
     * @return The ID of the inserted discount
     */
    @Insert
    long insertDiscount(Discount discount);

    /**
     * Updates an existing discount
     * @param discount Discount object with updated information
     */
    @Update
    void updateDiscount(Discount discount);

    /**
     * Deletes a discount from the database
     * @param discount Discount object to delete
     */
    @Delete
    void deleteDiscount(Discount discount);

    /**
     * Gets all discounts from the database
     * @return List of all discounts
     */
    @Query("SELECT * FROM discounts ORDER BY createdAt DESC")
    List<Discount> getAllDiscounts();

    /**
     * Gets a discount by its ID
     * @param id Discount ID
     * @return Discount object if found, null otherwise
     */
    @Query("SELECT * FROM discounts WHERE id = :id")
    Discount getDiscountById(int id);

    /**
     * Gets all active discounts
     * @return List of active discounts
     */
    @Query("SELECT * FROM discounts WHERE isActive = 1 ORDER BY createdAt DESC")
    List<Discount> getActiveDiscounts();

    /**
     * Gets all discounts for a specific tour
     * @param tourId Tour ID
     * @return List of discounts for the tour
     */
    @Query("SELECT * FROM discounts WHERE tourId = :tourId ORDER BY createdAt DESC")
    List<Discount> getDiscountsByTourId(int tourId);

    /**
     * Gets all active discounts for a specific tour
     * @param tourId Tour ID
     * @return List of active discounts for the tour
     */
    @Query("SELECT * FROM discounts WHERE tourId = :tourId AND isActive = 1 " +
           "AND startDate <= :currentTime AND endDate >= :currentTime " +
           "ORDER BY discountValue DESC")
    List<Discount> getActiveDiscountsByTourId(int tourId, long currentTime);

    /**
     * Gets global discounts (not tied to specific tours)
     * @return List of global discounts
     */
    @Query("SELECT * FROM discounts WHERE tourId IS NULL ORDER BY createdAt DESC")
    List<Discount> getGlobalDiscounts();

    /**
     * Gets active global discounts
     * @return List of active global discounts
     */
    @Query("SELECT * FROM discounts WHERE tourId IS NULL AND isActive = 1 " +
           "AND startDate <= :currentTime AND endDate >= :currentTime " +
           "ORDER BY discountValue DESC")
    List<Discount> getActiveGlobalDiscounts(long currentTime);

    /**
     * Finds discount by code
     * @param discountCode Discount code
     * @return Discount object if found, null otherwise
     */
    @Query("SELECT * FROM discounts WHERE discountCode = :discountCode AND isActive = 1")
    Discount getDiscountByCode(String discountCode);

    /**
     * Gets valid discounts for a tour (including global discounts)
     * @param tourId Tour ID
     * @param currentTime Current timestamp
     * @return List of valid discounts
     */
    @Query("SELECT * FROM discounts WHERE " +
           "(tourId = :tourId OR tourId IS NULL) " +
           "AND isActive = 1 " +
           "AND startDate <= :currentTime " +
           "AND endDate >= :currentTime " +
           "AND (usageLimit <= 0 OR currentUsage < usageLimit) " +
           "ORDER BY discountValue DESC")
    List<Discount> getValidDiscountsForTour(int tourId, long currentTime);

    /**
     * Updates discount usage count
     * @param discountId Discount ID
     * @param newUsageCount New usage count
     */
    @Query("UPDATE discounts SET currentUsage = :newUsageCount WHERE id = :discountId")
    void updateDiscountUsage(int discountId, int newUsageCount);

    /**
     * Increments discount usage count
     * @param discountId Discount ID
     */
    @Query("UPDATE discounts SET currentUsage = currentUsage + 1 WHERE id = :discountId")
    void incrementDiscountUsage(int discountId);

    /**
     * Gets expired discounts for cleanup
     * @param currentTime Current timestamp
     * @return List of expired discounts
     */
    @Query("SELECT * FROM discounts WHERE endDate < :currentTime")
    List<Discount> getExpiredDiscounts(long currentTime);

    /**
     * Deactivates expired discounts
     * @param currentTime Current timestamp
     */
    @Query("UPDATE discounts SET isActive = 0 WHERE endDate < :currentTime AND isActive = 1")
    void deactivateExpiredDiscounts(long currentTime);

    /**
     * Gets discounts by type
     * @param discountType Discount type (PERCENTAGE or FIXED_AMOUNT)
     * @return List of discounts of specified type
     */
    @Query("SELECT * FROM discounts WHERE discountType = :discountType ORDER BY createdAt DESC")
    List<Discount> getDiscountsByType(String discountType);

    /**
     * Gets discounts within a date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of discounts within date range
     */
    @Query("SELECT * FROM discounts WHERE " +
           "startDate >= :startDate AND endDate <= :endDate " +
           "ORDER BY createdAt DESC")
    List<Discount> getDiscountsByDateRange(long startDate, long endDate);

    /**
     * Gets the best discount for a specific tour and order amount
     * @param tourId Tour ID
     * @param orderAmount Order amount
     * @param currentTime Current timestamp
     * @return Best applicable discount
     */
    @Query("SELECT * FROM discounts WHERE " +
           "(tourId = :tourId OR tourId IS NULL) " +
           "AND isActive = 1 " +
           "AND startDate <= :currentTime " +
           "AND endDate >= :currentTime " +
           "AND minOrderAmount <= :orderAmount " +
           "AND (usageLimit <= 0 OR currentUsage < usageLimit) " +
           "ORDER BY " +
           "CASE " +
           "WHEN discountType = 'FIXED_AMOUNT' THEN discountValue " +
           "WHEN discountType = 'PERCENTAGE' THEN " +
           "CASE " +
           "WHEN maxDiscountAmount > 0 THEN MIN(:orderAmount * discountValue / 100.0, maxDiscountAmount) " +
           "ELSE :orderAmount * discountValue / 100.0 " +
           "END " +
           "END DESC " +
           "LIMIT 1")
    Discount getBestDiscountForTour(int tourId, double orderAmount, long currentTime);

    /**
     * Gets total discount usage statistics
     * @return Total number of discount applications
     */
    @Query("SELECT SUM(currentUsage) FROM discounts")
    int getTotalDiscountUsage();

    /**
     * Gets discount count by status
     * @param isActive Active status
     * @return Count of discounts with specified status
     */
    @Query("SELECT COUNT(*) FROM discounts WHERE isActive = :isActive")
    int getDiscountCountByStatus(boolean isActive);
}
