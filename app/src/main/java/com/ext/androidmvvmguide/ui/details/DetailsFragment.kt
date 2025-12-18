package com.ext.androidmvvmguide.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ext.androidmvvmguide.App
import com.ext.androidmvvmguide.data.model.User
import com.ext.androidmvvmguide.databinding.FragmentDetailsBinding
import com.ext.androidmvvmguide.ui.common.BaseFragment
import com.ext.androidmvvmguide.utils.Constants
import com.ext.androidmvvmguide.utils.hide
import com.ext.androidmvvmguide.utils.show
import com.ext.androidmvvmguide.utils.showToast

/**
 * Details Fragment - Displays detailed user information
 * Simplified without Hilt
 */
class DetailsFragment : BaseFragment<FragmentDetailsBinding>() {

    // Get arguments from Bundle
    private val userId: Int by lazy {
        arguments?.getInt(Constants.ARG_USER_ID, -1) ?: -1
    }

    private val userName: String by lazy {
        arguments?.getString(Constants.ARG_USER_NAME) ?: "User Details"
    }

    /**
     * ViewModel instance with factory
     */
    private val viewModel: DetailsViewModel by viewModels {
        DetailsViewModelFactory(App.instance.userRepository, userId)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDetailsBinding {
        return FragmentDetailsBinding.inflate(inflater, container, false)
    }

    override fun setupUI() {
        // Set title from arguments
        binding.tvTitle.text = userName

        // Setup click listeners
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Load user details
        viewModel.loadUserDetails()
    }

    override fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            handleUiState(state)
        }
    }

    private fun handleUiState(state: DetailsUiState) {
        when (state) {
            is DetailsUiState.Loading -> {
                showLoading()
            }

            is DetailsUiState.Success -> {
                showSuccess(state.user)
            }

            is DetailsUiState.Error -> {
                showError(state.message)
            }
        }
    }

    private fun showLoading() {
        binding.apply {
            progressBar.show()
            layoutContent.hide()
            tvError.hide()
        }
    }

    private fun showSuccess(user: User) {
        binding.apply {
            progressBar.hide()
            layoutContent.show()
            tvError.hide()

            // Populate user details
            tvName.text = user.name
            tvUsername.text = "@${user.username}"
            tvEmail.text = user.email
            tvPhone.text = user.phone
            tvWebsite.text = user.website
        }
    }

    private fun showError(message: String) {
        binding.apply {
            progressBar.hide()
            layoutContent.hide()
            tvError.show()
            tvError.text = message
        }
        showToast(message)
    }
}