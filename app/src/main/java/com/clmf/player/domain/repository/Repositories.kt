package com.clmf.player.domain.repository

import com.clmf.player.domain.model.Connection
import com.clmf.player.domain.model.ContentType
import com.clmf.player.domain.model.FavoriteItem
import com.clmf.player.domain.model.HistoryItem
import kotlinx.coroutines.flow.Flow

interface ConnectionRepository {
    fun observeConnections(): Flow<List<Connection>>
    suspend fun getSelectedConnection(): Connection?
    suspend fun saveConnection(connection: Connection): Long
    suspend fun deleteConnection(connectionId: Long)
    suspend fun selectConnection(connectionId: Long)
}

interface FavoritesRepository {
    fun observeFavorites(type: ContentType? = null): Flow<List<FavoriteItem>>
    suspend fun isFavorite(contentId: String, type: ContentType): Boolean
    suspend fun toggleFavorite(item: FavoriteItem): Boolean
}

interface HistoryRepository {
    fun observeHistory(): Flow<List<HistoryItem>>
    suspend fun upsertProgress(item: HistoryItem)
    suspend fun remove(contentId: String, type: ContentType)
    suspend fun clear()
    suspend fun getProgress(contentId: String, type: ContentType): HistoryItem?
}
