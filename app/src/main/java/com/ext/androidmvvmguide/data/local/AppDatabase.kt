package com.ext.androidmvvmguide.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ext.androidmvvmguide.utils.Constants

/**
 * Room Database class
 *
 * @Database annotation marks the class as a Room database
 * - entities: List of entity classes
 * - version: Database version number (increment when schema changes)
 * - exportSchema: Whether to export schema to a folder
 */
@Database(
    entities = [UserEntity::class],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Abstract function to get UserDao
     * Room generates the implementation
     */
    abstract fun userDao(): UserDao
}