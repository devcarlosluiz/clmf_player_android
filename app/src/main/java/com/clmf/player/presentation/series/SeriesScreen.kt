package com.clmf.player.presentation.series

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.clmf.player.domain.model.Series
import com.clmf.player.presentation.components.CLMFCategoryChip
import com.clmf.player.presentation.components.CLMFErrorView
import com.clmf.player.presentation.components.CLMFLoading
import com.clmf.player.presentation.components.CLMFPosterCard
import com.clmf.player.presentation.navigation.Routes
import com.clmf.player.presentation.theme.ClmfSurface

@Composable
fun SeriesScreen(navController: NavHostController, viewModel: SeriesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val onSeriesClick: (Series) -> Unit = { series -> navController.navigate(Routes.seriesDetail(series.id)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Séries") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            if (maxWidth < 700.dp) {
                SeriesCompactLayout(state = state, viewModel = viewModel, onSeriesClick = onSeriesClick)
            } else {
                SeriesExpandedLayout(state = state, viewModel = viewModel, onSeriesClick = onSeriesClick)
            }
        }
    }
}

@Composable
private fun SeriesCompactLayout(
    state: SeriesUiState,
    viewModel: SeriesViewModel,
    onSeriesClick: (Series) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = { Text("Buscar série...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
            item {
                CLMFCategoryChip("Todos", state.selectedCategoryId == null, { viewModel.selectCategory(null) }, Modifier.padding(end = 8.dp))
            }
            items(state.categories) { category ->
                CLMFCategoryChip(category.name, state.selectedCategoryId == category.id, { viewModel.selectCategory(category.id) }, Modifier.padding(end = 8.dp))
            }
        }

        if (state.series.isEmpty()) {
            CLMFErrorView(message = "Nenhuma série encontrada nesta lista.")
        } else {
            LazyVerticalGrid(columns = GridCells.Adaptive(130.dp), contentPadding = PaddingValues(16.dp)) {
                items(state.series, key = { it.id }) { series ->
                    CLMFPosterCard(
                        title = series.name,
                        imageUrl = series.posterUrl,
                        subtitle = series.year,
                        modifier = Modifier.padding(6.dp),
                        onClick = { onSeriesClick(series) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SeriesExpandedLayout(
    state: SeriesUiState,
    viewModel: SeriesViewModel,
    onSeriesClick: (Series) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left column: categories
        LazyColumn(
            modifier = Modifier.width(170.dp).fillMaxHeight().background(ClmfSurface),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                CLMFCategoryChip(
                    "Todos", state.selectedCategoryId == null,
                    { viewModel.selectCategory(null) },
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            items(state.categories, key = { it.id }) { category ->
                CLMFCategoryChip(
                    category.name, state.selectedCategoryId == category.id,
                    { viewModel.selectCategory(category.id) },
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Poster grid takes the remaining width
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Buscar série...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
            if (state.series.isEmpty()) {
                CLMFErrorView(message = "Nenhuma série encontrada nesta lista.")
            } else {
                LazyVerticalGrid(columns = GridCells.Adaptive(130.dp), contentPadding = PaddingValues(8.dp)) {
                    items(state.series, key = { it.id }) { series ->
                        CLMFPosterCard(
                            title = series.name,
                            imageUrl = series.posterUrl,
                            subtitle = series.year,
                            modifier = Modifier.padding(6.dp),
                            onClick = { onSeriesClick(series) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesDetailScreen(
    navController: NavHostController,
    seriesId: String,
    viewModel: SeriesDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(seriesId) { viewModel.load(seriesId) }
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.series?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favoritar"
                        )
                    }
                }
            )
        }
    ) { padding ->
        val series = state.series
        when {
            state.isLoading -> CLMFLoading(modifier = Modifier.padding(padding))
            series == null -> CLMFErrorView(
                message = state.errorMessage ?: "Série não encontrada.",
                modifier = Modifier.padding(padding)
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                coil.compose.AsyncImage(
                    model = series.posterUrl,
                    contentDescription = series.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(bottom = 16.dp)
                )
                Text(series.name, style = MaterialTheme.typography.titleLarge)
                series.description?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }

                Text("Temporadas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                if (state.seasons.isEmpty()) {
                    Text(state.errorMessage ?: "Nenhuma temporada disponível.")
                } else {
                    state.seasons.forEach { season ->
                        OutlinedButton(
                            onClick = { navController.navigate(Routes.episodes(seriesId, season)) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text("Temporada $season")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EpisodesScreen(
    navController: NavHostController,
    seriesId: String,
    seasonNumber: Int,
    viewModel: EpisodesViewModel = hiltViewModel()
) {
    LaunchedEffect(seriesId, seasonNumber) { viewModel.load(seriesId, seasonNumber) }
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Temporada $seasonNumber") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> CLMFLoading(modifier = Modifier.padding(padding))
            state.episodes.isEmpty() -> CLMFErrorView(
                message = state.errorMessage ?: "Nenhum episódio encontrado.",
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(state.episodes, key = { it.id }) { episode ->
                    androidx.compose.material3.ListItem(
                        headlineContent = { Text(episode.name) },
                        supportingContent = { episode.description?.let { Text(it, maxLines = 2) } },
                        modifier = Modifier.padding(bottom = 4.dp).clickable {
                            navController.navigate(
                                Routes.player(
                                    com.clmf.player.domain.model.ContentType.EPISODE.name,
                                    episode.id,
                                    episode.streamUrl,
                                    episode.name
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}
