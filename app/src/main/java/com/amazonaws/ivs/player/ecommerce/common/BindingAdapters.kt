package com.amazonaws.ivs.player.ecommerce.common

import android.graphics.Paint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object BindingAdapters {

    @BindingAdapter("strikeThrough")
    @JvmStatic
    fun strikeThrough(view: TextView, show: Boolean) {
        view.paintFlags = if (show) {
            view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    @BindingAdapter("changeVisibility")
    @JvmStatic
    fun setChangeVisibility(view: View, fadeIn: Boolean) {
        if (fadeIn) {
            view.fadeIn()
        } else {
            view.fadeOut()
        }
    }

    @BindingAdapter("fadeAnimation")
    @JvmStatic
    fun setFadeAnimation(view: View, fadeOut: Boolean) {
        if (fadeOut) {
            view.visibility = View.VISIBLE
            view.fadeOut()
        } else {
            view.visibility = View.INVISIBLE
            view.fadeIn()
        }
    }

    @BindingAdapter("changeHeight")
    @JvmStatic
    fun setViewHeight(view: View, value: Boolean) {
        if (value) {
            view.animatePlaceholderHeight(true)
        } else {
            view.animatePlaceholderHeight(false)
        }
    }

    @BindingAdapter("loadImage")
    @JvmStatic
    fun setImage(view: ImageView, url: String) {
        Glide.with(view.context).load(url).into(view)
    }
}
