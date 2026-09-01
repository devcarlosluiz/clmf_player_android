package com.clmf.player.presentation.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clmf.player.domain.model.ContentType
import com.clmf.player.domain.model.FavoriteItem
import com.clmf.player.domain.model.Movie
import com.clmf.player.domain.repository.ContentRepository
import com.clmf.player.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoviesUiState(
    val categories: List<com.clmf.player.domain.model.Category> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<String?>(null)
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<MoviesUiState> = combine(
        contentRepository.observeMovieCategories(),
        contentRepository.observeMovies(),
        selectedCategory,
        searchQuery
    ) { categories, movies, category, query ->
        MoviesUiState(
            categories = categories,
            movies = movies
                .filter { category == null || it.categoryId == category }
                .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) },
            selectedCategoryId = category,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MoviesUiState())

    fun selectCategory(categoryId: String?) { selectedCategory.value = categoryId }
    fun onSearchQueryChange(query: String) { searchQuery.value = query }
    fun refresh() { viewModelScope.launch { contentRepository.refreshMovies() } }
}

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    fun load(movieId: String) {
        viewModelScope.launch {
            contentRepository.observeMovies().collect { movies ->
                _movie.value = movies.find { it.id == movieId }
                _isFavorite.value = favoritesRepository.isFavorite(movieId, ContentType.MOVIE)
            }
        }
    }

    fun toggleFavorite() {
        val movie = _movie.value ?: return
        viewModelScope.launch {
            val nowFavorite = favoritesRepository.toggleFavorite(
                FavoriteItem(
                    contentId = movie.id,
                    contentType = ContentType.MOVIE,
                    name = movie.name,
                    imageUrl = movie.posterUrl,
                    addedAtMillis = System.currentTimeMillis()
                )
            )
            _isFavorite.value = nowFavorite
        }
    }
}
