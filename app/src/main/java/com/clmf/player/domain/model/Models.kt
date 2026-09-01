package com.clmf.player.domain.model

enum class ContentType { LIVE, MOVIE, SERIES, EPISODE }

enum class ConnectionType { XTREAM, M3U }

data class Connection(
    val id: Long = 0,
    val name: String,
    val type: ConnectionType = ConnectionType.XTREAM,
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val playlistUrl: String = "",
    val isSelected: Boolean = false
)

data class AccountInfo(
    val username: String,
    val status: String,
    val expirationDate: Long?,
    val isTrial: Boolean,
    val activeConnections: Int,
    val maxConnections: Int
)

data class Category(
    val id: String,
    val name: String,
    val type: ContentType
)

data class Channel(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val categoryId: String,
    val streamUrl: String,
    val epgChannelId: String?,
    val isFavorite: Boolean = false
)

data class Movie(
    val id: String,
    val name: String,
    val posterUrl: String?,
    val categoryId: String,
    val streamUrl: String,
    val description: String? = null,
    val year: String? = null,
    val genre: String? = null,
    val durationMinutes: Int? = null,
    val cast: String? = null,
    val rating: Double? = null,
    val isFavorite: Boolean = false
)

data class Series(
    val id: String,
    val name: String,
    val posterUrl: String?,
    val categoryId: String,
    val description: String? = null,
    val year: String? = null,
    val genre: String? = null,
    val cast: String? = null,
    val rating: Double? = null,
    val isFavorite: Boolean = false
)

data class Season(
    val seriesId: String,
    val seasonNumber: Int,
    val name: String,
    val posterUrl: String? = null
)

data class Episode(
    val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val name: String,
    val streamUrl: String,
    val thumbnailUrl: String? = null,
    val description: String? = null,
    val durationMinutes: Int? = null
)

data class EpgProgram(
    val channelId: String,
    val title: String,
    val description: String?,
    val startTimeMillis: Long,
    val endTimeMillis: Long
)

data class FavoriteItem(
    val contentId: String,
    val contentType: ContentType,
    val name: String,
    val imageUrl: String?,
    val addedAtMillis: Long
)

data class HistoryItem(
    val contentId: String,
    val contentType: ContentType,
    val name: String,
    val imageUrl: String?,
    val positionMillis: Long,
    val durationMillis: Long,
    val watchedAtMillis: Long,
    val streamUrl: String,
    val seriesId: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null
) {
    val progress: Float
        get() = if (durationMillis > 0) (positionMillis.toFloat() / durationMillis).coerceIn(0f, 1f) else 0f
}

/** Everything the player needs to start a playback session, regardless of content type. */
data class PlaybackRequest(
    val contentId: String,
    val contentType: ContentType,
    val title: String,
    val streamUrl: String,
    val imageUrl: String? = null,
    val startPositionMillis: Long = 0L,
    val channelList: List<Channel> = emptyList()
)
