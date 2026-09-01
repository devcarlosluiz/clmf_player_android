package com.clmf.player.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.clmf.player.domain.model.ContentType
import com.clmf.player.presentation.components.CLMFChannelCard
import com.clmf.player.player.PlayerState
import kotlin.math.roundToLong

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    navController: NavHostController,
    contentType: String,
    contentId: String,
    streamUrl: String,
    title: String,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val liveChannels by viewModel.liveChannels.collectAsState()
    var showChannelList by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    val isLive = contentType == ContentType.LIVE.name

    LaunchedEffect(contentId) {
        viewModel.start(contentType, contentId, streamUrl, title)
    }

    // Orientation is locked to landscape app-wide via the manifest, so the
    // player no longer needs to request/restore it itself — just clean up.
    DisposableEffect(Unit) {
        onDispose { viewModel.saveProgress() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    player = viewModel.playerManager.exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { showControls = !showControls }
        )

        when (val state = playerState) {
            is PlayerState.Loading, is PlayerState.Buffering -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.secondary)
            }
            is PlayerState.Retrying -> {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    Text(
                        "Reconectando... (${state.attempt}/${state.maxAttempts})",
                        color = Color.White,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
            is PlayerState.Error -> {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = Color.White, modifier = Modifier.padding(16.dp))
                    Row {
                        androidx.compose.material3.TextButton(onClick = viewModel::retry) { Text("TENTAR NOVAMENTE") }
                        androidx.compose.material3.TextButton(onClick = { navController.popBackStack() }) { Text("VOLTAR") }
                    }
                }
            }
            else -> Unit
        }

        if (showControls) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                    if (isLive) {
                        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                        IconButton(onClick = { showChannelList = true }) {
                            Icon(Icons.Filled.List, contentDescription = "Lista de canais", tint = Color.White)
                        }
                    }
                }

                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLive) {
                        IconButton(onClick = { viewModel.playNextChannel(forward = false) }) {
                            Icon(Icons.Filled.SkipPrevious, contentDescription = "Canal anterior", tint = Color.White)
                        }
                    }
                    IconButton(onClick = viewModel::togglePlayPause) {
                        Icon(
                            imageVector = if (playerState is PlayerState.Playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    if (isLive) {
                        IconButton(onClick = { viewModel.playNextChannel(forward = true) }) {
                            Icon(Icons.Filled.SkipNext, contentDescription = "Próximo canal", tint = Color.White)
                        }
                    }
                }

                if (!isLive) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(formatMillis(progress.positionMillis), color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = progress.positionMillis.toFloat(),
                            valueRange = 0f..(progress.durationMillis.coerceAtLeast(1).toFloat()),
                            onValueChange = { viewModel.seekTo(it.roundToLong()) },
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text(formatMillis(progress.durationMillis), color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
                androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
            }
        }

        if (showChannelList) {
            ModalBottomSheet(onDismissRequest = { showChannelList = false }) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(liveChannels, key = { it.id }) { channel ->
                        CLMFChannelCard(
                            name = channel.name,
                            logoUrl = channel.logoUrl,
                            isFavorite = channel.isFavorite,
                            onClick = {
                                viewModel.playChannel(channel)
                                showChannelList = false
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatMillis(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
