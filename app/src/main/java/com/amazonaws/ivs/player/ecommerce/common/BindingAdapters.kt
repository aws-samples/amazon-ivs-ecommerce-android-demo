package com.amazonaws.ivs.player.ecommerce.common

import android.graphics.Paint
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.amazonaws.ivs.player.ecommerce.BuildConfig
import com.amazonaws.ivs.player.ecommerce.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

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

    @BindingAdapter("loadImage")
    @JvmStatic
    fun loadImage(view: ImageView, url: String?) {
        if (url == null) return
        val radius = view.context.resources.getDimensionPixelSize(R.dimen.radius_normal)
        Glide.with(view.context)
            .load(BuildConfig.BASE_IMAGE_URL + url)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(view)
    }

    @BindingAdapter("loadLogo")
    @JvmStatic
    fun loadLogo(view: ImageView, url: String) {
        Glide.with(view.context).load(url).circleCrop().into(view)
    }
}
