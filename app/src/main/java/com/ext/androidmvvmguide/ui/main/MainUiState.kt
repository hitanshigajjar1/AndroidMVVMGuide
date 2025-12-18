package com.ext.androidmvvmguide.ui.main

import com.ext.androidmvvmguide.data.model.User

/**
 * UI State for Main Screen
 * Represents all possible states of the Main screen
 */
sealed class MainUiState {
    /**
     * Initial state before any action
     */
    object Idle : MainUiState()

    /**
     * Loading state while fetching data
     */
    object Loading : MainUiState()

    /**
     * Success state with list of users
     */
    data class Success(val users: List<User>) : MainUiState()

    /**
     * Error state with error message
     */
    data class Error(val message: String) : MainUiState()

    /**
     * Empty state when no data available
     */
    object Empty : MainUiState()
}