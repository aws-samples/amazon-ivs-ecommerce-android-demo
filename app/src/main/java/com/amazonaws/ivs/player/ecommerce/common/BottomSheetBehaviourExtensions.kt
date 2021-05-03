package com.amazonaws.ivs.player.ecommerce.common

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

fun <T : View> BottomSheetBehavior<T>.isOpened() =
    state == BottomSheetBehavior.STATE_EXPANDED || state == BottomSheetBehavior.STATE_HALF_EXPANDED

fun <T : View> BottomSheetBehavior<T>.open() {
    if (!isOpened()) {
        state = BottomSheetBehavior.STATE_EXPANDED
    }
}

fun <T : View> BottomSheetBehavior<T>.hide() {
    if (isOpened()) {
        state = BottomSheetBehavior.STATE_HIDDEN
    }
}
