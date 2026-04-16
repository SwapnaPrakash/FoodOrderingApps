package com.swapna.foodapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.swapna.foodapp.data.local.converter.Converters
import com.swapna.foodapp.data.local.dao.CartDao
import com.swapna.foodapp.data.local.dao.MenuItemDao
import com.swapna.foodapp.data.local.dao.RestaurantDao
import com.swapna.foodapp.data.local.dao.UserDao
import com.swapna.foodapp.data.local.entity.CartItemEntity
import com.swapna.foodapp.data.local.entity.MenuItemEntity
import com.swapna.foodapp.data.local.entity.RestaurantEntity
import com.swapna.foodapp.data.local.entity.UserEntity
import com.swapna.foodapp.utils.AppConstants

@Database(
    entities = [
        RestaurantEntity::class,
        CartItemEntity::class,
        MenuItemEntity::class,
        UserEntity::class,
    ],
    version = AppConstants.DB_VERSION,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun restaurantDao(): RestaurantDao
    abstract fun cartDao(): CartDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun userDao(): UserDao

    companion object {

        // ── Migration 1 → 2 ───────────────────────────────────
        // WHY companion object?
        // Migration is a static value
        // DatabaseModule can access without instance:
        //   AppDatabase.MIGRATION_1_2
        //
        // Adds new columns added in this session:
        //   restaurants: distance_km, phone_number,
        //                opening_hours, highlights_json, known_for
        //   menu_items:  is_bestseller, is_available
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

                // ── restaurants table ─────────────────────────
                database.execSQL(
                    "ALTER TABLE ${AppConstants.TABLE_RESTAURANTS} " +
                            "ADD COLUMN ${AppConstants.COL_DISTANCE_KM} " +
                            "REAL NOT NULL DEFAULT 0.0"
                )
                database.execSQL(
                    "ALTER TABLE ${AppConstants.TABLE_RESTAURANTS} " +
                            "ADD COLUMN ${AppConstants.COL_PHONE_NUMBER} " +
                            "TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE ${AppConstants.TABLE_RESTAURANTS} " +
                            "ADD COLUMN ${AppConstants.COL_OPENING_HOURS} " +
                            "TEXT NOT NULL DEFAULT '11 AM - 11 PM'"
                )
                database.execSQL(
                    "ALTER TABLE ${AppConstants.TABLE_RESTAURANTS} " +
                            "ADD COLUMN ${AppConstants.COL_HIGHLIGHTS_JSON} " +
                            "TEXT NOT NULL DEFAULT '[]'"
                )
                database.execSQL(
                    "ALTER TABLE ${AppConstants.TABLE_RESTAURANTS} " +
                            "ADD COLUMN ${AppConstants.COL_KNOWN_FOR} " +
                            "TEXT NOT NULL DEFAULT ''"
                )

                // ── menu_items table ──────────────────────────
                database.execSQL(
                    "ALTER TABLE ${AppConstants.TABLE_MENU_ITEMS} " +
                            "ADD COLUMN ${AppConstants.COL_IS_BESTSELLER} " +
                            "INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE ${AppConstants.TABLE_MENU_ITEMS} " +
                            "ADD COLUMN ${AppConstants.COL_IS_AVAILABLE} " +
                            "INTEGER NOT NULL DEFAULT 1"
                )
            }
        }
    }


}
