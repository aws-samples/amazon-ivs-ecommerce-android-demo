package com.amazonaws.ivs.player.ecommerce.common

import android.content.Context
import kotlinx.coroutines.*
import timber.log.Timber

private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

fun launchMain(block: suspend CoroutineScope.() -> Unit) = mainScope.launch(
    context = CoroutineExceptionHandler { _, e -> Timber.d("Coroutine failed ${e.localizedMessage}") },
    block = block
)

fun Context.readJsonAsset(fileName: String): String {
    val inputStream = assets.open(fileName)
    val size = inputStream.available()
    val buffer = ByteArray(size)
    inputStream.read(buffer)
    inputStream.close()
    return String(buffer, Charsets.UTF_8)
}
