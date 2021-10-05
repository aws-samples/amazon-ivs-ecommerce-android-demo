package com.amazonaws.ivs.player.ecommerce.common

import kotlinx.coroutines.delay

fun Int.onTick(onTick: (Int) -> Unit, onFinished: () -> Unit) {
    launchMain {
        var currentTime = this@onTick
        onTick(currentTime)
        if (currentTime < 0) {
            onFinished()
            return@launchMain
        }
        delay(TIMEOUT_INTERVAL)
        currentTime -= 1
        currentTime.onTick(onTick, onFinished)
    }
}
