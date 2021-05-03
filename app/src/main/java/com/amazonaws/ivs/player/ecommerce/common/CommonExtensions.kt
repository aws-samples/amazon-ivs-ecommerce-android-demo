package com.amazonaws.ivs.player.ecommerce.common

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.amazonaws.ivs.player.ecommerce.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import timber.log.Timber

private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

fun launchMain(block: suspend CoroutineScope.() -> Unit) = mainScope.launch(
    context = CoroutineExceptionHandler { _, e -> Timber.d("Coroutine failed ${e.localizedMessage}") },
    block = block
)

fun Int.toColor(context: Context) = ContextCompat.getColor(context, this)

fun Activity.showErrorDialog() {
    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
        .setTitle(resources.getString(R.string.error))
        .setMessage(resources.getString(R.string.error_dialog_message))
        .setPositiveButton(resources.getString(R.string.close)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun Activity.hideKeyboard() {
    val view = currentFocus ?: window.decorView
    val token = view.windowToken
    view.clearFocus()
    ContextCompat.getSystemService(this, InputMethodManager::class.java)?.hideSoftInputFromWindow(token, 0)
}
