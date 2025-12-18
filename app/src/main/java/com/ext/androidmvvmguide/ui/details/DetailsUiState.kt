package com.ext.androidmvvmguide.ui.details

import com.ext.androidmvvmguide.data.model.User

/**
 * UI State for Details Screen
 * Represents all possible states of the Details screen
 */
sealed class DetailsUiState {
    /**
     * Loading state while fetching user details
     */
    object Loading : DetailsUiState()

    /**
     * Success state with user details
     */
    data class Success(val user: User) : DetailsUiState()

    /**
     * Error state with error message
     */
    data class Error(val message: String) : DetailsUiState()
}