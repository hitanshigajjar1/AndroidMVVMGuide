package com.ext.androidmvvmguide.data.repository

import com.ext.androidmvvmguide.data.local.UserDao
import com.ext.androidmvvmguide.data.local.toDomainModel
import com.ext.androidmvvmguide.data.local.toEntity
import com.ext.androidmvvmguide.data.model.User
import com.ext.androidmvvmguide.data.remote.ApiService
import com.ext.androidmvvmguide.data.remote.toDomainModel
import com.ext.androidmvvmguide.utils.Constants
import com.ext.androidmvvmguide.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Repository pattern implementation
 * Single source of truth for User data
 * Simplified without Hilt - no @Inject annotation needed
 */
class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao
) {

    /**
     * Get all users with cache-first strategy
     */
    fun getUsers(forceRefresh: Boolean = false): Flow<Result<List<User>>> = flow {
        try {
            // Emit loading state
            emit(Result.Loading)

            // If not forcing refresh, check cache first
            if (!forceRefresh) {
                val cachedUsers = userDao.getUserCount()
                if (cachedUsers > 0) {
                    // Emit cached data
                    userDao.getAllUsers().map { entities ->
                        entities.toDomainModel()
                    }.collect { users ->
                        emit(Result.Success(users))
                    }
                    return@flow
                }
            }

            // Fetch from API
            val response = apiService.getUsers()
            val users = response.toDomainModel()

            // Cache the data
            userDao.insertUsers(users.toEntity())

            // Emit fresh data
            emit(Result.Success(users))

        } catch (e: Exception) {
            // Emit error with fallback to cache
            val cachedUsers = userDao.getUserCount()
            if (cachedUsers > 0) {
                userDao.getAllUsers().map { entities ->
                    entities.toDomainModel()
                }.collect { users ->
                    emit(Result.Success(users))
                }
            } else {
                emit(Result.Error(e, e.message ?: Constants.ERROR_UNKNOWN))
            }
        }
    }

    /**
     * Get a specific user by ID
     */
    suspend fun getUserById(userId: Int): Result<User> {
        return try {
            // Try to get from cache first
            val cachedUser = userDao.getUserById(userId)
            if (cachedUser != null) {
                Result.Success(cachedUser.toDomainModel())
            } else {
                // Fetch from API
                val response = apiService.getUserById(userId)
                val user = response.toDomainModel()

                // Cache it
                userDao.insertUser(user.toEntity())

                Result.Success(user)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: Constants.ERROR_UNKNOWN)
        }
    }

    /**
     * Get users from local database only
     */
    fun getUsersFromCache(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.toDomainModel()
        }
    }

    /**
     * Clear all cached users
     */
    suspend fun clearCache() {
        userDao.deleteAllUsers()
    }
}