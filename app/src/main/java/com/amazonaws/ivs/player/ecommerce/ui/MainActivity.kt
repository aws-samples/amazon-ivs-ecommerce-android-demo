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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.storeButton.setOnClickListener {
            when (binding.motionLayout.currentState) {
                R.id.state_normal -> {
                    when (anchorType) {
                        AnchorType.TOP_LEFT -> binding.motionLayout.setTransition(R.id.state_normal_to_streams_top_left)
                        AnchorType.TOP_RIGHT -> binding.motionLayout.setTransition(R.id.state_normal_to_streams_top_right)
                        AnchorType.BOTTOM_LEFT -> binding.motionLayout.setTransition(R.id.state_normal_to_streams_bottom_left)
                        AnchorType.BOTTOM_RIGHT -> binding.motionLayout.setTransition(R.id.state_normal_to_streams_bottom_right)
                    }
                    binding.motionLayout.transitionToEnd()
                }
            }
        }
        binding.playerView.setOnClickListener {
            if (touchOffset != null) return@setOnClickListener
            when (binding.motionLayout.currentState) {
                R.id.state_normal -> {
                    binding.motionLayout.setTransition(R.id.state_normal_to_tapped)
                    binding.motionLayout.transitionToEnd()
                }
                R.id.state_tapped -> {
                    binding.motionLayout.setTransition(R.id.state_tapped_to_normal)
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
                viewModel.playerSize?.let { size ->
                    binding.player.zoomToFit(size)
                }
            }
        })

        binding.productAdd.setOnClickListener { /* Ignored */ }
        binding.productBuy.setOnClickListener { /* Ignored */ }
        binding.productList.adapter = adapter
        binding.player.onReady {
            viewModel.initPlayer(binding.player)
        }
        binding.motionLayout.getConstraintSet(binding.motionLayout.currentState).run {
            getConstraint(R.id.bar_bottom).propertySet.alpha = 0f
            getConstraint(R.id.timer_bubble).propertySet.alpha = 0f
        }

        launchMain {
            viewModel.products.collect { products ->
                adapter.products = products
                products.firstOrNull { it.isSelected }?.let { product ->
                    binding.product = product
                    binding.motionLayout.getConstraintSet(binding.motionLayout.currentState).run {
                        getConstraint(R.id.bar_bottom).propertySet.alpha = 1f
                        getConstraint(R.id.timer_bubble).propertySet.alpha = 1f
                    }
                    binding.barBottom.animateAlpha(1f)
                    binding.timerBubble.animateAlpha(1f)
                }
            }
        }
        launchMain {
            viewModel.onLoading.collect { isLoading ->
                binding.playerLoading.animateAlpha(if (isLoading) ALPHA_VISIBLE else ALPHA_GONE)
                binding.player.animateAlpha(if (isLoading) ALPHA_GONE else ALPHA_VISIBLE)
            }
        }
        launchMain {
            viewModel.onSizeChanged.collect { size ->
                binding.player.zoomToFit(size)
            }
        }
        launchMain {
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
                    launchMain {
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

    private fun updatePlayerState() = launchMain {
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
            viewModel.playerSize?.let { size ->
                binding.player.zoomToFit(size)
            }
        }
    }

    private fun restorePlayerView() {
        when (anchorType) {
            AnchorType.TOP_LEFT -> binding.motionLayout.setTransition(R.id.state_streams_to_normal_top_left)
            AnchorType.TOP_RIGHT -> binding.motionLayout.setTransition(R.id.state_streams_to_normal_top_right)
            AnchorType.BOTTOM_LEFT -> binding.motionLayout.setTransition(R.id.state_streams_to_normal_bottom_left)
            AnchorType.BOTTOM_RIGHT -> binding.motionLayout.setTransition(R.id.state_streams_to_normal_bottom_right)
        }
        binding.motionLayout.transitionToEnd()
        viewModel.playerSize?.let { size ->
            binding.player.zoomToFit(size)
        }
    }
}
