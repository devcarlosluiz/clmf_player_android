package com.clmf.player.di

import android.content.Context
import androidx.room.Room
import com.clmf.player.data.local.dao.CategoryDao
import com.clmf.player.data.local.dao.ChannelDao
import com.clmf.player.data.local.dao.ConnectionDao
import com.clmf.player.data.local.dao.FavoriteDao
import com.clmf.player.data.local.dao.HistoryDao
import com.clmf.player.data.local.dao.MovieDao
import com.clmf.player.data.local.dao.SeriesDao
import com.clmf.player.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideConnectionDao(db: AppDatabase): ConnectionDao = db.connectionDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideChannelDao(db: AppDatabase): ChannelDao = db.channelDao()
    @Provides fun provideMovieDao(db: AppDatabase): MovieDao = db.movieDao()
    @Provides fun provideSeriesDao(db: AppDatabase): SeriesDao = db.seriesDao()
    @Provides fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()
}
