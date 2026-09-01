package com.clmf.player.data.repository

import com.clmf.player.data.local.dao.CategoryDao
import com.clmf.player.data.local.dao.ChannelDao
import com.clmf.player.data.local.dao.MovieDao
import com.clmf.player.data.local.dao.SeriesDao
import com.clmf.player.data.local.entity.CategoryEntity
import com.clmf.player.data.local.entity.ChannelEntity
import com.clmf.player.data.local.entity.MovieEntity
import com.clmf.player.data.local.entity.SeriesEntity
import com.clmf.player.data.remote.m3u.M3uPlaylistFetcher
import com.clmf.player.domain.model.Category
import com.clmf.player.domain.model.Channel
import com.clmf.player.domain.model.ConnectionType
import com.clmf.player.domain.model.ContentType
import com.clmf.player.domain.model.Episode
import com.clmf.player.domain.model.Movie
import com.clmf.player.domain.model.Series
import com.clmf.player.domain.repository.ConnectionRepository
import com.clmf.player.domain.repository.ContentRepository
import com.clmf.player.domain.repository.IPTVProvider
import com.clmf.player.domain.repository.SearchResults
import com.clmf.player.utils.AppError
import com.clmf.player.utils.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val provider: IPTVProvider,
    private val connectionRepository: ConnectionRepository,
    private val categoryDao: CategoryDao,
    private val channelDao: ChannelDao,
    private val movieDao: MovieDao,
    private val seriesDao: SeriesDao,
    private val m3uPlaylistFetcher: M3uPlaylistFetcher
) : ContentRepository {

    override fun observeLiveCategories(): Flow<List<Category>> =
        categoryDao.observeByType(ContentType.LIVE.name).map { list -> list.map { it.toDomain() } }

    override fun observeLiveChannels(): Flow<List<Channel>> =
        channelDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeMovieCategories(): Flow<List<Category>> =
        categoryDao.observeByType(ContentType.MOVIE.name).map { list -> list.map { it.toDomain() } }

    override fun observeMovies(): Flow<List<Movie>> =
        movieDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeSeriesCategories(): Flow<List<Category>> =
        categoryDao.observeByType(ContentType.SERIES.name).map { list -> list.map { it.toDomain() } }

    override fun observeSeries(): Flow<List<Series>> =
        seriesDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getEpisodes(seriesId: String): AppResult<List<Episode>> {
        val connection = connectionRepository.getSelectedConnection()
            ?: return AppResult.Error(AppError.ServerUnavailable)
        return provider.getEpisodes(connection, seriesId)
    }

    override suspend fun refreshLiveTv(): AppResult<Unit> {
        val connection = connectionRepository.getSelectedConnection()
            ?: return AppResult.Error(AppError.ServerUnavailable)

        if (connection.type == ConnectionType.M3U) {
            return when (val result = m3uPlaylistFetcher.fetch(connection.playlistUrl)) {
                is AppResult.Success -> {
                    categoryDao.replaceType(ContentType.LIVE.name, result.data.categories.map { it.toEntity() })
                    channelDao.replaceAll(result.data.channels.map { it.toEntity() })
                    AppResult.Success(Unit)
                }
                is AppResult.Error -> result
            }
        }

        val categories = provider.getLiveCategories(connection)
        if (categories is AppResult.Error) return AppResult.Error(categories.error)
        val channels = provider.getLiveChannels(connection)
        if (channels is AppResult.Error) return AppResult.Error(channels.error)

        categoryDao.replaceType(ContentType.LIVE.name, (categories as AppResult.Success).data.map { it.toEntity() })
        channelDao.replaceAll((channels as AppResult.Success).data.map { it.toEntity() })
        return AppResult.Success(Unit)
    }

    override suspend fun refreshMovies(): AppResult<Unit> {
        val connection = connectionRepository.getSelectedConnection()
            ?: return AppResult.Error(AppError.ServerUnavailable)

        // M3U playlists don't reliably separate movies/series from live channels —
        // clear anything left over from a previous Xtream connection instead of
        // leaving it stranded and unreachable.
        if (connection.type == ConnectionType.M3U) {
            categoryDao.replaceType(ContentType.MOVIE.name, emptyList())
            movieDao.replaceAll(emptyList())
            return AppResult.Success(Unit)
        }

        val categories = provider.getMovieCategories(connection)
        if (categories is AppResult.Error) return AppResult.Error(categories.error)
        val movies = provider.getMovies(connection)
        if (movies is AppResult.Error) return AppResult.Error(movies.error)

        categoryDao.replaceType(ContentType.MOVIE.name, (categories as AppResult.Success).data.map { it.toEntity() })
        movieDao.replaceAll((movies as AppResult.Success).data.map { it.toEntity() })
        return AppResult.Success(Unit)
    }

    override suspend fun refreshSeries(): AppResult<Unit> {
        val connection = connectionRepository.getSelectedConnection()
            ?: return AppResult.Error(AppError.ServerUnavailable)

        if (connection.type == ConnectionType.M3U) {
            categoryDao.replaceType(ContentType.SERIES.name, emptyList())
            seriesDao.replaceAll(emptyList())
            return AppResult.Success(Unit)
        }

        val categories = provider.getSeriesCategories(connection)
        if (categories is AppResult.Error) return AppResult.Error(categories.error)
        val series = provider.getSeries(connection)
        if (series is AppResult.Error) return AppResult.Error(series.error)

        categoryDao.replaceType(ContentType.SERIES.name, (categories as AppResult.Success).data.map { it.toEntity() })
        seriesDao.replaceAll((series as AppResult.Success).data.map { it.toEntity() })
        return AppResult.Success(Unit)
    }

    override suspend fun refreshAll(): AppResult<Unit> {
        refreshLiveTv().let { if (it is AppResult.Error) return it }
        refreshMovies().let { if (it is AppResult.Error) return it }
        refreshSeries().let { if (it is AppResult.Error) return it }
        return AppResult.Success(Unit)
    }

    override suspend fun search(query: String): SearchResults {
        if (query.isBlank()) return SearchResults()
        return SearchResults(
            channels = channelDao.search(query).map { it.toDomain() },
            movies = movieDao.search(query).map { it.toDomain() },
            series = seriesDao.search(query).map { it.toDomain() }
        )
    }

    private fun CategoryEntity.toDomain() = Category(id = id, name = name, type = ContentType.valueOf(type))
    private fun Category.toEntity() = CategoryEntity(id = id, name = name, type = type.name)

    private fun ChannelEntity.toDomain() = Channel(
        id = id, name = name, logoUrl = logoUrl, categoryId = categoryId,
        streamUrl = streamUrl, epgChannelId = epgChannelId
    )
    private fun Channel.toEntity() = ChannelEntity(
        id = id, name = name, logoUrl = logoUrl, categoryId = categoryId,
        streamUrl = streamUrl, epgChannelId = epgChannelId
    )

    private fun MovieEntity.toDomain() = Movie(
        id = id, name = name, posterUrl = posterUrl, categoryId = categoryId, streamUrl = streamUrl,
        description = description, year = year, genre = genre, durationMinutes = durationMinutes,
        cast = cast, rating = rating
    )
    private fun Movie.toEntity() = MovieEntity(
        id = id, name = name, posterUrl = posterUrl, categoryId = categoryId, streamUrl = streamUrl,
        description = description, year = year, genre = genre, durationMinutes = durationMinutes,
        cast = cast, rating = rating
    )

    private fun SeriesEntity.toDomain() = Series(
        id = id, name = name, posterUrl = posterUrl, categoryId = categoryId,
        description = description, year = year, genre = genre, cast = cast, rating = rating
    )
    private fun Series.toEntity() = SeriesEntity(
        id = id, name = name, posterUrl = posterUrl, categoryId = categoryId,
        description = description, year = year, genre = genre, cast = cast, rating = rating
    )
}
