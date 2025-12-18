package com.ext.androidmvvmguide.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for User operations
 * Defines database operations for the User entity
 */
@Dao
interface UserDao {

    /**
     * Get all users from database
     * Returns Flow for reactive updates
     */
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    /**
     * Get a specific user by ID
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    /**
     * Insert a single user
     * OnConflictStrategy.REPLACE: Replace if already exists
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Insert multiple users
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    /**
     * Update an existing user
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Delete a specific user
     */
    @Delete
    suspend fun deleteUser(user: UserEntity)

    /**
     * Delete all users from database
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    /**
     * Get user count
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}