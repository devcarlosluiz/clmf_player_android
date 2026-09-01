package com.clmf.player.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clmf.player.domain.model.Channel
import com.clmf.player.domain.model.ContentType
import com.clmf.player.domain.model.HistoryItem
import com.clmf.player.domain.model.PlaybackRequest
import com.clmf.player.domain.repository.ContentRepository
import com.clmf.player.domain.repository.HistoryRepository
import com.clmf.player.player.PlaybackProgress
import com.clmf.player.player.PlayerManager
import com.clmf.player.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val playerManager: PlayerManager,
    private val historyRepository: HistoryRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerManager.playerState
    val progress: StateFlow<PlaybackProgress> = playerManager.progress

    private val _liveChannels = MutableStateFlow<List<Channel>>(emptyList())
    val liveChannels: StateFlow<List<Channel>> = _liveChannels.asStateFlow()

    private var currentRequest: PlaybackRequest? = null

    fun start(contentType: String, contentId: String, streamUrl: String, title: String) {
        viewModelScope.launch {
            val type = runCatching { ContentType.valueOf(contentType) }.getOrDefault(ContentType.MOVIE)
            val resumePosition = historyRepository.getProgress(contentId, type)?.positionMillis ?: 0L

            if (type == ContentType.LIVE) {
                _liveChannels.value = contentRepository.observeLiveChannels().first()
            }

            val request = PlaybackRequest(
                contentId = contentId,
                contentType = type,
                title = title,
                streamUrl = streamUrl,
                startPositionMillis = if (type == ContentType.LIVE) 0L else resumePosition
            )
            currentRequest = request
            // If this content is already playing (e.g. the user tapped a live
            // preview that was already streaming), avoid an unnecessary reconnect.
            if (playerManager.currentContentId != contentId) {
                playerManager.play(request)
            }
            trackProgress()
        }
    }

    fun playChannel(channel: Channel) {
        val request = PlaybackRequest(
            contentId = channel.id,
            contentType = ContentType.LIVE,
            title = channel.name,
            streamUrl = channel.streamUrl
        )
        currentRequest = request
        playerManager.play(request)
    }

    fun playNextChannel(forward: Boolean) {
        val channels = _liveChannels.value
        val currentId = currentRequest?.contentId ?: return
        val index = channels.indexOfFirst { it.id == currentId }
        if (index == -1 || channels.isEmpty()) return
        val nextIndex = ((index + if (forward) 1 else -1) + channels.size) % channels.size
        playChannel(channels[nextIndex])
    }

    fun togglePlayPause() = playerManager.togglePlayPause()
    fun seekTo(positionMillis: Long) = playerManager.seekTo(positionMillis)
    fun setPlaybackSpeed(speed: Float) = playerManager.setPlaybackSpeed(speed)
    fun retry() = playerManager.retryNow()

    private fun trackProgress() {
        viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                saveProgress()
            }
        }
    }

    fun saveProgress() {
        val request = currentRequest ?: return
        if (request.contentType == ContentType.LIVE) return
        val position = playerManager.currentPositionMillis()
        val duration = playerManager.currentDurationMillis()
        if (duration <= 0) return
        viewModelScope.launch {
            historyRepository.upsertProgress(
                HistoryItem(
                    contentId = request.contentId,
                    contentType = request.contentType,
                    name = request.title,
                    imageUrl = request.imageUrl,
                    positionMillis = position,
                    durationMillis = duration,
                    watchedAtMillis = System.currentTimeMillis(),
                    streamUrl = request.streamUrl
                )
            )
        }
    }

    override fun onCleared() {
        saveProgress()
        super.onCleared()
    }
}
