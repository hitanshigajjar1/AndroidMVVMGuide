package com.ext.androidmvvmguide.data.model

/**
 * Domain model representing a User
 * This is used throughout the app (UI layer)
 */
data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String
) {
    companion object {
        fun empty() = User(
            id = 0,
            name = "",
            username = "",
            email = "",
            phone = "",
            website = ""
        )
    }
}