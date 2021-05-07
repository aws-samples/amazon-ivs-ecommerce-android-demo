package com.amazonaws.ivs.player.ecommerce.common

import com.amazonaws.ivs.player.ecommerce.R
import com.amazonaws.ivs.player.ecommerce.models.LiveStreamItem

object Configuration {
    private const val LINK_PORTRAIT = "https://4da4a22026d3.us-west-2.playback.live-video.net/api/video/v1/us-west-2.298083573632.channel.WbhDQYgfYHoT.m3u8"
    private const val LINK_LANDSCAPE = "https://4da4a22026d3.us-west-2.playback.live-video.net/api/video/v1/us-west-2.298083573632.channel.JQj8mTBfhb7e.m3u8"
    const val BASE_URL = "https://assets.codepen.io/2960061" // Base URL prepended to images sent over metadata
    const val EXTRA_LINK = "extra_link"

    // Carousel items
    private val stream1 = LiveStreamItem(R.drawable.live_carousel_image_1, R.drawable.avatar_1, LINK_PORTRAIT)
    private val stream2 = LiveStreamItem(R.drawable.live_carousel_image_2, R.drawable.avatar_2, LINK_LANDSCAPE)
    private val stream3 = LiveStreamItem(R.drawable.live_carousel_image_3, R.drawable.avatar_3, LINK_PORTRAIT)
    private val stream4 = LiveStreamItem(R.drawable.live_carousel_image_4, R.drawable.avatar_4, LINK_PORTRAIT)
    val streamList = listOf(stream1, stream2, stream3, stream4)

    const val ANIMATION_DURATION = 200L
    const val MILLIS_DAY = 86400000

    const val BLUR_RADIUS = 15F // Landscape video background blur (0-25)
    const val INTERACTION = 5000L // 5s - Delay before auto-scrolling to On Stream item
    const val BACKGROUND_DELAY = 100L
}
