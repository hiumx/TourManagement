package com.example.tourmanagement.utils;

import android.content.Context;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Discount;
import com.example.tourmanagement.model.Tour;
import java.util.Calendar;
import java.util.List;

/**
 * Utility class to populate sample discount data for testing and demonstration
 */
public class DiscountDataPopulator {

    /**
     * Populates sample discount data if no discounts exist
     * @param context Application context
     */
    public static void populateSampleDiscounts(Context context) {
        TourManagementDatabase database = TourManagementDatabase.getDatabase(context);

        // Check if discounts already exist
        List<Discount> existingDiscounts = database.discountDao().getAllDiscounts();
        if (!existingDiscounts.isEmpty()) {
            return; // Discounts already exist, no need to populate
        }

        // Get available tours
        List<Tour> tours = database.tourDao().getActiveTours();
        if (tours.isEmpty()) {
            return; // No tours available to apply discounts to
        }

        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();

        // Set end date to 30 days from now
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        long endTime = calendar.getTimeInMillis();

        try {
            // Create some attractive sample discounts

            // 1. Early Bird Discount - 20% off for all tours
            Discount earlyBird = new Discount();
            earlyBird.setDiscountName("Early Bird Special");
            earlyBird.setDescription("Book early and save 20% on any tour!");
            earlyBird.setDiscountType(Discount.DiscountType.PERCENTAGE);
            earlyBird.setDiscountValue(20.0);
            earlyBird.setMaxDiscountAmount(100.0);
            earlyBird.setMinOrderAmount(50.0);
            earlyBird.setStartDate(currentTime);
            earlyBird.setEndDate(endTime);
            earlyBird.setActive(true);
            earlyBird.setTourId(null); // Global discount
            database.discountDao().insertDiscount(earlyBird);

            // 2. Weekend Special - $50 off for first tour
            if (tours.size() > 0) {
                Discount weekendSpecial = new Discount();
                weekendSpecial.setDiscountName("Weekend Getaway");
                weekendSpecial.setDescription("$50 off your weekend adventure!");
                weekendSpecial.setDiscountType(Discount.DiscountType.FIXED_AMOUNT);
                weekendSpecial.setDiscountValue(50.0);
                weekendSpecial.setMinOrderAmount(150.0);
                weekendSpecial.setStartDate(currentTime);
                weekendSpecial.setEndDate(endTime);
                weekendSpecial.setActive(true);
                weekendSpecial.setTourId(tours.get(0).getId());
                database.discountDao().insertDiscount(weekendSpecial);
            }

            // 3. Summer Sale - 15% off for second tour
            if (tours.size() > 1) {
                Discount summerSale = new Discount();
                summerSale.setDiscountName("Summer Sale");
                summerSale.setDescription("Beat the heat with 15% off!");
                summerSale.setDiscountType(Discount.DiscountType.PERCENTAGE);
                summerSale.setDiscountValue(15.0);
                summerSale.setMaxDiscountAmount(75.0);
                summerSale.setMinOrderAmount(100.0);
                summerSale.setStartDate(currentTime);
                summerSale.setEndDate(endTime);
                summerSale.setActive(true);
                summerSale.setTourId(tours.get(1).getId());
                database.discountDao().insertDiscount(summerSale);
            }

            // 4. Flash Sale - 30% off for third tour (if available)
            if (tours.size() > 2) {
                Discount flashSale = new Discount();
                flashSale.setDiscountName("Flash Sale");
                flashSale.setDescription("Limited time - 30% off this amazing tour!");
                flashSale.setDiscountType(Discount.DiscountType.PERCENTAGE);
                flashSale.setDiscountValue(30.0);
                flashSale.setMaxDiscountAmount(120.0);
                flashSale.setMinOrderAmount(80.0);
                flashSale.setStartDate(currentTime);
                flashSale.setEndDate(endTime);
                flashSale.setActive(true);
                flashSale.setTourId(tours.get(2).getId());
                database.discountDao().insertDiscount(flashSale);
            }

            android.util.Log.d("DiscountPopulator", "Sample discounts created successfully!");

        } catch (Exception e) {
            android.util.Log.e("DiscountPopulator", "Error creating sample discounts: " + e.getMessage());
        }
    }
}
