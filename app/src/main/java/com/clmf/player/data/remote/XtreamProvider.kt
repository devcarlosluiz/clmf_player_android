package com.clmf.player.data.remote

import com.clmf.player.data.remote.api.XtreamApi
import com.clmf.player.data.remote.dto.AccountInfoDto
import com.clmf.player.data.remote.dto.CategoryDto
import com.clmf.player.data.remote.dto.ChannelDto
import com.clmf.player.data.remote.dto.EpgListingDto
import com.clmf.player.data.remote.dto.EpisodeDto
import com.clmf.player.data.remote.dto.MovieDto
import com.clmf.player.data.remote.dto.SeriesDto
import com.clmf.player.data.remote.dto.SeriesInfoDto
import com.clmf.player.domain.model.AccountInfo
import com.clmf.player.domain.model.Category
import com.clmf.player.domain.model.Channel
import com.clmf.player.domain.model.Connection
import com.clmf.player.domain.model.ContentType
import com.clmf.player.domain.model.Episode
import com.clmf.player.domain.model.EpgProgram
import com.clmf.player.domain.model.Movie
import com.clmf.player.domain.model.Series
import com.clmf.player.domain.repository.IPTVProvider
import com.clmf.player.utils.AppError
import com.clmf.player.utils.AppResult
import com.clmf.player.utils.ErrorMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamProvider @Inject constructor(
    private val api: XtreamApi,
    private val json: Json
) : IPTVProvider {

    private fun baseUrl(connection: Connection): String {
        val trimmed = connection.serverUrl.trimEnd('/')
        return "$trimmed/player_api.php"
    }

    private suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> = try {
        AppResult.Success(withContext(Dispatchers.IO) { block() })
    } catch (t: Throwable) {
        AppResult.Error(ErrorMapper.map(t))
    }

    override suspend fun testConnection(connection: Connection): AppResult<AccountInfo> =
        getAccountInfo(connection)

    override suspend fun getAccountInfo(connection: Connection): AppResult<AccountInfo> = safeCall {
        val element = api.call(baseUrl(connection), connection.username, connection.password)
        val dto = json.decodeFromJsonElement(AccountInfoDto.serializer(), element)
        val userInfo = dto.userInfo ?: throw IllegalStateException("Missing user_info")
        if (userInfo.auth == 0) throw XtreamAuthException()
        AccountInfo(
            username = userInfo.username.orEmpty(),
            status = userInfo.status ?: "Unknown",
            expirationDate = userInfo.expDate?.toLongOrNull()?.times(1000),
            isTrial = userInfo.isTrial == "1",
            activeConnections = userInfo.activeConnections?.toIntOrNull() ?: 0,
            maxConnections = userInfo.maxConnections?.toIntOrNull() ?: 1
        )
    }.let { result ->
        if (result is AppResult.Error && result.error is AppError.Unknown) {
            AppResult.Error(AppError.InvalidCredentials)
        } else result
    }

    override suspend fun getLiveCategories(connection: Connection): AppResult<List<Category>> = safeCall {
        val element = api.call(baseUrl(connection), connection.username, connection.password, action = "get_live_categories")
        decodeCategoryList(element, ContentType.LIVE)
    }

    override suspend fun getLiveChannels(connection: Connection, categoryId: String?): AppResult<List<Channel>> = safeCall {
        val element = api.call(
            baseUrl(connection), connection.username, connection.password,
            action = "get_live_streams", categoryId = categoryId
        )
        val list = decodeList(element, ChannelDto.serializer())
        list.map { dto ->
            Channel(
                id = dto.streamId.toString(),
                name = dto.name,
                logoUrl = dto.streamIcon,
                categoryId = dto.categoryId.orEmpty(),
                streamUrl = buildLiveStreamUrl(connection, dto.streamId.toString()),
                epgChannelId = dto.epgChannelId
            )
        }
    }

    override suspend fun getMovieCategories(connection: Connection): AppResult<List<Category>> = safeCall {
        val element = api.call(baseUrl(connection), connection.username, connection.password, action = "get_vod_categories")
        decodeCategoryList(element, ContentType.MOVIE)
    }

    override suspend fun getMovies(connection: Connection, categoryId: String?): AppResult<List<Movie>> = safeCall {
        val element = api.call(
            baseUrl(connection), connection.username, connection.password,
            action = "get_vod_streams", categoryId = categoryId
        )
        val list = decodeList(element, MovieDto.serializer())
        list.map { dto ->
            val extension = dto.containerExtension ?: "mp4"
            Movie(
                id = dto.streamId.toString(),
                name = dto.name,
                posterUrl = dto.streamIcon,
                categoryId = dto.categoryId.orEmpty(),
                streamUrl = buildMovieStreamUrl(connection, dto.streamId.toString(), extension),
                year = dto.releaseDate?.take(4),
                rating = dto.rating?.toDoubleOrNull()
            )
        }
    }

    override suspend fun getSeriesCategories(connection: Connection): AppResult<List<Category>> = safeCall {
        val element = api.call(baseUrl(connection), connection.username, connection.password, action = "get_series_categories")
        decodeCategoryList(element, ContentType.SERIES)
    }

    override suspend fun getSeries(connection: Connection, categoryId: String?): AppResult<List<Series>> = safeCall {
        val element = api.call(
            baseUrl(connection), connection.username, connection.password,
            action = "get_series", categoryId = categoryId
        )
        val list = decodeList(element, SeriesDto.serializer())
        list.map { dto ->
            Series(
                id = dto.seriesId.toString(),
                name = dto.name,
                posterUrl = dto.cover,
                categoryId = dto.categoryId.orEmpty(),
                description = dto.plot,
                year = dto.releaseDate?.take(4),
                genre = dto.genre,
                cast = dto.cast,
                rating = dto.rating?.toDoubleOrNull()
            )
        }
    }

    override suspend fun getEpisodes(connection: Connection, seriesId: String): AppResult<List<Episode>> = safeCall {
        val element = api.call(
            baseUrl(connection), connection.username, connection.password,
            action = "get_series_info", seriesId = seriesId
        )
        val info = json.decodeFromJsonElement(SeriesInfoDto.serializer(), element)
        val episodes = mutableListOf<Episode>()
        info.episodes?.forEach { (_, seasonEpisodes) ->
            seasonEpisodes.forEach { dto ->
                val extension = dto.containerExtension ?: "mp4"
                episodes += Episode(
                    id = dto.id,
                    seriesId = seriesId,
                    seasonNumber = dto.season ?: 1,
                    episodeNumber = dto.episodeNum,
                    name = dto.title ?: "Episódio ${dto.episodeNum}",
                    streamUrl = buildSeriesEpisodeUrl(connection, dto.id, extension),
                    thumbnailUrl = dto.info?.movieImage,
                    description = dto.info?.plot,
                    durationMinutes = dto.info?.durationSecs?.div(60)
                )
            }
        }
        episodes.sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
    }

    override suspend fun getShortEpg(connection: Connection, channelId: String): AppResult<List<EpgProgram>> = safeCall {
        val element = api.call(
            baseUrl(connection), connection.username, connection.password,
            action = "get_short_epg", streamId = channelId
        )
        val listing = json.decodeFromJsonElement(EpgListingDto.serializer(), element)
        listing.epgListings.orEmpty().mapNotNull { dto ->
            val start = dto.startTimestamp?.toLongOrNull()?.times(1000) ?: return@mapNotNull null
            val end = dto.stopTimestamp?.toLongOrNull()?.times(1000) ?: return@mapNotNull null
            EpgProgram(
                channelId = channelId,
                title = decodeBase64OrRaw(dto.title) ?: "Sem título",
                description = decodeBase64OrRaw(dto.description),
                startTimeMillis = start,
                endTimeMillis = end
            )
        }
    }

    private fun decodeBase64OrRaw(value: String?): String? {
        if (value.isNullOrBlank()) return value
        return runCatching {
            String(android.util.Base64.decode(value, android.util.Base64.DEFAULT))
        }.getOrDefault(value)
    }

    override fun buildLiveStreamUrl(connection: Connection, channelId: String, extension: String): String {
        val base = connection.serverUrl.trimEnd('/')
        return "$base/live/${connection.username}/${connection.password}/$channelId.$extension"
    }

    override fun buildMovieStreamUrl(connection: Connection, movieId: String, extension: String): String {
        val base = connection.serverUrl.trimEnd('/')
        return "$base/movie/${connection.username}/${connection.password}/$movieId.$extension"
    }

    private fun buildSeriesEpisodeUrl(connection: Connection, episodeId: String, extension: String): String {
        val base = connection.serverUrl.trimEnd('/')
        return "$base/series/${connection.username}/${connection.password}/$episodeId.$extension"
    }

    private fun decodeCategoryList(element: JsonElement, type: ContentType): List<Category> =
        decodeList(element, CategoryDto.serializer()).map {
            Category(id = it.categoryId, name = it.categoryName, type = type)
        }

    private fun <T> decodeList(element: JsonElement, serializer: kotlinx.serialization.KSerializer<T>): List<T> {
        if (element !is JsonArray) return emptyList()
        return element.mapNotNull { item ->
            runCatching { json.decodeFromJsonElement(serializer, item) }.getOrNull()
        }
    }
}

class XtreamAuthException : Exception("Invalid Xtream credentials")
