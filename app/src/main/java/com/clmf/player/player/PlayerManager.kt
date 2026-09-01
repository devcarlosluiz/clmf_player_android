package com.clmf.player.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.clmf.player.domain.model.PlaybackRequest
import com.clmf.player.utils.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps a single [ExoPlayer] instance and exposes playback state as [StateFlow]s,
 * decoupling the UI layer from Media3 entirely. Implements the retry policy:
 * 3 attempts with 1s / 2s / 5s backoff before surfacing a terminal [PlayerState.Error].
 */
@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    private val retryDelaysMillis = listOf(1_000L, 2_000L, 5_000L)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var retryJob: Job? = null
    private var pendingRequest: PlaybackRequest? = null

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _progress = MutableStateFlow(PlaybackProgress())
    val progress: StateFlow<PlaybackProgress> = _progress.asStateFlow()

    val exoPlayer: ExoPlayer by lazy { buildPlayer() }

    private fun buildPlayer(): ExoPlayer {
        val dataSourceFactory = DefaultDataSource.Factory(
            context,
            OkHttpDataSource.Factory(okHttpClient)
        )
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(15_000, 50_000, 2_500, 5_000)
            .build()

        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> _playerState.update { PlayerState.Buffering }
                            Player.STATE_READY -> {
                                retryJob?.cancel()
                                _playerState.update { if (playWhenReady) PlayerState.Playing else PlayerState.Paused }
                            }
                            Player.STATE_ENDED -> _playerState.update { PlayerState.Ended }
                            Player.STATE_IDLE -> Unit
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) _playerState.update { PlayerState.Playing }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        AppLogger.error("Player error: ${error.errorCodeName}", error)
                        attemptRetry(error)
                    }
                })
                startProgressTracking()
            }
    }

    val currentContentId: String?
        get() = pendingRequest?.contentId

    fun play(request: PlaybackRequest) {
        pendingRequest = request
        retryJob?.cancel()
        _playerState.update { PlayerState.Loading }
        val mediaItem = MediaItem.Builder()
            .setUri(request.streamUrl)
            .setMediaId(request.contentId)
            .build()
        exoPlayer.setMediaItem(mediaItem, request.startPositionMillis)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    /** Stops playback without releasing the underlying ExoPlayer instance. */
    fun stop() {
        retryJob?.cancel()
        pendingRequest = null
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _playerState.update { PlayerState.Idle }
    }

    fun togglePlayPause() {
        exoPlayer.playWhenReady = !exoPlayer.playWhenReady
    }

    fun seekTo(positionMillis: Long) {
        exoPlayer.seekTo(positionMillis)
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
    }

    fun retryNow() {
        pendingRequest?.let { play(it) }
    }

    fun currentPositionMillis(): Long = exoPlayer.currentPosition
    fun currentDurationMillis(): Long = exoPlayer.duration.coerceAtLeast(0)

    private fun attemptRetry(error: PlaybackException) {
        val request = pendingRequest
        if (request == null) {
            _playerState.update { PlayerState.Error(friendlyMessage(error)) }
            return
        }
        retryJob?.cancel()
        retryJob = scope.launch {
            for ((index, delayMs) in retryDelaysMillis.withIndex()) {
                if (!isActive) return@launch
                val attempt = index + 1
                _playerState.update { PlayerState.Retrying(attempt, retryDelaysMillis.size) }
                delay(delayMs)
                if (!isActive) return@launch
                val mediaItem = MediaItem.Builder()
                    .setUri(request.streamUrl)
                    .setMediaId(request.contentId)
                    .build()
                exoPlayer.setMediaItem(mediaItem, exoPlayer.currentPosition)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                // Give the player a moment to report success/failure before the next retry.
                delay(1_500)
                if (exoPlayer.playbackState == Player.STATE_READY || exoPlayer.isPlaying) {
                    return@launch
                }
            }
            _playerState.update { PlayerState.Error(friendlyMessage(error)) }
        }
    }

    private fun friendlyMessage(error: PlaybackException): String =
        "Não foi possível reproduzir este conteúdo."

    private fun startProgressTracking() {
        scope.launch {
            while (isActive) {
                _progress.update {
                    PlaybackProgress(
                        positionMillis = exoPlayer.currentPosition,
                        durationMillis = exoPlayer.duration.coerceAtLeast(0),
                        bufferedPercentage = exoPlayer.bufferedPercentage
                    )
                }
                delay(500)
            }
        }
    }

    fun release() {
        retryJob?.cancel()
        exoPlayer.release()
    }
}
