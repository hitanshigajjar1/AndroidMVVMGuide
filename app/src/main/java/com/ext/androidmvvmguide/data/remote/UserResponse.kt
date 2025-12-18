package com.ext.androidmvvmguide.data.remote

import com.ext.androidmvvmguide.data.model.User
import com.google.gson.annotations.SerializedName

/**
 * Network response model for User API
 * Maps JSON response to Kotlin object
 */
data class UserResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("website")
    val website: String
)

/**
 * Extension function to convert API response to domain model
 */
fun UserResponse.toDomainModel(): User {
    return User(
        id = id,
        name = name,
        username = username,
        email = email,
        phone = phone,
        website = website
    )
}

/**
 * Extension function to convert list of responses
 */
fun List<UserResponse>.toDomainModel(): List<User> {
    return map { it.toDomainModel() }
}