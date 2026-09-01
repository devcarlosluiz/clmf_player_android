package com.clmf.player.presentation.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.clmf.player.domain.model.Connection
import com.clmf.player.presentation.components.CLMFErrorView
import com.clmf.player.presentation.navigation.Routes
import com.clmf.player.presentation.theme.ClmfAccent
import com.clmf.player.presentation.theme.ClmfOnSurfaceMuted
import com.clmf.player.presentation.theme.ClmfSurfaceVariant

@Composable
fun PlaylistsScreen(navController: NavHostController, viewModel: PlaylistsViewModel = hiltViewModel()) {
    val connections by viewModel.connections.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlists") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.ADD_PLAYLIST) }) {
                Icon(Icons.Filled.Add, contentDescription = "Nova playlist")
            }
        }
    ) { padding ->
        if (connections.isEmpty()) {
            CLMFErrorView(
                message = "Nenhuma playlist cadastrada. Toque em + para adicionar uma.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = 16.dp
                )
            ) {
                items(connections, key = { it.id }) { connection ->
                    PlaylistRow(
                        connection = connection,
                        onActivate = { viewModel.activate(connection) },
                        onDelete = { viewModel.delete(connection) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    connection: Connection,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ClmfSurfaceVariant)
            .clickable(onClick = onActivate)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = if (connection.isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (connection.isSelected) "Ativa" else "Ativar",
            tint = if (connection.isSelected) ClmfAccent else ClmfOnSurfaceMuted
        )
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(connection.name, style = MaterialTheme.typography.bodyLarge)
            val subtitle = when (connection.type) {
                com.clmf.player.domain.model.ConnectionType.XTREAM -> connection.serverUrl
                com.clmf.player.domain.model.ConnectionType.M3U -> "M3U · ${connection.playlistUrl}"
            }
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ClmfOnSurfaceMuted)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Remover", tint = ClmfOnSurfaceMuted)
        }
    }
}
