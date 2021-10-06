package com.amazonaws.ivs.player.ecommerce.ui.viewmodels

import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.lifecycle.ViewModel
import com.amazonaws.ivs.player.MediaPlayer
import com.amazonaws.ivs.player.MediaType
import com.amazonaws.ivs.player.Player
import com.amazonaws.ivs.player.ecommerce.BuildConfig
import com.amazonaws.ivs.player.ecommerce.common.*
import com.amazonaws.ivs.player.ecommerce.models.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber

class MainViewModel(products: ProductsModel) : ViewModel() {

    private var player: MediaPlayer? = null
    private var playerListener: Player.Listener? = null

    private val rawProducts = mutableListOf<ProductModel>()
    private val metadata = mutableListOf<String>()
    private val _onError = MutableSharedFlow<ErrorModel>(replay = 1)
    private val _onSizeChanged = MutableSharedFlow<SizeModel>(replay = 1)
    private val _onLoading = MutableSharedFlow<Boolean>(replay = 1)
    private val _products = MutableSharedFlow<List<ProductModel>>(replay = 1)

    val onError: SharedFlow<ErrorModel> = _onError
    val onSizeChanged: SharedFlow<SizeModel> = _onSizeChanged
    val onLoading: SharedFlow<Boolean> = _onLoading
    val products: SharedFlow<List<ProductModel>> = _products

    val playerSize get() = _onSizeChanged.replayCache.lastOrNull()
    val isShowingProduct get() = _products.replayCache.lastOrNull()?.any { it.isSelected } ?: false

    init {
        rawProducts.addAll(products.products)
        selectProduct()
    }

    fun initPlayer(textureView: TextureView) {
        _onLoading.tryEmit(true)
        player = MediaPlayer(textureView.context)
        player?.setListener(
            onVideoSizeChanged = { width, height ->
                Timber.d("Video size changed: $width $height")
                _onSizeChanged.tryEmit(SizeModel(width, height))
            },
            onStateChanged = { state ->
                Timber.d("State changed: $state")
                _onLoading.tryEmit(state != Player.State.PLAYING)
            },
            onMetadata = { data, buffer ->
                if (MediaType.TEXT_PLAIN == data) {
                    try {
                        val productId = String(buffer.array(), Charsets.UTF_8).asObject<MetadataModel>().productId
                        if (metadata.lastOrNull() != productId) {
                            metadata.add(productId)
                        }
                        selectProduct()
                    } catch (e: Exception) {
                        Timber.d(e, "Failed to parse metadata")
                    }
                }
            },
            onError = { exception ->
                Timber.d("Error happened: $exception")
                _onError.tryEmit(ErrorModel(exception.code, exception.errorMessage))
            }
        )
        player?.setSurface(Surface(textureView.surfaceTexture))
        player?.load(Uri.parse(BuildConfig.BASE_STREAM_URL))
        player?.play()
    }

    fun play() {
        Timber.d("Starting playback")
        player?.play()
    }

    fun pause() {
        Timber.d("Pausing playback")
        player?.pause()
    }

    fun release() {
        Timber.d("Releasing player")
        playerListener?.let { player?.removeListener(it) }
        player?.release()
        player = null
    }

    private fun selectProduct() {
        if (rawProducts.any { it.isSelected }) return
        metadata.removeFirstOrNull()?.let { productId ->
            updateTimer(productId)
        }
    }

    private fun updateTimer(productId: String) {
        rawProducts.first { it.id == productId }.let { product ->
            product.timeLeft = METADATA_TIMEOUT
            _products.tryEmit(rawProducts.map { it.copy() })
            Timber.d("Counting down time for: $product")
            product.timeLeft.onTick(
                onTick = { timeLeft ->
                    product.timeLeft = timeLeft
                    _products.tryEmit(rawProducts.map { it.copy() })
                }, onFinished = {
                    Timber.d("Product time ended: $product")
                    selectProduct()
                }
            )
        }
    }
}
