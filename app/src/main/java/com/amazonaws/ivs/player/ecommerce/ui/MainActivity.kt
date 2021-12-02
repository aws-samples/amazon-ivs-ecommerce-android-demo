package com.amazonaws.ivs.player.ecommerce.ui

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.view.doOnNextLayout
import com.amazonaws.ivs.player.ecommerce.App
import com.amazonaws.ivs.player.ecommerce.R
import com.amazonaws.ivs.player.ecommerce.common.*
import com.amazonaws.ivs.player.ecommerce.databinding.ActivityMainBinding
import com.amazonaws.ivs.player.ecommerce.models.AnchorType
import com.amazonaws.ivs.player.ecommerce.models.PlayerParams
import com.amazonaws.ivs.player.ecommerce.ui.adapters.ProductsAdapter
import com.amazonaws.ivs.player.ecommerce.ui.viewmodels.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by lazyViewModel(
        { application as App },
        { MainViewModel(readJsonAsset(JSON_FILE_NAME).asObject()) }
    )
    private val adapter by lazy { ProductsAdapter() }
    private var touchOffset: PlayerParams? = null
    private var anchorType = AnchorType.BOTTOM_RIGHT

    private val isShowingStreams get() = when (binding.motionLayout.currentState) {
        R.id.state_streams_bottom_right,
        R.id.state_streams_bottom_left,
        R.id.state_streams_top_right,
        R.id.state_streams_top_left -> true
        else -> false
    }
    private val isNormalOrMetadataState get() = when (binding.motionLayout.currentState) {
        R.id.state_normal,
        R.id.state_metadata -> true
        else -> false
    }
    private val isTappedState get() = when (binding.motionLayout.currentState) {
        R.id.state_tapped_normal,
        R.id.state_tapped_metadata -> true
        else -> false
    }
    private val isNormalState get() = binding.motionLayout.currentState == R.id.state_normal
    private val isTransitioning get() = binding.motionLayout.progress > 0 && binding.motionLayout.progress < 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.storeButton.setOnClickListener {
            if (isNormalOrMetadataState) {
                val endState = when (anchorType) {
                    AnchorType.TOP_LEFT -> if (isNormalState) R.id.state_normal_to_streams_top_left else
                        R.id.state_metadata_to_streams_top_left
                    AnchorType.TOP_RIGHT -> if (isNormalState) R.id.state_normal_to_streams_top_right else
                        R.id.state_metadata_to_streams_top_right
                    AnchorType.BOTTOM_LEFT -> if (isNormalState) R.id.state_normal_to_streams_bottom_left else
                        R.id.state_metadata_to_streams_bottom_left
                    AnchorType.BOTTOM_RIGHT -> if (isNormalState) R.id.state_normal_to_streams_bottom_right else
                        R.id.state_metadata_to_streams_bottom_right
                }
                binding.motionLayout.setTransition(endState)
                binding.motionLayout.transitionToEnd()
            }
        }
        binding.playerView.setOnClickListener {
            if (touchOffset != null) return@setOnClickListener
            when (binding.motionLayout.currentState) {
                R.id.state_normal -> {
                    Timber.d("Transition to normal tapped")
                    binding.motionLayout.setTransition(R.id.state_normal_to_tapped)
                    binding.motionLayout.transitionToEnd()
                }
                R.id.state_metadata -> {
                    Timber.d("Transition to metadata tapped")
                    binding.motionLayout.setTransition(R.id.state_metadata_to_tapped)
                    binding.motionLayout.transitionToEnd()
                }
                R.id.state_tapped_normal -> {
                    Timber.d("Transition to normal")
                    binding.motionLayout.setTransition(R.id.state_tapped_to_normal)
                    binding.motionLayout.transitionToEnd()
                }
                R.id.state_tapped_metadata -> {
                    Timber.d("Transition to metadata")
                    binding.motionLayout.setTransition(R.id.state_tapped_to_metadata)
                    binding.motionLayout.transitionToEnd()
                }
                R.id.state_streams_bottom_right,
                R.id.state_streams_bottom_left,
                R.id.state_streams_top_right,
                R.id.state_streams_top_left -> restorePlayerView()
            }
        }
        binding.motionLayout.setTransitionListener(object : TransitionAdapter() {
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                super.onTransitionCompleted(motionLayout, currentId)
                resizePlayer()
            }

            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {
                super.onTransitionChange(motionLayout, startId, endId, progress)
                if (endId == R.id.state_normal || endId == R.id.state_metadata) resizePlayer()
            }
        })

        binding.productAdd.setOnClickListener { /* Ignored */ }
        binding.productBuy.setOnClickListener { /* Ignored */ }
        binding.productList.adapter = adapter
        binding.player.onReady {
            viewModel.initPlayer(binding.player)
        }

        launchUI {
            viewModel.products.collect { products ->
                adapter.products = products
                var endId = if (isTappedState) R.id.state_tapped_normal else R.id.state_normal
                var transitionId = if (isTappedState) R.id.state_tapped_metadata_to_normal else R.id.state_metadata_to_normal
                var selectedProduct = viewModel.hasProductToSelect
                products.firstOrNull { it.isSelected }?.let { product ->
                    binding.product = product
                    selectedProduct = true
                }
                if (selectedProduct) {
                    endId = if (isTappedState) R.id.state_tapped_metadata else R.id.state_metadata
                    transitionId = if (isTappedState) R.id.state_tapped_normal_to_metadata else R.id.state_normal_to_metadata
                }
                if (binding.motionLayout.currentState != endId && !isTransitioning && (isNormalOrMetadataState || isTappedState)) {
                    Timber.d("Transitioning to metadata: ${endId == R.id.state_metadata || endId == R.id.state_tapped_metadata}")
                    binding.motionLayout.setTransition(transitionId)
                    binding.motionLayout.transitionToEnd()
                }
            }
        }
        launchUI {
            viewModel.onLoading.collect { isLoading ->
                binding.playerLoading.animateAlpha(if (isLoading) ALPHA_VISIBLE else ALPHA_GONE)
                binding.player.animateAlpha(if (isLoading) ALPHA_GONE else ALPHA_VISIBLE)
            }
        }
        launchUI {
            viewModel.onSizeChanged.collect { size ->
                binding.player.zoomToFit(size)
            }
        }
        launchUI {
            viewModel.onError.collect { error ->
                binding.root.showSnackBar(error.message)
            }
        }
    }

    override fun onBackPressed() {
        if (isShowingStreams) {
            restorePlayerView()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }

    override fun onResume() {
        super.onResume()
        viewModel.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null && isShowingStreams) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (binding.playerView.isTouched(event.x, event.y)) {
                        touchOffset = binding.playerView.getTouchOffset(event.x, event.y)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    touchOffset?.let { offset ->
                        binding.playerView.x = event.x - offset.x
                        binding.playerView.y = event.y - offset.y
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val eventDuration = event.eventTime - event.downTime
                    if (eventDuration <= CLICK_THRESHOLD || touchOffset == null) {
                        touchOffset = null
                        return super.dispatchTouchEvent(event)
                    }
                    val anchorPoint = binding.motionLayout.getAnchorPoint(PlayerParams(
                        event.x,
                        event.y,
                        binding.playerView.width,
                        binding.playerView.height
                    ))
                    anchorType = anchorPoint.type
                    launchUI {
                        delay(50L)
                        binding.playerView.animate()
                            .x(anchorPoint.x)
                            .y(anchorPoint.y)
                            .setDuration(ANIMATION_DURATION)
                            .start()
                        touchOffset = null
                        updatePlayerState()
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun resizePlayer() {
        viewModel.playerSize?.let { size ->
            binding.player.zoomToFit(size)
        }
    }

    private fun updatePlayerState() = launchUI {
        delay(ANIMATION_DURATION)
        val x = binding.playerView.x
        val y = binding.playerView.y
        when (anchorType) {
            AnchorType.TOP_LEFT -> binding.motionLayout.jumpToState(R.id.state_streams_top_left)
            AnchorType.TOP_RIGHT -> binding.motionLayout.jumpToState(R.id.state_streams_top_right)
            AnchorType.BOTTOM_LEFT -> binding.motionLayout.jumpToState(R.id.state_streams_bottom_left)
            AnchorType.BOTTOM_RIGHT -> binding.motionLayout.jumpToState(R.id.state_streams_bottom_right)
        }
        binding.motionLayout.requestLayout()
        binding.playerView.doOnNextLayout { view ->
            view.x = x
            view.y = y
            resizePlayer()
        }
    }

    private fun restorePlayerView() {
        val endState = when (anchorType) {
            AnchorType.TOP_LEFT -> if (viewModel.isShowingProduct) R.id.state_streams_to_metadata_top_left else
                R.id.state_streams_to_normal_top_left
            AnchorType.TOP_RIGHT -> if (viewModel.isShowingProduct) R.id.state_streams_to_metadata_top_right else
                R.id.state_streams_to_normal_top_right
            AnchorType.BOTTOM_LEFT -> if (viewModel.isShowingProduct) R.id.state_streams_to_metadata_bottom_left else
                R.id.state_streams_to_normal_bottom_left
            AnchorType.BOTTOM_RIGHT -> if (viewModel.isShowingProduct) R.id.state_streams_to_metadata_bottom_right else
                R.id.state_streams_to_normal_bottom_right
        }
        binding.motionLayout.setTransition(endState)
        binding.motionLayout.transitionToEnd()
        resizePlayer()
    }
}
