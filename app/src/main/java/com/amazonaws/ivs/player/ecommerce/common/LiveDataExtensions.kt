package com.amazonaws.ivs.player.ecommerce.common

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData

class Consumable<out T>(private val content: T) {

    private var isConsumed = false

    fun consume(): T? {
        return if (isConsumed) {
            null
        } else {
            isConsumed = true
            content
        }
    }

    val consumedValue get() = content
}

@Suppress("unused")
class ConsumableLiveData<T> : MutableLiveData<Consumable<T>>() {

    val consumedValue get() = value?.consumedValue

    fun postConsumable(data: T) {
        postValue(Consumable(data))
    }

    fun consume() {
        value?.consume()
    }

    inline fun observeConsumable(owner: LifecycleOwner, crossinline onConsumed: (T) -> Unit) {
        observe(owner, { it?.consume()?.let(onConsumed) })
    }
}
