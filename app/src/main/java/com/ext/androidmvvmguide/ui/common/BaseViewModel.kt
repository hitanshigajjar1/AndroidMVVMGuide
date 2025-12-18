package com.ext.androidmvvmguide.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Base ViewModel with common functionality
 * All ViewModels should extend this class
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * Global exception handler for coroutines
     * Can be overridden in child classes
     */
    protected open val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    /**
     * Handle errors from coroutines
     * Override in child classes for custom error handling
     */
    protected open fun handleError(throwable: Throwable) {
        // Default implementation
        throwable.printStackTrace()
    }

    /**
     * Launch a coroutine with exception handling
     */
    protected fun launchSafe(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }
}