package com.clmf.player.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "clmf_settings")

enum class AppTheme { DARK, LIGHT, SYSTEM }

data class PlayerSettings(
    val autoPlay: Boolean = true,
    val autoFullscreen: Boolean = true,
    val autoRetry: Boolean = true,
    val bufferSeconds: Int = 30
)

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val AUTO_PLAY = booleanPreferencesKey("auto_play")
        val AUTO_FULLSCREEN = booleanPreferencesKey("auto_fullscreen")
        val AUTO_RETRY = booleanPreferencesKey("auto_retry")
        val SHOW_CATEGORIES = booleanPreferencesKey("show_categories")
    }

    val theme: Flow<AppTheme> = context.dataStore.data.map {
        runCatching { AppTheme.valueOf(it[Keys.THEME] ?: AppTheme.DARK.name) }.getOrDefault(AppTheme.DARK)
    }

    val playerSettings: Flow<PlayerSettings> = context.dataStore.data.map {
        PlayerSettings(
            autoPlay = it[Keys.AUTO_PLAY] ?: true,
            autoFullscreen = it[Keys.AUTO_FULLSCREEN] ?: true,
            autoRetry = it[Keys.AUTO_RETRY] ?: true
        )
    }

    val showCategories: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHOW_CATEGORIES] ?: true }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setAutoPlay(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_PLAY] = enabled }
    }

    suspend fun setAutoFullscreen(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_FULLSCREEN] = enabled }
    }

    suspend fun setAutoRetry(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_RETRY] = enabled }
    }

    suspend fun setShowCategories(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_CATEGORIES] = enabled }
    }
}
