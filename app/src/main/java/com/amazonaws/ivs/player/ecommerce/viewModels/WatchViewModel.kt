package com.amazonaws.ivs.player.ecommerce.viewModels

import android.content.Context
import android.net.Uri
import android.view.Surface
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amazonaws.ivs.player.MediaPlayer
import com.amazonaws.ivs.player.MediaType
import com.amazonaws.ivs.player.Player
import com.amazonaws.ivs.player.ecommerce.common.ConsumableLiveData
import com.amazonaws.ivs.player.ecommerce.common.asObject
import com.amazonaws.ivs.player.ecommerce.common.setListener
import com.amazonaws.ivs.player.ecommerce.models.MetadataModel
import com.amazonaws.ivs.player.ecommerce.models.ProductModel
import timber.log.Timber

class WatchViewModel : ViewModel() {

    private var player: MediaPlayer? = null
    private var playerListener: Player.Listener? = null

    var url: String? = null
    val buffering = MutableLiveData<Boolean>()
    val playerParamsChanged = MutableLiveData<Pair<Int, Int>>()
    val errorHappened = ConsumableLiveData<Pair<String, String>>()
    val products = MutableLiveData<List<ProductModel>>()
    val showControls = MutableLiveData<Boolean>()
    val enableAutoScroll = MutableLiveData<Boolean>()
    val isPlaying = MutableLiveData<Boolean>()
    val isLandscape = MutableLiveData<Boolean>()

    fun initPlayer(context: Context) {
        showControls.value = true
        // Media player initialization
        player = MediaPlayer(context)

        player?.setListener(
            onVideoSizeChanged = { width, height ->
                Timber.d("Video size changed: $width $height")
                isLandscape.value = width >= height
                playerParamsChanged.value = Pair(width, height)
            },
            onStateChanged = { state ->
                Timber.d("State changed: $state")
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
                        products.value = String(buffer.array(), Charsets.UTF_8)
                            .asObject<MetadataModel>()
                            .products
                    } catch (exception: Exception) {
                        Timber.d("Error happened: $exception")
                    }
                }
            },
            onError = { exception ->
                Timber.d("Error happened: $exception")
                errorHappened.postConsumable(Pair(exception.code.toString(), exception.errorMessage))
            }
        )
    }

    private fun playerLoadStream(uri: Uri) {
        Timber.d("Loading stream URI: $uri")
        // Loads the specified stream
        player?.load(uri)
        player?.play()
    }

    fun playerStart(surface: Surface) {
        Timber.d("Starting player")
        updateSurface(surface)
        playerLoadStream(Uri.parse(url))
        play()
    }

    fun updateSurface(surface: Surface?) {
        Timber.d("Updating player surface: $surface")
        // Sets the Surface to use for rendering video
        player?.setSurface(surface)
    }

    fun play() {
        Timber.d("Starting playback")
        // Starts or resumes playback of the stream.
        player?.play()
    }

    fun pause() {
        Timber.d("Pausing playback")
        // Pauses playback of the stream.
        player?.pause()
    }

    fun release() {
        Timber.d("Releasing player")
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
