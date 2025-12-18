package com.ext.androidmvvmguide.ui.common

/**
 * Base sealed class for representing UI states
 * Can be extended for specific screens
 */
sealed class UiState<out T> {
    /**
     * Initial idle state
     */
    object Idle : UiState<Nothing>()

    /**
     * Loading state
     */
    object Loading : UiState<Nothing>()

    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Error state with message
     */
    data class Error(val message: String) : UiState<Nothing>()
}

/**
 * Extension to check if state is loading
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * Extension to check if state is success
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success

/**
 * Extension to check if state is error
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error