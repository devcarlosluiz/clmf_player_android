package com.clmf.player.domain.repository

import com.clmf.player.domain.model.AccountInfo
import com.clmf.player.domain.model.Category
import com.clmf.player.domain.model.Channel
import com.clmf.player.domain.model.Connection
import com.clmf.player.domain.model.Episode
import com.clmf.player.domain.model.EpgProgram
import com.clmf.player.domain.model.Movie
import com.clmf.player.domain.model.Series
import com.clmf.player.utils.AppResult

/**
 * Abstraction over an IPTV backend. Xtream Codes is the first implementation;
 * an M3U-based provider can implement the same contract in the future.
 */
interface IPTVProvider {
    suspend fun testConnection(connection: Connection): AppResult<AccountInfo>
    suspend fun getAccountInfo(connection: Connection): AppResult<AccountInfo>
    suspend fun getLiveCategories(connection: Connection): AppResult<List<Category>>
    suspend fun getLiveChannels(connection: Connection, categoryId: String? = null): AppResult<List<Channel>>
    suspend fun getMovieCategories(connection: Connection): AppResult<List<Category>>
    suspend fun getMovies(connection: Connection, categoryId: String? = null): AppResult<List<Movie>>
    suspend fun getSeriesCategories(connection: Connection): AppResult<List<Category>>
    suspend fun getSeries(connection: Connection, categoryId: String? = null): AppResult<List<Series>>
    suspend fun getEpisodes(connection: Connection, seriesId: String): AppResult<List<Episode>>
    suspend fun getShortEpg(connection: Connection, channelId: String): AppResult<List<EpgProgram>>
    fun buildLiveStreamUrl(connection: Connection, channelId: String, extension: String = "m3u8"): String
    fun buildMovieStreamUrl(connection: Connection, movieId: String, extension: String): String
}
