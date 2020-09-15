package com.amazonaws.ivs.player.ecommerce.common

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.amazonaws.ivs.player.ecommerce.R
import com.amazonaws.ivs.player.ecommerce.common.spans.BackgroundSpan
import kotlinx.android.synthetic.main.view_dialog.*
import kotlinx.coroutines.*

private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

fun launchMain(block: suspend CoroutineScope.() -> Unit) = mainScope.launch(
    context = CoroutineExceptionHandler { _, e -> Log.d(Configuration.TAG, "Coroutine failed ${e.localizedMessage}") },
    block = block
)

fun Activity.showDialog(title: String, message: String) {
    val dialog = Dialog(this).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(R.layout.view_dialog)
    }
    dialog.title.text = getString(R.string.error_happened_template, title)
    dialog.message.text = message
    dialog.dismiss_btn.setOnClickListener {
        dialog.dismiss()
    }
    dialog.show()
}

fun Activity.hideKeyboard() {
    val view = currentFocus ?: window.decorView
    val token = view.windowToken
    view.clearFocus()
    ContextCompat.getSystemService(this, InputMethodManager::class.java)?.hideSoftInputFromWindow(token, 0)
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
 * Counter background span
 * @param context Context
 * @param paddingMulti padding multiplier to properly center number 1 and 0 on the canvas
 */
fun Spannable.setBackgroundSpan(context: Context, paddingMulti: Float) {
    setSpan(
        BackgroundSpan(
            ContextCompat.getColor(context, R.color.colorBlack),
            ContextCompat.getColor(context, R.color.colorWhite),
            context.resources.getDimension(R.dimen.spannable_radius).toInt(),
            context.resources.getDimension(R.dimen.spannable_padding).toInt(),
            context.resources.getDimension(R.dimen.spannable_width).toInt(),
            paddingMulti
        ), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}
