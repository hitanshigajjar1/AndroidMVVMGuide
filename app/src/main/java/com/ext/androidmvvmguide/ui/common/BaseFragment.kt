package com.ext.androidmvvmguide.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Base Fragment with ViewBinding support
 * All fragments should extend this class
 *
 * @param VB ViewBinding type
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null

    /**
     * ViewBinding instance (non-null when view is created)
     */
    protected val binding get() = _binding!!

    /**
     * Abstract function to inflate ViewBinding
     * Must be implemented by child classes
     */
    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /**
     * Setup UI components
     * Called after view is created
     */
    abstract fun setupUI()

    /**
     * Setup observers for LiveData/Flow
     * Called after view is created
     */
    abstract fun setupObservers()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}