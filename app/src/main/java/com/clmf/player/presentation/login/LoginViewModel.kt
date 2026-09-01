package com.clmf.player.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clmf.player.data.remote.m3u.M3uPlaylistFetcher
import com.clmf.player.domain.model.Connection
import com.clmf.player.domain.model.ConnectionType
import com.clmf.player.domain.repository.ConnectionRepository
import com.clmf.player.domain.repository.IPTVProvider
import com.clmf.player.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val type: ConnectionType = ConnectionType.XTREAM,
    val name: String = "",
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val playlistUrl: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val connected: Boolean = false
) {
    val isFormValid: Boolean
        get() = when (type) {
            ConnectionType.XTREAM ->
                name.isNotBlank() && serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            ConnectionType.M3U ->
                name.isNotBlank() && playlistUrl.isNotBlank()
        }
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val iptvProvider: IPTVProvider,
    private val m3uPlaylistFetcher: M3uPlaylistFetcher,
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onTypeChange(value: ConnectionType) = _uiState.update { it.copy(type = value, errorMessage = null) }
    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onServerChange(value: String) = _uiState.update { it.copy(serverUrl = value, errorMessage = null) }
    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onPlaylistUrlChange(value: String) = _uiState.update { it.copy(playlistUrl = value, errorMessage = null) }
    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun connect() {
        val state = _uiState.value
        if (!state.isFormValid) {
            _uiState.update { it.copy(errorMessage = "Preencha todos os campos.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (state.type) {
                ConnectionType.XTREAM -> connectXtream(state)
                ConnectionType.M3U -> connectM3u(state)
            }
        }
    }

    private suspend fun connectXtream(state: LoginUiState) {
        val connection = Connection(
            name = state.name.trim(),
            type = ConnectionType.XTREAM,
            serverUrl = normalizeUrl(state.serverUrl),
            username = state.username.trim(),
            password = state.password
        )
        when (val result = iptvProvider.testConnection(connection)) {
            is AppResult.Success -> {
                connectionRepository.saveConnection(connection)
                _uiState.update { it.copy(isLoading = false, connected = true) }
            }
            is AppResult.Error -> {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.error.friendlyMessage) }
            }
        }
    }

    private suspend fun connectM3u(state: LoginUiState) {
        val playlistUrl = state.playlistUrl.trim()
        when (val result = m3uPlaylistFetcher.fetch(playlistUrl)) {
            is AppResult.Success -> {
                val connection = Connection(
                    name = state.name.trim(),
                    type = ConnectionType.M3U,
                    playlistUrl = playlistUrl
                )
                connectionRepository.saveConnection(connection)
                _uiState.update { it.copy(isLoading = false, connected = true) }
            }
            is AppResult.Error -> {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.error.friendlyMessage) }
            }
        }
    }

    private fun normalizeUrl(input: String): String {
        val trimmed = input.trim().trimEnd('/')
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "http://$trimmed"
    }
}
