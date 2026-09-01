package com.clmf.player.data.remote.m3u

import com.clmf.player.domain.model.Category
import com.clmf.player.domain.model.Channel
import com.clmf.player.domain.model.ContentType
import com.clmf.player.utils.AppError
import com.clmf.player.utils.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

data class M3uPlaylist(
    val categories: List<Category>,
    val channels: List<Channel>
)

/**
 * Downloads an M3U/M3U8 playlist and turns it into the same [Category]/[Channel]
 * shapes the rest of the app already understands, using `group-title` as the
 * category. M3U playlists don't reliably distinguish movies/series from live
 * channels, so this only feeds the Live TV section.
 */
@Singleton
class M3uPlaylistFetcher @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    suspend fun fetch(playlistUrl: String): AppResult<M3uPlaylist> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(playlistUrl).build()
            val entries = okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext AppResult.Error(AppError.Http(response.code))
                // Read line-by-line straight from the network stream: some IPTV
                // panels serve playlists tens of megabytes long, and loading the
                // whole body into one String before parsing it can OOM.
                response.body?.charStream()?.buffered()?.use { reader ->
                    M3uParser.parse(reader.lineSequence())
                }.orEmpty()
            }
            if (entries.isEmpty()) return@withContext AppResult.Error(AppError.EmptyPlaylist)

            val categoryNames = entries.mapNotNull { it.groupTitle }.distinct()
            val categories = categoryNames.map { name ->
                Category(id = name, name = name, type = ContentType.LIVE)
            }
            val channels = entries.mapIndexed { index, entry ->
                Channel(
                    id = entry.tvgId?.takeIf { it.isNotBlank() } ?: "m3u_$index",
                    name = entry.name,
                    logoUrl = entry.logoUrl,
                    categoryId = entry.groupTitle ?: "",
                    streamUrl = entry.streamUrl,
                    epgChannelId = entry.tvgId
                )
            }
            AppResult.Success(M3uPlaylist(categories, channels))
        } catch (t: Throwable) {
            AppResult.Error(com.clmf.player.utils.ErrorMapper.map(t))
        }
    }
}
