package com.clmf.player.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.clmf.player.data.local.dao.CategoryDao
import com.clmf.player.data.local.dao.ChannelDao
import com.clmf.player.data.local.dao.ConnectionDao
import com.clmf.player.data.local.dao.FavoriteDao
import com.clmf.player.data.local.dao.HistoryDao
import com.clmf.player.data.local.dao.MovieDao
import com.clmf.player.data.local.dao.SeriesDao
import com.clmf.player.data.local.entity.CategoryEntity
import com.clmf.player.data.local.entity.ChannelEntity
import com.clmf.player.data.local.entity.ConnectionEntity
import com.clmf.player.data.local.entity.FavoriteEntity
import com.clmf.player.data.local.entity.HistoryEntity
import com.clmf.player.data.local.entity.MovieEntity
import com.clmf.player.data.local.entity.SeriesEntity

@Database(
    entities = [
        ConnectionEntity::class,
        CategoryEntity::class,
        ChannelEntity::class,
        MovieEntity::class,
        SeriesEntity::class,
        FavoriteEntity::class,
        HistoryEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun connectionDao(): ConnectionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun channelDao(): ChannelDao
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao

    companion object {
        const val DATABASE_NAME = "clmf_player.db"
    }
}
