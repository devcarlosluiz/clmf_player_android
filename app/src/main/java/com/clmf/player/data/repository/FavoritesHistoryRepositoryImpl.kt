package com.clmf.player.data.repository

import com.clmf.player.data.local.dao.FavoriteDao
import com.clmf.player.data.local.dao.HistoryDao
import com.clmf.player.data.local.entity.FavoriteEntity
import com.clmf.player.data.local.entity.HistoryEntity
import com.clmf.player.domain.model.ContentType
import com.clmf.player.domain.model.FavoriteItem
import com.clmf.player.domain.model.HistoryItem
import com.clmf.player.domain.repository.FavoritesRepository
import com.clmf.player.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao
) : FavoritesRepository {

    override fun observeFavorites(type: ContentType?): Flow<List<FavoriteItem>> =
        dao.observe(type?.name).map { list -> list.map { it.toDomain() } }

    override suspend fun isFavorite(contentId: String, type: ContentType): Boolean =
        dao.count(contentId, type.name) > 0

    override suspend fun toggleFavorite(item: FavoriteItem): Boolean {
        val exists = dao.count(item.contentId, item.contentType.name) > 0
        if (exists) {
            dao.delete(item.contentId, item.contentType.name)
            return false
        }
        dao.insert(
            FavoriteEntity(
                contentId = item.contentId,
                contentType = item.contentType.name,
                name = item.name,
                imageUrl = item.imageUrl,
                addedAtMillis = item.addedAtMillis
            )
        )
        return true
    }

    private fun FavoriteEntity.toDomain() = FavoriteItem(
        contentId = contentId,
        contentType = ContentType.valueOf(contentType),
        name = name,
        imageUrl = imageUrl,
        addedAtMillis = addedAtMillis
    )
}

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryDao
) : HistoryRepository {

    override fun observeHistory(): Flow<List<HistoryItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsertProgress(item: HistoryItem) {
        dao.upsert(
            HistoryEntity(
                contentId = item.contentId,
                contentType = item.contentType.name,
                name = item.name,
                imageUrl = item.imageUrl,
                positionMillis = item.positionMillis,
                durationMillis = item.durationMillis,
                watchedAtMillis = item.watchedAtMillis,
                streamUrl = item.streamUrl,
                seriesId = item.seriesId,
                seasonNumber = item.seasonNumber,
                episodeNumber = item.episodeNumber
            )
        )
    }

    override suspend fun remove(contentId: String, type: ContentType) {
        dao.delete(contentId, type.name)
    }

    override suspend fun clear() {
        dao.clear()
    }

    override suspend fun getProgress(contentId: String, type: ContentType): HistoryItem? =
        dao.get(contentId, type.name)?.toDomain()

    private fun HistoryEntity.toDomain() = HistoryItem(
        contentId = contentId,
        contentType = ContentType.valueOf(contentType),
        name = name,
        imageUrl = imageUrl,
        positionMillis = positionMillis,
        durationMillis = durationMillis,
        watchedAtMillis = watchedAtMillis,
        streamUrl = streamUrl,
        seriesId = seriesId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber
    )
}
