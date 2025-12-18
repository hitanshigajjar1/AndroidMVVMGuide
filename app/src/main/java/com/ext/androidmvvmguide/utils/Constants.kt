package com.ext.androidmvvmguide.utils

/**
 * Constants used throughout the application
 */
object Constants {

    // API Configuration
    const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    const val NETWORK_TIMEOUT = 30L // seconds

    // Database Configuration
    const val DATABASE_NAME = "mvvm_guide_database"
    const val DATABASE_VERSION = 1

    // Preferences Keys
    const val PREF_NAME = "mvvm_guide_prefs"
    const val PREF_IS_FIRST_LAUNCH = "is_first_launch"

    // Navigation Arguments
    const val ARG_USER_ID = "user_id"
    const val ARG_USER_NAME = "user_name"

    // Error Messages
    const val ERROR_NETWORK = "Network error. Please check your connection."
    const val ERROR_UNKNOWN = "An unexpected error occurred."
    const val ERROR_NO_DATA = "No data available."
}