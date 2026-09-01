package com.clmf.player.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.clmf.player.presentation.navigation.Routes
import com.clmf.player.BuildConfig
import com.clmf.player.data.local.PreferencesDataStore
import com.clmf.player.domain.repository.ConnectionRepository
import com.clmf.player.domain.repository.ContentRepository
import com.clmf.player.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val contentRepository: ContentRepository,
    private val historyRepository: HistoryRepository,
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val playerSettings = preferencesDataStore.playerSettings
    val theme = preferencesDataStore.theme

    fun setAutoPlay(enabled: Boolean) = viewModelScope.launch { preferencesDataStore.setAutoPlay(enabled) }
    fun setAutoRetry(enabled: Boolean) = viewModelScope.launch { preferencesDataStore.setAutoRetry(enabled) }
    fun setAutoFullscreen(enabled: Boolean) = viewModelScope.launch { preferencesDataStore.setAutoFullscreen(enabled) }

    fun refreshContent() = viewModelScope.launch {
        _isSyncing.value = true
        contentRepository.refreshAll()
        _isSyncing.value = false
    }

    fun clearHistory() = viewModelScope.launch { historyRepository.clear() }
}

@Composable
fun SettingsScreen(navController: NavHostController, viewModel: SettingsViewModel = hiltViewModel()) {
    val playerSettings by viewModel.playerSettings.collectAsState(initial = com.clmf.player.data.local.PlayerSettings())
    val isSyncing by viewModel.isSyncing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Player", style = MaterialTheme.typography.titleMedium)
            SettingsSwitchRow("Reprodução automática", playerSettings.autoPlay, viewModel::setAutoPlay)
            SettingsSwitchRow("Auto fullscreen", playerSettings.autoFullscreen, viewModel::setAutoFullscreen)
            SettingsSwitchRow("Reconexão automática", playerSettings.autoRetry, viewModel::setAutoRetry)

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Playlists", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text("Gerenciar playlists")
                androidx.compose.material3.TextButton(onClick = { navController.navigate(Routes.PLAYLISTS) }) {
                    Text("ABRIR")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(if (isSyncing) "Atualizando conteúdo..." else "Atualizar conteúdo")
                androidx.compose.material3.TextButton(onClick = viewModel::refreshContent, enabled = !isSyncing) {
                    Text("ATUALIZAR")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Conteúdo", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text("Limpar histórico")
                androidx.compose.material3.TextButton(onClick = viewModel::clearHistory) {
                    Text("LIMPAR")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Aplicativo", style = MaterialTheme.typography.titleMedium)
            Text("Versão ${BuildConfig.VERSION_NAME}", modifier = Modifier.padding(top = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text("Licença e ID do dispositivo")
                androidx.compose.material3.TextButton(onClick = { navController.navigate(Routes.LICENSE) }) {
                    Text("VER")
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
