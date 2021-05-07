package com.amazonaws.ivs.player.ecommerce.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.ViewUtil
import com.amazonaws.ivs.player.ecommerce.R
import com.amazonaws.ivs.player.ecommerce.activities.adapters.ProductAdapter
import com.amazonaws.ivs.player.ecommerce.common.*
import com.amazonaws.ivs.player.ecommerce.common.Configuration.EXTRA_LINK
import com.amazonaws.ivs.player.ecommerce.common.Configuration.INTERACTION
import com.amazonaws.ivs.player.ecommerce.common.Configuration.TAG
import com.amazonaws.ivs.player.ecommerce.databinding.ActivityPlayerBinding
import com.amazonaws.ivs.player.ecommerce.viewModels.WatchViewModel
import kotlinx.android.synthetic.main.activity_player.*

class WatchActivity : AppCompatActivity() {

    private val productAdapter by lazy {
        ProductAdapter {
            startActivity(Intent(this, DetailsActivity::class.java))
            overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_default)
        }
    }

    private val viewModel: WatchViewModel by lazyViewModel {
        WatchViewModel(application)
    }

    private val timerHandler = Handler()
    private val timerRunnable = Runnable {
        runOnUiThread {
            Log.d(TAG, "Enable auto scroll")
            viewModel.enableAutoScroll.value = true
        }
    }

    private val holderCallback by lazy {
        object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                /* Ignored */
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                Log.d(TAG, "Surface destroyed")
                viewModel.updateSurface(null)
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                Log.d(TAG, "Surface created")
                viewModel.updateSurface(holder?.surface)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityPlayerBinding>(this, R.layout.activity_player)
            .apply {
                data = viewModel
                lifecycleOwner = this@WatchActivity
            }
        getIntentData()

        viewModel.playerParamsChanged.observe(this, Observer {
            Log.d(TAG, "Player layout params changed ${it.first} ${it.second}")
            if (viewModel.isLandscape.value == true) {
                ViewUtil.setLayoutParams(surface_view, it.first, it.second)
            } else {
                surface_view.setPortraitDimens(windowManager, it.first, it.second)
            }
        })

        viewModel.errorHappened.observe(this, Observer {
            Log.d(TAG, "Error dialog is shown")
            showDialog(it.first, it.second)
        })

        viewModel.products.observe(this, Observer { items ->
            Log.d(TAG, "Products updated: $items")
            productAdapter.items = items

            if (viewModel.enableAutoScroll.value == true) {
                val featuredPosition = productAdapter.getFeaturedPosition()
                if (featuredPosition != null && featuredPosition >= 0) {
                    product_view.smoothScrollToPosition(featuredPosition)
                }
            }
        })

        viewModel.isPlaying.observe(this, Observer { isPlaying ->
            if (isPlaying) {
                launchMain {
                    initBlurredBackground()
                }
            }
        })

        initUi()
        viewModel.playerStart(surface_view.holder.surface)
    }

    private fun getIntentData() {
        if (intent != null && intent.hasExtra(EXTRA_LINK)) {
            viewModel.url.value = intent.extras?.getString(EXTRA_LINK)
        }
    }

    private fun initUi() {
        surface_view.holder.addCallback(holderCallback)

        product_view.apply {
            adapter = productAdapter
            itemAnimator = null

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        restartTimer()
                    }
                }
            })
        }

        close_btn.setOnClickListener {
            onBackPressed()
        }

        root_view.setOnClickListener {
            restartTimer()
            viewModel.showControls()
        }

        overlay_view.setOnClickListener {
            if (viewModel.showControls.value == false) {
                viewModel.showControls.value = true
            }
        }

        PagerSnapHelper().attachToRecyclerView(product_view)
        restartTimer()
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.anim_default, R.anim.anim_slide_down)
    }

    override fun onResume() {
        super.onResume()
        viewModel.play()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
        surface_view.holder.removeCallback(holderCallback)
    }

    /**
     * Auto scroll timer reset
     */
    private fun restartTimer() {
        viewModel.enableAutoScroll.value = false
        timerHandler.removeCallbacks(timerRunnable)
        timerHandler.postDelayed(timerRunnable, INTERACTION)
    }

}
