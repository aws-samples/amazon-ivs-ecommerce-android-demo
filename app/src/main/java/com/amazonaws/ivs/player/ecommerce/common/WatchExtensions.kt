package com.amazonaws.ivs.player.ecommerce.common

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import androidx.annotation.WorkerThread
import com.amazonaws.ivs.player.ecommerce.R
import com.amazonaws.ivs.player.ecommerce.databinding.ActivityPlayerBinding
import kotlinx.coroutines.delay

/**
 * Blurred background
 * implemented using PixelCopy (API > 24)
 */
suspend fun Activity.initBlurredBackground(binding: ActivityPlayerBinding) {
    delay(Configuration.BACKGROUND_DELAY)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        runOnUiThread {
            if (binding.surfaceView.holder.surface.isValid) {
                val surfaceBitmap =
                    Bitmap.createBitmap(binding.surfaceView.width, binding.surfaceView.height, Bitmap.Config.ARGB_8888)

                // PixelCopy provides a mechanisms to issue pixel copy requests to allow for copy operations from Surface to Bitmap
                PixelCopy.request(
                    binding.surfaceView,
                    surfaceBitmap,
                    {
                        if (it == PixelCopy.SUCCESS) {
                            val scaledBitmap = surfaceBitmap.scaleBitmap(
                                binding.backgroundView.width,
                                binding.backgroundView.height
                            )
                            val blurredBitmap = scaledBitmap.blurBitmap(applicationContext)
                            binding.backgroundView.setImageBitmap(blurredBitmap)
                            binding.backgroundView.fadeIn()
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
            }
        }
    }
}

/**
 * Background Bitmap blur
 */
@WorkerThread
fun Bitmap.blurBitmap(applicationContext: Context): Bitmap {
    lateinit var renderScript: RenderScript
    try {
        // Create the output bitmap
        val output = Bitmap.createBitmap(
            width, height, config
        )
        // Blur the image
        renderScript = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
        val inAlloc = Allocation.createFromBitmap(renderScript, this)
        val outAlloc = Allocation.createTyped(renderScript, inAlloc.type)
        val theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        theIntrinsic.apply {
            setRadius(Configuration.BLUR_RADIUS)
            theIntrinsic.setInput(inAlloc)
            theIntrinsic.forEach(outAlloc)
        }
        outAlloc.copyTo(output)

        return output
    } finally {
        renderScript.finish()
    }
}

/**
 * Background Bitmap scaling
 */
@WorkerThread
fun Bitmap.scaleBitmap(maxWidth: Int, maxHeight: Int): Bitmap {
    var width = width
    var height = height

    when {
        width > height -> {
            // Landscape
            if (maxWidth > 0) {
                val ratio = width / maxWidth
                width = maxWidth
                if (ratio > 0) height /= ratio
            }
        }
        height > width -> {
            // Portrait
            if (maxHeight > 0) {
                val ratio = height / maxHeight
                height = maxHeight
                if (ratio > 0) width /= ratio
            }
        }
        else -> {
            // Square
            height = maxHeight
            width = maxWidth
        }
    }
    return Bitmap.createScaledBitmap(this, width, height, true)
}

/**
 * Height change animation for product placeholder view
 */
fun View.animatePlaceholderHeight(collapse: Boolean) {
    val normalHeight = context.resources.getDimension(R.dimen.placeholder_normal_height).toInt()
    val expandedHeight = context.resources.getDimension(R.dimen.placeholder_expanded_height).toInt()
    animateHeight(collapse, normalHeight, expandedHeight)
}

/**
 * Surface view width/height scaling
 * @param windowManager window manager
 * @param width video width
 * @param height video height
 */
fun SurfaceView.setPortraitDimens(windowManager: WindowManager, width: Int, height: Int) {
    val point = Point()
    windowManager.defaultDisplay.getSize(point)
    val aspectRatio = width.toFloat() / height.toFloat()
    val portraitWidth = point.y * aspectRatio
    if (portraitWidth >= point.x) {
        layoutParams.width = portraitWidth.toInt()
        layoutParams.height = point.y
    } else {
        val portraitHeight = point.x / aspectRatio
        layoutParams.width = point.x
        layoutParams.height = portraitHeight.toInt()
    }
}
