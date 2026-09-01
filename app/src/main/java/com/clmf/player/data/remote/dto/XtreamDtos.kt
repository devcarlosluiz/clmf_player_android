package com.clmf.player.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoDto(
    @SerialName("username") val username: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("exp_date") val expDate: String? = null,
    @SerialName("is_trial") val isTrial: String? = null,
    @SerialName("active_cons") val activeConnections: String? = null,
    @SerialName("max_connections") val maxConnections: String? = null,
    @SerialName("auth") val auth: Int? = null
)

@Serializable
data class ServerInfoDto(
    @SerialName("url") val url: String? = null,
    @SerialName("port") val port: String? = null,
    @SerialName("https_port") val httpsPort: String? = null
)

@Serializable
data class AccountInfoDto(
    @SerialName("user_info") val userInfo: UserInfoDto? = null,
    @SerialName("server_info") val serverInfo: ServerInfoDto? = null
)

@Serializable
data class CategoryDto(
    @SerialName("category_id") val categoryId: String,
    @SerialName("category_name") val categoryName: String
)

@Serializable
data class ChannelDto(
    @SerialName("stream_id") val streamId: Int,
    @SerialName("name") val name: String,
    @SerialName("stream_icon") val streamIcon: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("epg_channel_id") val epgChannelId: String? = null
)

@Serializable
data class MovieDto(
    @SerialName("stream_id") val streamId: Int,
    @SerialName("name") val name: String,
    @SerialName("stream_icon") val streamIcon: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("container_extension") val containerExtension: String? = null,
    @SerialName("rating") val rating: String? = null,
    @SerialName("releaseDate") val releaseDate: String? = null
)

@Serializable
data class MovieInfoDto(
    @SerialName("info") val info: MovieDetailDto? = null,
    @SerialName("movie_data") val movieData: MovieDto? = null
)

@Serializable
data class MovieDetailDto(
    @SerialName("plot") val plot: String? = null,
    @SerialName("genre") val genre: String? = null,
    @SerialName("duration") val duration: String? = null,
    @SerialName("cast") val cast: String? = null,
    @SerialName("releasedate") val releaseDate: String? = null,
    @SerialName("rating") val rating: String? = null
)

@Serializable
data class SeriesDto(
    @SerialName("series_id") val seriesId: Int,
    @SerialName("name") val name: String,
    @SerialName("cover") val cover: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("plot") val plot: String? = null,
    @SerialName("genre") val genre: String? = null,
    @SerialName("cast") val cast: String? = null,
    @SerialName("releaseDate") val releaseDate: String? = null,
    @SerialName("rating") val rating: String? = null
)

@Serializable
data class SeriesInfoDto(
    @SerialName("seasons") val seasons: List<SeasonDto>? = null,
    @SerialName("episodes") val episodes: Map<String, List<EpisodeDto>>? = null
)

@Serializable
data class SeasonDto(
    @SerialName("season_number") val seasonNumber: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("cover") val cover: String? = null
)

@Serializable
data class EpisodeDto(
    @SerialName("id") val id: String,
    @SerialName("episode_num") val episodeNum: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("container_extension") val containerExtension: String? = null,
    @SerialName("season") val season: Int? = null,
    @SerialName("info") val info: EpisodeInfoDto? = null
)

@Serializable
data class EpisodeInfoDto(
    @SerialName("plot") val plot: String? = null,
    @SerialName("duration_secs") val durationSecs: Int? = null,
    @SerialName("movie_image") val movieImage: String? = null
)

@Serializable
data class EpgListingDto(
    @SerialName("epg_listings") val epgListings: List<EpgDto>? = null
)

@Serializable
data class EpgDto(
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("start_timestamp") val startTimestamp: String? = null,
    @SerialName("stop_timestamp") val stopTimestamp: String? = null
)
