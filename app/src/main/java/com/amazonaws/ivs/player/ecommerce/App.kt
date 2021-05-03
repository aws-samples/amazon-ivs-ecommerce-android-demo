package com.amazonaws.ivs.player.ecommerce

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.amazonaws.ivs.player.ecommerce.common.LineNumberDebugTree
import timber.log.Timber

open class App : Application(), ViewModelStoreOwner {

    override fun getViewModelStore() = appViewModelStore

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree("Ecommerce"))
        }
    }

    companion object {
        private val appViewModelStore: ViewModelStore by lazy {
            ViewModelStore()
        }
    }
}
