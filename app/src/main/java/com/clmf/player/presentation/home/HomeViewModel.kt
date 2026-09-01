package com.clmf.player.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clmf.player.BuildConfig
import com.clmf.player.domain.model.ConnectionType
import com.clmf.player.domain.model.HistoryItem
import com.clmf.player.domain.repository.ConnectionRepository
import com.clmf.player.domain.repository.ContentRepository
import com.clmf.player.domain.repository.HistoryRepository
import com.clmf.player.domain.repository.IPTVProvider
import com.clmf.player.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val continueWatching: List<HistoryItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val expirationDate: String? = null,
    val versionName: String = BuildConfig.VERSION_NAME
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val contentRepository: ContentRepository,
    private val connectionRepository: ConnectionRepository,
    private val iptvProvider: IPTVProvider
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _expirationDate = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        historyRepository.observeHistory(),
        _isRefreshing,
        _expirationDate
    ) { history, refreshing, expiration ->
        HomeUiState(
            continueWatching = history.take(15),
            isRefreshing = refreshing,
            expirationDate = expiration
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        refresh()
        loadAccountInfo()
    }

    private fun loadAccountInfo() {
        viewModelScope.launch {
            val connection = connectionRepository.getSelectedConnection() ?: return@launch
            if (connection.type == ConnectionType.M3U) return@launch
            val result = iptvProvider.getAccountInfo(connection)
            if (result is AppResult.Success) {
                val expMillis = result.data.expirationDate
                _expirationDate.value = expMillis?.let {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            contentRepository.refreshAll()
            _isRefreshing.value = false
        }
    }
}
