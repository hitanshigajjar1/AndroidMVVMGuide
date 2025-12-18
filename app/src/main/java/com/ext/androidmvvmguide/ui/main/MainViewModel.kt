package com.ext.androidmvvmguide.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ext.androidmvvmguide.data.repository.UserRepository
import com.ext.androidmvvmguide.utils.Result
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for Main Screen
 * Simplified without Hilt
 */
class MainViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // Private MutableLiveData (internal use only)
    private val _uiState = MutableLiveData<MainUiState>(MainUiState.Idle)

    // Public LiveData (exposed to UI)
    val uiState: LiveData<MainUiState> = _uiState

    /**
     * Load users from repository
     *
     * @param forceRefresh If true, bypasses cache and fetches from API
     */
    fun loadUsers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            userRepository.getUsers(forceRefresh)
                .catch { exception ->
                    // Handle Flow exceptions
                    _uiState.value = MainUiState.Error(
                        exception.message ?: "Unknown error occurred"
                    )
                }
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.value = MainUiState.Loading
                        }

                        is Result.Success -> {
                            if (result.data.isEmpty()) {
                                _uiState.value = MainUiState.Empty
                            } else {
                                _uiState.value = MainUiState.Success(result.data)
                            }
                        }

                        is Result.Error -> {
                            _uiState.value = MainUiState.Error(
                                result.message ?: "Failed to load users"
                            )
                        }
                    }
                }
        }
    }

    /**
     * Refresh users (force refresh from API)
     */
    fun refreshUsers() {
        loadUsers(forceRefresh = true)
    }

    /**
     * Clear cache and reload
     */
    fun clearCacheAndReload() {
        viewModelScope.launch {
            userRepository.clearCache()
            loadUsers(forceRefresh = true)
        }
    }
}

/**
 * Factory for creating MainViewModel with dependencies
 */
class MainViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}