package com.ext.androidmvvmguide.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ext.androidmvvmguide.App
import com.ext.androidmvvmguide.R
import com.ext.androidmvvmguide.databinding.FragmentMainBinding
import com.ext.androidmvvmguide.ui.common.BaseFragment
import com.ext.androidmvvmguide.utils.*

/**
 * Main Fragment - Displays list of users
 * Simplified without Hilt
 */
class MainFragment : BaseFragment<FragmentMainBinding>() {

    /**
     * ViewModel instance with factory
     */
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(App.instance.userRepository)
    }

    /**
     * RecyclerView adapter
     */
    private val userAdapter by lazy {
        UserAdapter { user ->
            // Navigate to details screen using Bundle arguments
            val bundle = bundleOf(
                Constants.ARG_USER_ID to user.id,
                Constants.ARG_USER_NAME to user.name
            )
            findNavController().navigate(R.id.action_main_to_details, bundle)
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMainBinding {
        return FragmentMainBinding.inflate(inflater, container, false)
    }

    override fun setupUI() {
        // Setup RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }

        // Setup click listeners
        binding.btnLoad.setOnClickListener {
            viewModel.loadUsers()
        }

        binding.btnRefresh.setOnClickListener {
            viewModel.refreshUsers()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshUsers()
        }
    }

    override fun setupObservers() {
        // Observe UI state changes
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            handleUiState(state)
        }
    }

    /**
     * Handle different UI states
     */
    private fun handleUiState(state: MainUiState) {
        when (state) {
            is MainUiState.Idle -> {
                showIdle()
            }

            is MainUiState.Loading -> {
                showLoading()
            }

            is MainUiState.Success -> {
                showSuccess(state.users)
            }

            is MainUiState.Error -> {
                showError(state.message)
            }

            is MainUiState.Empty -> {
                showEmpty()
            }
        }
    }

    private fun showIdle() {
        binding.apply {
            progressBar.hide()
            recyclerView.hide()
            tvError.hide()
            tvEmpty.hide()
            swipeRefresh.isRefreshing = false
        }
    }

    private fun showLoading() {
        binding.apply {
            progressBar.show()
            recyclerView.hide()
            tvError.hide()
            tvEmpty.hide()
        }
    }

    private fun showSuccess(users: List<com.ext.androidmvvmguide.data.model.User>) {
        binding.apply {
            progressBar.hide()
            recyclerView.show()
            tvError.hide()
            tvEmpty.hide()
            swipeRefresh.isRefreshing = false

            userAdapter.submitList(users)
        }
        showToast("Loaded ${users.size} users")
    }

    private fun showError(message: String) {
        binding.apply {
            progressBar.hide()
            recyclerView.hide()
            tvError.show()
            tvEmpty.hide()
            swipeRefresh.isRefreshing = false

            tvError.text = message
        }
        showToast(message)
    }

    private fun showEmpty() {
        binding.apply {
            progressBar.hide()
            recyclerView.hide()
            tvError.hide()
            tvEmpty.show()
            swipeRefresh.isRefreshing = false
        }
    }
}