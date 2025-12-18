package com.ext.androidmvvmguide.utils

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Extension functions for common operations
 */

// View Extensions
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

// Fragment Extensions
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}

fun Fragment.showLongToast(message: String) {
    showToast(message, Toast.LENGTH_LONG)
}

// String Extensions
fun String?.isValidEmail(): Boolean {
    return this?.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) ?: false
}

fun String?.orDefault(default: String = ""): String {
    return this ?: default
}

// Collection Extensions
fun <T> List<T>?.orEmpty(): List<T> {
    return this ?: emptyList()
}