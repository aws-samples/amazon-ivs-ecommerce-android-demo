package com.amazonaws.ivs.player.ecommerce.common

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnLayout
import com.amazonaws.ivs.player.ecommerce.R
import com.amazonaws.ivs.player.ecommerce.models.AnchorPoint
import com.amazonaws.ivs.player.ecommerce.models.AnchorType
import com.amazonaws.ivs.player.ecommerce.models.PlayerParams
import com.amazonaws.ivs.player.ecommerce.models.SizeModel
import com.google.android.material.snackbar.Snackbar

fun View.animateAlpha(newAlpha: Float) {
    if (alpha != newAlpha) {
        animate().alpha(newAlpha).start()
    }
}

fun TextureView.onReady(onReady: (surface: Surface) -> Unit) {
    if (surfaceTexture != null) {
        onReady(Surface(surfaceTexture))
        return
    }
    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            surfaceTextureListener = null
            onReady(Surface(surfaceTexture))
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            /* Ignored */
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = false

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            /* Ignored */
        }
    }
}

fun CoordinatorLayout.showSnackBar(message: String) =
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()

fun View.isTouched(posX: Float, posY: Float): Boolean {
    val bounds = intArrayOf(x.toInt(), y.toInt())
    getLocationOnScreen(bounds)
    return posX > bounds[0] && posX < bounds[0] + width && posY > bounds[1] && posY < bounds[1] + height
}

fun View.getTouchOffset(posX: Float, posY: Float) = PlayerParams(posX - x, posY - y, width, height)

fun View.getAnchorPoint(params: PlayerParams): AnchorPoint {
    val margin = context.resources.getDimensionPixelSize(R.dimen.margin_normal)
    val x = if (params.x < width / 2) margin else width - params.width - margin
    val y = if (params.y < height / 2) margin else height - params.height - margin
    val type = when {
        x == margin && y == margin -> AnchorType.TOP_LEFT
        x == margin && y != margin -> AnchorType.BOTTOM_LEFT
        x != margin && y == margin -> AnchorType.TOP_RIGHT
        else -> AnchorType.BOTTOM_RIGHT
    }
    return AnchorPoint(x.toFloat(), y.toFloat(), type)
}

fun View.zoomToFit(videoSize: SizeModel) {
    (parent as View).doOnLayout { parentView ->
        val cardWidth = parentView.measuredWidth
        val cardHeight = parentView.measuredHeight
        val size = calculateSurfaceSize(cardWidth, cardHeight, videoSize.width, videoSize.height)
        layoutParams = FrameLayout.LayoutParams(size.width, size.height)
    }
}

private fun calculateSurfaceSize(surfaceWidth: Int, surfaceHeight: Int, videoWidth: Int, videoHeight: Int): SizeModel {
    val ratioHeight = videoHeight.toFloat() / videoWidth.toFloat()
    val ratioWidth = videoWidth.toFloat() / videoHeight.toFloat()
    val isPortrait = videoWidth < videoHeight
    val calculatedHeight = if (isPortrait) (surfaceWidth / ratioWidth).toInt() else (surfaceWidth * ratioHeight).toInt()
    val calculatedWidth = if (isPortrait) (surfaceHeight / ratioHeight).toInt() else (surfaceHeight * ratioWidth).toInt()
    return if (calculatedWidth >= surfaceWidth) {
        SizeModel(calculatedWidth, surfaceHeight)
    } else {
        SizeModel(surfaceWidth, calculatedHeight)
    }
}
