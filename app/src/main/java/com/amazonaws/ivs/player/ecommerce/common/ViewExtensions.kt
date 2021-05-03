package com.amazonaws.ivs.player.ecommerce.common

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.amazonaws.ivs.player.ecommerce.R
import com.amazonaws.ivs.player.ecommerce.common.spans.BackgroundSpan

fun AppCompatActivity.changeUtilityAppearance(
    colorId: Int,
    isLight: Boolean = false
) {
    val utilityBackgroundColor = colorId.toColor(this)

    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            window.insetsController?.setSystemBarsAppearance(
                if (isLight) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
            window.insetsController?.setSystemBarsAppearance(
                if (isLight) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                if (isLight) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0
        }
        else -> {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (isLight) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0
        }
    }

    window.statusBarColor = utilityBackgroundColor
    window.navigationBarColor = utilityBackgroundColor
}

/**
 * Fade in view
 */
fun View.fadeIn() {
    if (this.visibility == View.INVISIBLE) {
        startAnimation(getFadeInAnimation())
        this.visibility = View.VISIBLE
    }
}

/**
 * Fade out view
 */
fun View.fadeOut() {
    if (this.visibility == View.VISIBLE) {
        startAnimation(getFadeOutAnimation())
        this.visibility = View.INVISIBLE
    }
}

/**
 * Slide up and fade in view
 * @param fromTop direction boolean
 */
fun View.slideUpFadeIn(fromTop: Boolean) {
    if (this.visibility == View.INVISIBLE) {
        val animation = AnimationSet(true).apply {
            addAnimation(getFadeInAnimation())
            addAnimation(getSlideUpAnimation(height.toFloat(), fromTop))
        }
        startAnimation(animation)
        this.visibility = View.VISIBLE
    }
}

/**
 * Slide down and fade out view
 * @param fromTop direction boolean
 */
fun View.slideDownFadeOut(fromTop: Boolean) {
    if (this.visibility == View.VISIBLE) {
        val animation = AnimationSet(true).apply {
            addAnimation(getFadeOutAnimation())
            addAnimation(getSlideDownAnimation(height.toFloat(), fromTop))
        }
        startAnimation(animation)
        this.visibility = View.INVISIBLE
    }
}

/**
 * Height change animation
 */
fun View.animateHeight(collapse: Boolean, normalHeight: Int, expandedHeight: Int) {
    val valueAnimator = getHeightValueAnimator(collapse, normalHeight, expandedHeight)

    valueAnimator.addUpdateListener {
        val value = it.animatedValue as Int
        layoutParams.height = value
        requestLayout()
    }

    AnimatorSet().apply {
        interpolator = AccelerateInterpolator()
        play(valueAnimator)
        start()
    }
}

/**
 *  Offer counter format
 *  @param time offer time string
 */
fun TextView.setCounter(time: String) {
    val counterText = SpannableStringBuilder("")
    val chars = time.toCharArray()

    for (char: Char in chars) {
        val spannable = SpannableString(char.toString())
        if (char.toString().isNotBlank()) {
            spannable.setBackgroundSpan(
                context,
                if (char.toString() == "1") 2f else if (char.toString() == "0") 0.5f else 1f
            )
        } else {
            counterText.append(":")
        }
        counterText.append(spannable)
    }
    text = counterText
}

/**
 * Slide up animation
 * @param height view height
 * @param fromTop direction boolean
 */
private fun getSlideUpAnimation(height: Float, fromTop: Boolean): Animation {
    val toHeight = if (fromTop) -height else height
    return TranslateAnimation(0f, 0f, toHeight, 0f).apply {
        duration = Configuration.ANIMATION_DURATION
        fillAfter = true
    }
}

/**
 * Slide down animation
 * @param height view height
 * @param fromTop direction boolean
 */
private fun getSlideDownAnimation(height: Float, fromTop: Boolean): Animation {
    val toHeight = if (fromTop) -height else height
    return TranslateAnimation(0f, 0f, 0f, toHeight).apply {
        duration = Configuration.ANIMATION_DURATION
        fillAfter = true
    }
}

/**
 * Fade in animation
 */
fun getFadeInAnimation(): Animation {
    return AlphaAnimation(0f, 1f).apply {
        interpolator = DecelerateInterpolator()
        duration = Configuration.ANIMATION_DURATION
    }
}

/**
 * Fade out animation
 */
fun getFadeOutAnimation(): Animation {
    return AlphaAnimation(1f, 0f).apply {
        interpolator = DecelerateInterpolator()
        startOffset = Configuration.ANIMATION_DURATION
        duration = Configuration.ANIMATION_DURATION
    }
}

/**
 * Height value animator
 */
private fun getHeightValueAnimator(collapse: Boolean, expandHeight: Int, collapseHeight: Int): ValueAnimator {
    return if (collapse) {
        ValueAnimator.ofInt(collapseHeight, expandHeight).setDuration(Configuration.ANIMATION_DURATION)
    } else {
        ValueAnimator.ofInt(expandHeight, collapseHeight).setDuration(Configuration.ANIMATION_DURATION)
    }
}

/**
 * Counter background span
 * @param context Context
 * @param paddingMulti padding multiplier to properly center number 1 and 0 on the canvas
 */
fun Spannable.setBackgroundSpan(context: Context, paddingMulti: Float) {
    setSpan(
        BackgroundSpan(
            ContextCompat.getColor(context, R.color.black_color),
            ContextCompat.getColor(context, R.color.white_color),
            context.resources.getDimension(R.dimen.spannable_radius).toInt(),
            context.resources.getDimension(R.dimen.spannable_padding).toInt(),
            context.resources.getDimension(R.dimen.spannable_width).toInt(),
            paddingMulti
        ), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}
