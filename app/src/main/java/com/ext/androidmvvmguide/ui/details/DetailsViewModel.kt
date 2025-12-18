package com.ext.androidmvvmguide.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ext.androidmvvmguide.data.repository.UserRepository
import com.ext.androidmvvmguide.utils.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for Details Screen
 * Simplified without Hilt
 */
class DetailsViewModel(
    private val userRepository: UserRepository,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableLiveData<DetailsUiState>()
    val uiState: LiveData<DetailsUiState> = _uiState

    /**
     * Load user details
     */
    fun loadUserDetails() {
        if (userId <= 0) {
            _uiState.value = DetailsUiState.Error("Invalid user ID")
            return
        }

        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading

            when (val result = userRepository.getUserById(userId)) {
                is Result.Success -> {
                    _uiState.value = DetailsUiState.Success(result.data)
                }

                is Result.Error -> {
                    _uiState.value = DetailsUiState.Error(
                        result.message ?: "Failed to load user details"
                    )
                }

                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }
}

/**
 * Factory for creating DetailsViewModel with dependencies
 */
class DetailsViewModelFactory(
    private val userRepository: UserRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(userRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}