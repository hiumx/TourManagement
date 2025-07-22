package com.example.tourmanagement.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;
import com.example.tourmanagement.dao.BookingDao;
import com.example.tourmanagement.dao.TourDao;
import com.example.tourmanagement.dao.UserDao;
import com.example.tourmanagement.model.Booking;
import com.example.tourmanagement.model.Tour;
import com.example.tourmanagement.model.User;

/**
 * Room Database class for the Tour Management application.
 * This class serves as the main access point for the underlying SQLite database.
 *
 * Features:
 * - Centralized database access for all entities
 * - Singleton pattern for database instance management
 * - Automatic database creation and migration handling
 * - Thread-safe database operations
 *
 * Database Schema:
 * - Users table: Stores user authentication and profile data
 * - Tours table: Stores tour packages and availability information
 * - Bookings table: Stores booking transactions and payment data
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
@Database(
    entities = {User.class, Tour.class, Booking.class},
    version = 3,
    exportSchema = false
)
public abstract class TourManagementDatabase extends RoomDatabase {

    /**
     * Database instance for singleton pattern
     */
    private static volatile TourManagementDatabase INSTANCE;

    /**
     * Database name constant
     */
    private static final String DATABASE_NAME = "tour_management_db";

    /**
     * Gets the UserDao for user-related database operations
     * @return UserDao instance
     */
    public abstract UserDao userDao();

    /**
     * Gets the TourDao for tour-related database operations
     * @return TourDao instance
     */
    public abstract TourDao tourDao();

    /**
     * Gets the BookingDao for booking-related database operations
     * @return BookingDao instance
     */
    public abstract BookingDao bookingDao();

    /**
     * Migration from version 1 to 2: Add profileImagePath column to users table
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add profileImagePath column to users table
            database.execSQL("ALTER TABLE users ADD COLUMN profileImagePath TEXT");
        }
    };

    /**
     * Migration from version 2 to 3: Add isAdmin column to users table
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add isAdmin column to users table with NOT NULL constraint and default value
            database.execSQL("ALTER TABLE users ADD COLUMN isAdmin INTEGER NOT NULL DEFAULT 0");
        }
    };

    /**
     * Gets the singleton instance of the database
     * Implements thread-safe singleton pattern for database access
     *
     * @param context Application context
     * @return Database instance
     */
    public static TourManagementDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (TourManagementDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TourManagementDatabase.class,
                            DATABASE_NAME
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .allowMainThreadQueries() // For simplicity - in production, use background threads
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Destroys the database instance
     * Used for testing or when database needs to be recreated
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
