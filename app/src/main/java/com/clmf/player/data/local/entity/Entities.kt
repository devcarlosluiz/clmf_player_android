package com.clmf.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connections")
data class ConnectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "XTREAM",
    val serverUrl: String = "",
    val username: String = "",
    val encryptedPassword: String = "",
    val playlistUrl: String = "",
    val isSelected: Boolean = false
)

@Entity(tableName = "categories", primaryKeys = ["id", "type"])
data class CategoryEntity(
    val id: String,
    val name: String,
    val type: String
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logoUrl: String?,
    val categoryId: String,
    val streamUrl: String,
    val epgChannelId: String?
)

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: String,
    val name: String,
    val posterUrl: String?,
    val categoryId: String,
    val streamUrl: String,
    val description: String?,
    val year: String?,
    val genre: String?,
    val durationMinutes: Int?,
    val cast: String?,
    val rating: Double?
)

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey val id: String,
    val name: String,
    val posterUrl: String?,
    val categoryId: String,
    val description: String?,
    val year: String?,
    val genre: String?,
    val cast: String?,
    val rating: Double?
)

@Entity(tableName = "favorites", primaryKeys = ["contentId", "contentType"])
data class FavoriteEntity(
    val contentId: String,
    val contentType: String,
    val name: String,
    val imageUrl: String?,
    val addedAtMillis: Long
)

@Entity(tableName = "history", primaryKeys = ["contentId", "contentType"])
data class HistoryEntity(
    val contentId: String,
    val contentType: String,
    val name: String,
    val imageUrl: String?,
    val positionMillis: Long,
    val durationMillis: Long,
    val watchedAtMillis: Long,
    val streamUrl: String,
    val seriesId: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?
)
