package com.clmf.player.di

import com.clmf.player.data.remote.XtreamProvider
import com.clmf.player.data.repository.ConnectionRepositoryImpl
import com.clmf.player.data.repository.ContentRepositoryImpl
import com.clmf.player.data.repository.FavoritesRepositoryImpl
import com.clmf.player.data.repository.HistoryRepositoryImpl
import com.clmf.player.domain.repository.ConnectionRepository
import com.clmf.player.domain.repository.ContentRepository
import com.clmf.player.domain.repository.FavoritesRepository
import com.clmf.player.domain.repository.HistoryRepository
import com.clmf.player.domain.repository.IPTVProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindIPTVProvider(impl: XtreamProvider): IPTVProvider

    @Binds
    @Singleton
    abstract fun bindConnectionRepository(impl: ConnectionRepositoryImpl): ConnectionRepository

    @Binds
    @Singleton
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository
}
