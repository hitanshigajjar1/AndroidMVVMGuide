package com.ext.androidmvvmguide.utils

/**
 * A generic sealed class that represents the result of an operation
 * Used to handle success, error, and loading states consistently
 */
sealed class Result<out T> {
    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Error state with exception
     */
    data class Error(val exception: Throwable, val message: String? = exception.message) : Result<Nothing>()

    /**
     * Loading state (optional, can be used for initial loading)
     */
    object Loading : Result<Nothing>()
}

/**
 * Extension function to safely extract data from Result
 */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

/**
 * Extension function to check if Result is success
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/**
 * Extension function to check if Result is error
 */
fun <T> Result<T>.isError(): Boolean = this is Result.Error