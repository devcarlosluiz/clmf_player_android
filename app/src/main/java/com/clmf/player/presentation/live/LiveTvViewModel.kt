package com.clmf.player.presentation.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clmf.player.domain.model.Category
import com.clmf.player.domain.model.Channel
import com.clmf.player.domain.model.ConnectionType
import com.clmf.player.domain.model.ContentType
import com.clmf.player.domain.model.EpgProgram
import com.clmf.player.domain.model.FavoriteItem
import com.clmf.player.domain.repository.ConnectionRepository
import com.clmf.player.domain.repository.ContentRepository
import com.clmf.player.domain.repository.FavoritesRepository
import com.clmf.player.domain.repository.IPTVProvider
import com.clmf.player.domain.model.PlaybackRequest
import com.clmf.player.player.PlayerManager
import com.clmf.player.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FAVORITES_CATEGORY_ID = "__favorites__"

data class LiveTvUiState(
    val categories: List<Category> = emptyList(),
    val channels: List<Channel> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val favoritesRepository: FavoritesRepository,
    private val connectionRepository: ConnectionRepository,
    private val iptvProvider: IPTVProvider,
    val playerManager: PlayerManager
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<String?>(null)
    private val searchQuery = MutableStateFlow("")

    private val _selectedChannel = MutableStateFlow<Channel?>(null)
    val selectedChannel: StateFlow<Channel?> = _selectedChannel.asStateFlow()

    private val _epgPrograms = MutableStateFlow<List<EpgProgram>>(emptyList())
    val epgPrograms: StateFlow<List<EpgProgram>> = _epgPrograms.asStateFlow()

    val uiState: StateFlow<LiveTvUiState> = combine(
        contentRepository.observeLiveCategories(),
        contentRepository.observeLiveChannels(),
        favoritesRepository.observeFavorites(ContentType.LIVE),
        selectedCategory,
        searchQuery
    ) { categories, channels, favorites, category, query ->
        val favoriteIds = favorites.map { it.contentId }.toSet()

        val filtered = channels
            .filter { category == null || category == FAVORITES_CATEGORY_ID || it.categoryId == category }
            .filter { category != FAVORITES_CATEGORY_ID || it.id in favoriteIds }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .map { it.copy(isFavorite = it.id in favoriteIds) }

        LiveTvUiState(
            categories = categories,
            channels = filtered,
            selectedCategoryId = category,
            searchQuery = query,
            favoriteIds = favoriteIds,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LiveTvUiState())

    fun selectCategory(categoryId: String?) {
        selectedCategory.value = categoryId
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(
                FavoriteItem(
                    contentId = channel.id,
                    contentType = ContentType.LIVE,
                    name = channel.name,
                    imageUrl = channel.logoUrl,
                    addedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun refresh() {
        viewModelScope.launch { contentRepository.refreshLiveTv() }
    }

    fun selectChannel(channel: Channel) {
        _selectedChannel.value = channel
        _epgPrograms.value = emptyList()

        if (playerManager.currentContentId != channel.id) {
            playerManager.play(
                PlaybackRequest(
                    contentId = channel.id,
                    contentType = ContentType.LIVE,
                    title = channel.name,
                    streamUrl = channel.streamUrl
                )
            )
        }

        viewModelScope.launch {
            val connection = connectionRepository.getSelectedConnection() ?: return@launch
            // M3U playlists have no EPG endpoint to call.
            if (connection.type != ConnectionType.XTREAM) return@launch
            val result = iptvProvider.getShortEpg(connection, channel.id)
            if (result is AppResult.Success) {
                _epgPrograms.value = result.data
            }
        }
    }

    /** Called when leaving the Live TV screen so the preview doesn't keep streaming in the background. */
    fun stopPreview() {
        playerManager.stop()
    }

    companion object {
        const val FAVORITES_ID = FAVORITES_CATEGORY_ID
    }
}
