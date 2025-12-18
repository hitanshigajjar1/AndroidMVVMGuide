package com.ext.androidmvvmguide.di

import android.content.Context
import androidx.room.Room
import com.ext.androidmvvmguide.data.local.AppDatabase
import com.ext.androidmvvmguide.data.local.UserDao
import com.ext.androidmvvmguide.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides Room Database instance
     *
     * @ApplicationContext - Provides app context (safe to hold)
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For demo purposes only!
            .build()
    }

    /**
     * Provides UserDao
     */
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}