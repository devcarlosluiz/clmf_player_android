package com.clmf.player.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.clmf.player.data.local.entity.CategoryEntity
import com.clmf.player.data.local.entity.ChannelEntity
import com.clmf.player.data.local.entity.ConnectionEntity
import com.clmf.player.data.local.entity.FavoriteEntity
import com.clmf.player.data.local.entity.HistoryEntity
import com.clmf.player.data.local.entity.MovieEntity
import com.clmf.player.data.local.entity.SeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connections ORDER BY id DESC")
    fun observeAll(): Flow<List<ConnectionEntity>>

    @Query("SELECT * FROM connections WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelected(): ConnectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(connection: ConnectionEntity): Long

    @Query("DELETE FROM connections WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE connections SET isSelected = (id = :id)")
    suspend fun selectOnly(id: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name")
    fun observeByType(type: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE type = :type")
    suspend fun clearType(type: String)

    @Transaction
    suspend fun replaceType(type: String, categories: List<CategoryEntity>) {
        clearType(type)
        insertAll(categories)
    }
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY name")
    fun observeAll(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' LIMIT 50")
    suspend fun search(query: String): List<ChannelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(channels: List<ChannelEntity>) {
        clearAll()
        insertAll(channels)
    }
}

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY name")
    fun observeAll(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE name LIKE '%' || :query || '%' LIMIT 50")
    suspend fun search(query: String): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    @Query("DELETE FROM movies")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(movies: List<MovieEntity>) {
        clearAll()
        insertAll(movies)
    }
}

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series ORDER BY name")
    fun observeAll(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE name LIKE '%' || :query || '%' LIMIT 50")
    suspend fun search(query: String): List<SeriesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(series: List<SeriesEntity>)

    @Query("DELETE FROM series")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(series: List<SeriesEntity>) {
        clearAll()
        insertAll(series)
    }
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE (:type IS NULL OR contentType = :type) ORDER BY addedAtMillis DESC")
    fun observe(type: String?): Flow<List<FavoriteEntity>>

    @Query("SELECT COUNT(*) FROM favorites WHERE contentId = :contentId AND contentType = :contentType")
    suspend fun count(contentId: String, contentType: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE contentId = :contentId AND contentType = :contentType")
    suspend fun delete(contentId: String, contentType: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY watchedAtMillis DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE contentId = :contentId AND contentType = :contentType LIMIT 1")
    suspend fun get(contentId: String, contentType: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HistoryEntity)

    @Query("DELETE FROM history WHERE contentId = :contentId AND contentType = :contentType")
    suspend fun delete(contentId: String, contentType: String)

    @Query("DELETE FROM history")
    suspend fun clear()
}
