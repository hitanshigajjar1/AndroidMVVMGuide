package com.ext.androidmvvmguide.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API interface
 * Defines all network endpoints
 */
interface ApiService {

    /**
     * Get all users from the API
     * Endpoint: GET /users
     */
    @GET("users")
    suspend fun getUsers(): List<UserResponse>

    /**
     * Get a specific user by ID
     * Endpoint: GET /users/{id}
     */
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): UserResponse
}