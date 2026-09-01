package com.clmf.player.presentation.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clmf.player.domain.model.Category
import com.clmf.player.domain.model.ContentType
import com.clmf.player.domain.model.Episode
import com.clmf.player.domain.model.FavoriteItem
import com.clmf.player.domain.model.Series
import com.clmf.player.domain.repository.ContentRepository
import com.clmf.player.domain.repository.FavoritesRepository
import com.clmf.player.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeriesUiState(
    val categories: List<Category> = emptyList(),
    val series: List<Series> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<String?>(null)
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<SeriesUiState> = combine(
        contentRepository.observeSeriesCategories(),
        contentRepository.observeSeries(),
        selectedCategory,
        searchQuery
    ) { categories, series, category, query ->
        SeriesUiState(
            categories = categories,
            series = series
                .filter { category == null || it.categoryId == category }
                .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) },
            selectedCategoryId = category,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeriesUiState())

    fun selectCategory(categoryId: String?) { selectedCategory.value = categoryId }
    fun onSearchQueryChange(query: String) { searchQuery.value = query }
    fun refresh() { viewModelScope.launch { contentRepository.refreshSeries() } }
}

data class SeriesDetailUiState(
    val series: Series? = null,
    val seasons: List<Int> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesDetailUiState())
    val uiState: StateFlow<SeriesDetailUiState> = _uiState

    fun load(seriesId: String) {
        viewModelScope.launch {
            contentRepository.observeSeries().collect { list ->
                val series = list.find { it.id == seriesId } ?: return@collect
                val isFavorite = favoritesRepository.isFavorite(seriesId, ContentType.SERIES)
                val episodesResult = contentRepository.getEpisodes(seriesId)
                val seasons = (episodesResult as? AppResult.Success)?.data
                    ?.map { it.seasonNumber }?.distinct()?.sorted().orEmpty()
                _uiState.value = SeriesDetailUiState(
                    series = series,
                    seasons = seasons,
                    isFavorite = isFavorite,
                    isLoading = false,
                    errorMessage = (episodesResult as? AppResult.Error)?.error?.friendlyMessage
                )
            }
        }
    }

    fun toggleFavorite() {
        val series = _uiState.value.series ?: return
        viewModelScope.launch {
            val nowFavorite = favoritesRepository.toggleFavorite(
                FavoriteItem(
                    contentId = series.id,
                    contentType = ContentType.SERIES,
                    name = series.name,
                    imageUrl = series.posterUrl,
                    addedAtMillis = System.currentTimeMillis()
                )
            )
            _uiState.value = _uiState.value.copy(isFavorite = nowFavorite)
        }
    }
}

data class EpisodesUiState(
    val episodes: List<Episode> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpisodesUiState())
    val uiState: StateFlow<EpisodesUiState> = _uiState

    fun load(seriesId: String, seasonNumber: Int) {
        viewModelScope.launch {
            when (val result = contentRepository.getEpisodes(seriesId)) {
                is AppResult.Success -> _uiState.value = EpisodesUiState(
                    episodes = result.data.filter { it.seasonNumber == seasonNumber },
                    isLoading = false
                )
                is AppResult.Error -> _uiState.value = EpisodesUiState(
                    isLoading = false,
                    errorMessage = result.error.friendlyMessage
                )
            }
        }
    }
}
