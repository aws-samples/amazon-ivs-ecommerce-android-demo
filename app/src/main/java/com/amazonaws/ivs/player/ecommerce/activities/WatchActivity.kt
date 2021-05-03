package com.amazonaws.ivs.player.ecommerce.activities

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.ViewUtil
import com.amazonaws.ivs.player.ecommerce.App
import com.amazonaws.ivs.player.ecommerce.R
import com.amazonaws.ivs.player.ecommerce.activities.adapters.ProductAdapter
import com.amazonaws.ivs.player.ecommerce.common.*
import com.amazonaws.ivs.player.ecommerce.common.Configuration.EXTRA_LINK
import com.amazonaws.ivs.player.ecommerce.common.Configuration.INTERACTION
import com.amazonaws.ivs.player.ecommerce.databinding.ActivityPlayerBinding
import com.amazonaws.ivs.player.ecommerce.viewModels.WatchViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import timber.log.Timber

class WatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val detailsDialog: BottomSheetBehavior<RelativeLayout> by lazy {
        BottomSheetBehavior.from(binding.detailsBottomSheet.root)
    }

    private val productAdapter by lazy {
        ProductAdapter {
            detailsDialog.open()
        }
    }

    private val viewModel by lazyViewModel(
        { application as App },
        { WatchViewModel() }
    )

    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = Runnable {
        launchMain {
            Timber.d("Enable auto scroll")
            viewModel.enableAutoScroll.value = true
        }
    }

    private val holderCallback by lazy {
        object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Timber.d("Surface created")
                viewModel.updateSurface(holder.surface)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                /* Ignored */
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Timber.d("Surface destroyed")
                viewModel.updateSurface(null)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.data = viewModel
        setContentView(binding.root)
        changeUtilityAppearance(R.color.black_color, false)

        if (intent != null && intent.hasExtra(EXTRA_LINK)) {
            viewModel.url = intent.extras?.getString(EXTRA_LINK)
        }
        viewModel.initPlayer(this)

        detailsDialog.hide()

        viewModel.playerParamsChanged.observe(this, {
            Timber.d("Player layout params changed ${it.first} ${it.second}")
            if (viewModel.isLandscape.value == true) {
                ViewUtil.setLayoutParams(binding.surfaceView, it.first, it.second)
            } else {
                binding.surfaceView.zoomToFit(windowManager, it.first, it.second)
            }
        })

        viewModel.errorHappened.observe(this, {
            Timber.d("Error dialog is shown")
            showErrorDialog()
        })

        viewModel.products.observe(this, { items ->
            Timber.d("Products updated: $items")
            productAdapter.items = items

            if (viewModel.enableAutoScroll.value == true) {
                val featuredPosition = productAdapter.getFeaturedPosition()
                if (featuredPosition >= 0) {
                    binding.productView.smoothScrollToPosition(featuredPosition)
                }
            }
        })

        viewModel.isPlaying.observe(this, { isPlaying ->
            if (isPlaying) {
                launchMain {
                    initBlurredBackground(binding)
                }
            }
        })

        initUi()
        viewModel.playerStart(binding.surfaceView.holder.surface)
    }

    private fun updateSurfaceView() {
        val playerParams = viewModel.playerParamsChanged.value
        binding.surfaceView.zoomToFit(windowManager, playerParams!!.first, playerParams.second)
    }

    private fun initUi() {
        binding.surfaceView.holder.addCallback(holderCallback)

        binding.productView.apply {
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

        binding.rootView.setOnClickListener {
            restartTimer()
            viewModel.showControls()
        }

        binding.overlayView.setOnClickListener {
            if (viewModel.showControls.value == false) {
                viewModel.showControls.value = true
            }
        }

        binding.detailsBottomSheet.backButton.setOnClickListener {
            detailsDialog.hide()
        }

        PagerSnapHelper().attachToRecyclerView(binding.productView)
        restartTimer()
    }

    private fun startMainActivity() {
        finish()
        overridePendingTransition(R.anim.anim_default, R.anim.anim_slide_down)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateSurfaceView()
    }

    override fun onBackPressed() {
        if (detailsDialog.isOpened()) {
            detailsDialog.hide()
        } else {
            startMainActivity()
        }
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
        binding.surfaceView.holder.removeCallback(holderCallback)
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
