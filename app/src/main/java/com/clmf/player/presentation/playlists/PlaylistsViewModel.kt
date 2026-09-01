package com.clmf.player.presentation.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clmf.player.domain.model.Connection
import com.clmf.player.domain.repository.ConnectionRepository
import com.clmf.player.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {

    val connections: StateFlow<List<Connection>> = connectionRepository.observeConnections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun activate(connection: Connection) {
        viewModelScope.launch {
            connectionRepository.selectConnection(connection.id)
            contentRepository.refreshAll()
        }
    }

    fun delete(connection: Connection) {
        viewModelScope.launch { connectionRepository.deleteConnection(connection.id) }
    }
}
