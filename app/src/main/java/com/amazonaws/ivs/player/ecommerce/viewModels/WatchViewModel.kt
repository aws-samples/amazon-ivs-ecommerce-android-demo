package com.amazonaws.ivs.player.ecommerce.viewModels

import android.app.Application
import android.net.Uri
import android.util.Log
import android.view.Surface
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amazonaws.ivs.player.MediaPlayer
import com.amazonaws.ivs.player.MediaType
import com.amazonaws.ivs.player.Player
import com.amazonaws.ivs.player.ecommerce.common.Configuration.TAG
import com.amazonaws.ivs.player.ecommerce.common.setListener
import com.amazonaws.ivs.player.ecommerce.models.MetadataModel
import com.amazonaws.ivs.player.ecommerce.models.ProductModel
import com.google.gson.Gson

class WatchViewModel(private val context: Application) : ViewModel() {

    private var player: MediaPlayer? = null
    private var playerListener: Player.Listener? = null

    val url = MutableLiveData<String>()
    val buffering = MutableLiveData<Boolean>()
    val playerParamsChanged = MutableLiveData<Pair<Int, Int>>()
    val errorHappened = MutableLiveData<Pair<String, String>>()
    val products = MutableLiveData<List<ProductModel>>()
    val showControls = MutableLiveData<Boolean>()
    val enableAutoScroll = MutableLiveData<Boolean>()
    val isPlaying = MutableLiveData<Boolean>()
    val isLandscape = MutableLiveData<Boolean>()

    init {
        showControls.value = true
        initPlayer()
    }

    private fun initPlayer() {
        // Media player initialization
        player = MediaPlayer(context)

        player?.setListener(
            onVideoSizeChanged = { width, height ->
                Log.d(TAG, "Video size changed: $width $height")
                isLandscape.value = width >= height
                playerParamsChanged.value = Pair(width, height)
            },
            onStateChanged = { state ->
                Log.d(TAG, "State changed: $state")
                when (state) {
                    Player.State.BUFFERING -> {
                        buffering.value = true
                    }
                    else -> {
                        buffering.value = false
                        isPlaying.value = false
                        if (state == Player.State.PLAYING) {
                            isPlaying.value = true
                        }
                    }
                }
            },
            onMetadata = { data, buffer ->
                if (MediaType.TEXT_PLAIN == data) {
                    try {
                        // Get question data item from buffer
                        val metadataModel = Gson().fromJson(
                            String(buffer.array(), Charsets.UTF_8),
                            MetadataModel::class.java
                        )
                        products.value = metadataModel.products
                        Log.d(TAG, "Received product data: $metadataModel")
                    } catch (exception: Exception) {
                        Log.d(TAG, "Error happened: $exception")
                    }
                }
            },
            onError = { exception ->
                Log.d(TAG, "Error happened: $exception")
                errorHappened.value = Pair(exception.code.toString(), exception.errorMessage)
            }
        )
    }

    private fun playerLoadStream(uri: Uri) {
        Log.d(TAG, "Loading stream URI: $uri")
        // Loads the specified stream
        player?.load(uri)
        player?.play()
    }

    fun playerStart(surface: Surface) {
        Log.d(TAG, "Starting player")
        updateSurface(surface)
        playerLoadStream(Uri.parse(url.value))
        play()
    }

    fun updateSurface(surface: Surface?) {
        Log.d(TAG, "Updating player surface: $surface")
        // Sets the Surface to use for rendering video
        player?.setSurface(surface)
    }

    fun play() {
        Log.d(TAG, "Starting playback")
        // Starts or resumes playback of the stream.
        player?.play()
    }

    fun pause() {
        Log.d(TAG, "Pausing playback")
        // Pauses playback of the stream.
        player?.pause()
    }

    fun release() {
        Log.d(TAG, "Releasing player")
        // Removes a playback state listener
        playerListener?.let { player?.removeListener(it) }
        // Releases the player instance
        player?.release()
        player = null
    }

    fun showControls() {
        showControls.value = showControls.value != true
    }

}
