package com.clmf.player.presentation.live

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.clmf.player.domain.model.Channel
import com.clmf.player.domain.model.ContentType
import com.clmf.player.player.PlayerManager
import com.clmf.player.presentation.components.CLMFCategoryChip
import com.clmf.player.presentation.components.CLMFChannelCard
import com.clmf.player.presentation.components.CLMFLoading
import com.clmf.player.presentation.navigation.Routes
import com.clmf.player.presentation.theme.ClmfOnSurfaceMuted
import com.clmf.player.presentation.theme.ClmfSurface
import com.clmf.player.presentation.theme.ClmfSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveTvScreen(navController: NavHostController, viewModel: LiveTvViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val epgPrograms by viewModel.epgPrograms.collectAsState()

    LaunchedEffect(state.channels) {
        if (selectedChannel == null && state.channels.isNotEmpty()) {
            viewModel.selectChannel(state.channels.first())
        }
    }

    fun goBack() {
        viewModel.stopPreview()
        navController.popBackStack()
    }

    BackHandler(onBack = ::goBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TV ao vivo") },
                navigationIcon = {
                    IconButton(onClick = ::goBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            CLMFLoading(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Below ~700dp (phones, portrait) there isn't room for a 3-column
            // layout with a live preview panel, so fall back to a single list
            // that opens the full-screen player directly. Tablets/TV/landscape
            // get the categories + channels + preview/EPG layout.
            if (maxWidth < 700.dp) {
                LiveTvCompactLayout(
                    state = state,
                    viewModel = viewModel,
                    onChannelClick = { channel ->
                        navController.navigate(
                            Routes.player(ContentType.LIVE.name, channel.id, channel.streamUrl, channel.name)
                        )
                    }
                )
            } else {
                LiveTvExpandedLayout(
                    state = state,
                    viewModel = viewModel,
                    selectedChannel = selectedChannel,
                    epgPrograms = epgPrograms,
                    onWatch = {
                        selectedChannel?.let { channel ->
                            navController.navigate(
                                Routes.player(ContentType.LIVE.name, channel.id, channel.streamUrl, channel.name)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LiveTvCompactLayout(
    state: LiveTvUiState,
    viewModel: LiveTvViewModel,
    onChannelClick: (Channel) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = { Text("Buscar canal...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
            item {
                CLMFCategoryChip(
                    name = "Todos",
                    selected = state.selectedCategoryId == null,
                    onClick = { viewModel.selectCategory(null) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            item {
                CLMFCategoryChip(
                    name = "Favoritos",
                    selected = state.selectedCategoryId == LiveTvViewModel.FAVORITES_ID,
                    onClick = { viewModel.selectCategory(LiveTvViewModel.FAVORITES_ID) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            items(state.categories, key = { it.id }) { category ->
                CLMFCategoryChip(
                    name = category.name,
                    selected = state.selectedCategoryId == category.id,
                    onClick = { viewModel.selectCategory(category.id) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(state.channels, key = { it.id }) { channel ->
                CLMFChannelCard(
                    name = channel.name,
                    logoUrl = channel.logoUrl,
                    isFavorite = channel.isFavorite,
                    onClick = { onChannelClick(channel) },
                    onToggleFavorite = { viewModel.toggleFavorite(channel) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LiveTvExpandedLayout(
    state: LiveTvUiState,
    viewModel: LiveTvViewModel,
    selectedChannel: Channel?,
    epgPrograms: List<com.clmf.player.domain.model.EpgProgram>,
    onWatch: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left column: categories
        LazyColumn(
            modifier = Modifier.width(170.dp).fillMaxHeight().background(ClmfSurface),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                CLMFCategoryChip(
                    name = "Todos",
                    selected = state.selectedCategoryId == null,
                    onClick = { viewModel.selectCategory(null) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            item {
                CLMFCategoryChip(
                    name = "Favoritos",
                    selected = state.selectedCategoryId == LiveTvViewModel.FAVORITES_ID,
                    onClick = { viewModel.selectCategory(LiveTvViewModel.FAVORITES_ID) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            items(state.categories, key = { it.id }) { category ->
                CLMFCategoryChip(
                    name = category.name,
                    selected = state.selectedCategoryId == category.id,
                    onClick = { viewModel.selectCategory(category.id) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Middle column: channel list
        Column(modifier = Modifier.width(280.dp).fillMaxHeight()) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Buscar canal...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
            LazyColumn(contentPadding = PaddingValues(horizontal = 8.dp)) {
                items(state.channels, key = { it.id }) { channel ->
                    CLMFChannelCard(
                        name = channel.name,
                        logoUrl = channel.logoUrl,
                        isFavorite = channel.isFavorite,
                        onClick = { viewModel.selectChannel(channel) },
                        onToggleFavorite = { viewModel.toggleFavorite(channel) },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }

        // Right column: preview + EPG
        ChannelPreviewPanel(
            channel = selectedChannel,
            epgPrograms = epgPrograms,
            playerManager = viewModel.playerManager,
            onWatch = onWatch,
            modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)
        )
    }
}

@Composable
private fun ChannelPreviewPanel(
    channel: Channel?,
    epgPrograms: List<com.clmf.player.domain.model.EpgProgram>,
    playerManager: PlayerManager,
    onWatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    // A single LazyColumn for the whole panel (instead of a plain Column) so
    // it always scrolls to reveal the EPG section on short/landscape screens,
    // without nesting a LazyColumn inside another scrollable container.
    LazyColumn(modifier = modifier) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(ClmfSurfaceVariant)
                    .clickable(enabled = channel != null, onClick = onWatch),
                contentAlignment = Alignment.Center
            ) {
                if (channel != null) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                player = playerManager.exoPlayer
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Selecione um canal", color = ClmfOnSurfaceMuted)
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
            if (channel != null) {
                Text(channel.name, style = MaterialTheme.typography.titleMedium)
                androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            }
            Text("Guia de programação", style = MaterialTheme.typography.titleMedium)
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        }

        if (epgPrograms.isEmpty()) {
            item {
                Text("Nenhuma programação disponível para este canal.", color = ClmfOnSurfaceMuted)
            }
        } else {
            items(epgPrograms) { program ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(program.startTimeMillis)),
                        color = ClmfOnSurfaceMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(program.title, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
