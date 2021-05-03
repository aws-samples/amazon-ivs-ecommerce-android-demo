package com.amazonaws.ivs.player.ecommerce.common

import com.amazonaws.ivs.player.Cue
import com.amazonaws.ivs.player.Player
import com.amazonaws.ivs.player.PlayerException
import com.amazonaws.ivs.player.Quality
import java.nio.ByteBuffer

/**
 * Player listener extension function
 */
inline fun Player.setListener(
    crossinline onAnalyticsEvent: (key: String, value: String) -> Unit = { _,_ -> },
    crossinline onRebuffering: () -> Unit = {},
    crossinline onSeekCompleted: (value: Long) -> Unit = { _ -> },
    crossinline onQualityChanged: (quality: Quality) -> Unit = { _ -> },
    crossinline onVideoSizeChanged: (width: Int, height: Int) -> Unit = { _,_ -> },
    crossinline onCue: (cue: Cue) -> Unit = { _ -> },
    crossinline onDurationChanged: (duration: Long) -> Unit = { _ -> },
    crossinline onStateChanged: (state: Player.State) -> Unit = { _ -> },
    crossinline onError: (exception: PlayerException) -> Unit = { _ -> },
    crossinline onMetadata: (data: String, buffer: ByteBuffer) -> Unit = { _, _ -> }
): Player.Listener {
    val listener = playerListener(
        onAnalyticsEvent, onRebuffering, onSeekCompleted, onQualityChanged, onVideoSizeChanged,
        onCue, onDurationChanged, onStateChanged, onError, onMetadata
    )

    addListener(listener)
    return listener
}

inline fun playerListener(
    crossinline onAnalyticsEvent: (key: String, value: String) -> Unit = { _,_ -> },
    crossinline onRebuffering: () -> Unit = {},
    crossinline onSeekCompleted: (value: Long) -> Unit = { _ -> },
    crossinline onQualityChanged: (quality: Quality) -> Unit = { _ -> },
    crossinline onVideoSizeChanged: (width: Int, height: Int) -> Unit = { _,_ -> },
    crossinline onCue: (cue: Cue) -> Unit = { _ -> },
    crossinline onDurationChanged: (duration: Long) -> Unit = { _ -> },
    crossinline onStateChanged: (state: Player.State) -> Unit = { _ -> },
    crossinline onError: (exception: PlayerException) -> Unit = { _ -> },
    crossinline onMetadata: (data: String, buffer: ByteBuffer) -> Unit = { _, _ -> }
): Player.Listener = object : Player.Listener() {
    // Indicates that a video analytics tracking event occurred.
    override fun onAnalyticsEvent(key: String, value: String) = onAnalyticsEvent(key, value)
    // Indicates that the player is buffering from a previous PLAYING state.
    override fun onRebuffering() = onRebuffering()
    // Indicates that the player has seeked to a given position as requested from seekTo(long).
    override fun onSeekCompleted(value: Long) = onSeekCompleted(value)
    // Indicates that the playing quality changed either from a user action or from an internal adaptive quality switch.
    override fun onQualityChanged(quality: Quality) = onQualityChanged(quality)
    // Indicates that the video dimensions changed.
    override fun onVideoSizeChanged(width: Int, height: Int) = onVideoSizeChanged(width, height)
    // Indicates that a timed cue was received.
    override fun onCue(cue: Cue) = onCue(cue)
    // Indicates that source duration changed
    override fun onDurationChanged(duration: Long) = onDurationChanged(duration)
    // Indicates that the player state changed.
    override fun onStateChanged(state: Player.State) = onStateChanged(state)
    // Indicates that an error occurred.
    override fun onError(exception: PlayerException) = onError(exception)
    // Indicates that a metadata event occurred.
    override fun onMetadata(data: String, buffer: ByteBuffer) = onMetadata(data, buffer)
}
