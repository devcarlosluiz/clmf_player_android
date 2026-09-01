package com.clmf.player.domain.repository

import com.clmf.player.domain.model.Category
import com.clmf.player.domain.model.Channel
import com.clmf.player.domain.model.Episode
import com.clmf.player.domain.model.Movie
import com.clmf.player.domain.model.Series
import com.clmf.player.utils.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first façade: exposes cached content instantly via Flow while
 * [refresh] pulls fresh data from the active [IPTVProvider] in the background.
 */
interface ContentRepository {
    fun observeLiveCategories(): Flow<List<Category>>
    fun observeLiveChannels(): Flow<List<Channel>>
    fun observeMovieCategories(): Flow<List<Category>>
    fun observeMovies(): Flow<List<Movie>>
    fun observeSeriesCategories(): Flow<List<Category>>
    fun observeSeries(): Flow<List<Series>>

    suspend fun getEpisodes(seriesId: String): AppResult<List<Episode>>

    suspend fun refreshLiveTv(): AppResult<Unit>
    suspend fun refreshMovies(): AppResult<Unit>
    suspend fun refreshSeries(): AppResult<Unit>
    suspend fun refreshAll(): AppResult<Unit>

    suspend fun search(query: String): SearchResults
}

data class SearchResults(
    val channels: List<Channel> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val series: List<Series> = emptyList()
)
