package com.example.tourmanagement.utils;

import android.content.Context;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Tour;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class to populate the database with sample tour data for testing.
 * This helps with testing the booking functionality when the database is empty.
 */
public class SampleDataUtil {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Adds sample tours to the database if it's empty
     * @param context Application context
     */
    public static void populateSampleData(Context context) {
        executor.execute(() -> {
            TourManagementDatabase database = TourManagementDatabase.getDatabase(context);

            // Check if tours already exist
            int tourCount = database.tourDao().getAllTours().size();
            if (tourCount > 0) {
                android.util.Log.d("SampleDataUtil", "Tours already exist in database, skipping sample data");
                return;
            }

            android.util.Log.d("SampleDataUtil", "Adding sample tours to database");

            // Create sample tours with working image URLs
            Tour tour1 = new Tour(
                "Paris City Tour",
                "https://images.unsplash.com/photo-1502602898536-47ad22581b52?w=500&h=300&fit=crop",
                "Paris, France",
                System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L), // 7 days from now
                "Experience the beauty of Paris with visits to the Eiffel Tower, Louvre Museum, and Seine River cruise.",
                299.99,
                20,
                3
            );

            Tour tour2 = new Tour(
                "Tokyo Adventure",
                "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=500&h=300&fit=crop",
                "Tokyo, Japan",
                System.currentTimeMillis() + (14 * 24 * 60 * 60 * 1000L), // 14 days from now
                "Discover modern Tokyo and traditional culture with visits to temples, anime districts, and Mount Fuji.",
                499.99,
                15,
                5
            );

            Tour tour3 = new Tour(
                "New York Highlights",
                "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=500&h=300&fit=crop",
                "New York, USA",
                System.currentTimeMillis() + (21 * 24 * 60 * 60 * 1000L), // 21 days from now
                "See the best of NYC including Times Square, Central Park, Statue of Liberty, and Broadway shows.",
                399.99,
                25,
                4
            );

            Tour tour4 = new Tour(
                "Bali Beach Escape",
                "https://images.unsplash.com/photo-1537953773345-d172ccf13cf1?w=500&h=300&fit=crop",
                "Bali, Indonesia",
                System.currentTimeMillis() + (28 * 24 * 60 * 60 * 1000L), // 28 days from now
                "Relax on pristine beaches, explore ancient temples, and enjoy the tropical paradise of Bali.",
                349.99,
                18,
                6
            );

            Tour tour5 = new Tour(
                "London Heritage Tour",
                "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=500&h=300&fit=crop",
                "London, UK",
                System.currentTimeMillis() + (10 * 24 * 60 * 60 * 1000L), // 10 days from now
                "Explore historic London with visits to Buckingham Palace, Tower Bridge, and the British Museum.",
                279.99,
                22,
                3
            );

            // Insert tours into database
            long id1 = database.tourDao().insertTour(tour1);
            long id2 = database.tourDao().insertTour(tour2);
            long id3 = database.tourDao().insertTour(tour3);
            long id4 = database.tourDao().insertTour(tour4);
            long id5 = database.tourDao().insertTour(tour5);

            android.util.Log.d("SampleDataUtil", "Sample tours inserted with IDs: " + id1 + ", " + id2 + ", " + id3 + ", " + id4 + ", " + id5);
        });
    }
}
