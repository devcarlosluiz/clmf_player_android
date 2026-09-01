package com.clmf.player.presentation.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.clmf.player.domain.model.ContentType
import com.clmf.player.domain.model.Movie
import com.clmf.player.presentation.components.CLMFCategoryChip
import com.clmf.player.presentation.components.CLMFErrorView
import com.clmf.player.presentation.components.CLMFLoading
import com.clmf.player.presentation.components.CLMFPlayFab
import com.clmf.player.presentation.components.CLMFPosterCard
import com.clmf.player.presentation.navigation.Routes
import com.clmf.player.presentation.theme.ClmfSurface

@Composable
fun MoviesScreen(navController: NavHostController, viewModel: MoviesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val onMovieClick: (Movie) -> Unit = { movie -> navController.navigate(Routes.movieDetail(movie.id)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filmes") },
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
                MoviesCompactLayout(state = state, viewModel = viewModel, onMovieClick = onMovieClick)
            } else {
                MoviesExpandedLayout(state = state, viewModel = viewModel, onMovieClick = onMovieClick)
            }
        }
    }
}

@Composable
private fun MoviesCompactLayout(
    state: MoviesUiState,
    viewModel: MoviesViewModel,
    onMovieClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = { Text("Buscar filme...") },
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

        if (state.movies.isEmpty()) {
            CLMFErrorView(message = "Nenhum filme encontrado nesta lista.")
        } else {
            LazyVerticalGrid(columns = GridCells.Adaptive(130.dp), contentPadding = PaddingValues(16.dp)) {
                items(state.movies, key = { it.id }) { movie ->
                    CLMFPosterCard(
                        title = movie.name,
                        imageUrl = movie.posterUrl,
                        subtitle = movie.year,
                        modifier = Modifier.padding(6.dp),
                        onClick = { onMovieClick(movie) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MoviesExpandedLayout(
    state: MoviesUiState,
    viewModel: MoviesViewModel,
    onMovieClick: (Movie) -> Unit
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
                placeholder = { Text("Buscar filme...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
            if (state.movies.isEmpty()) {
                CLMFErrorView(message = "Nenhum filme encontrado nesta lista.")
            } else {
                LazyVerticalGrid(columns = GridCells.Adaptive(130.dp), contentPadding = PaddingValues(8.dp)) {
                    items(state.movies, key = { it.id }) { movie ->
                        CLMFPosterCard(
                            title = movie.name,
                            imageUrl = movie.posterUrl,
                            subtitle = movie.year,
                            modifier = Modifier.padding(6.dp),
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieDetailScreen(
    navController: NavHostController,
    movieId: String,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(movieId) { viewModel.load(movieId) }
    val movie by viewModel.movie.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(movie?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favoritar"
                        )
                    }
                }
            )
        }
    ) { padding ->
        val currentMovie = movie
        if (currentMovie == null) {
            CLMFLoading(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            coil.compose.AsyncImage(
                model = currentMovie.posterUrl,
                contentDescription = currentMovie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(bottom = 16.dp)
            )
            Text(currentMovie.name, style = MaterialTheme.typography.titleLarge)
            currentMovie.description?.let {
                Text(it, modifier = Modifier.padding(top = 8.dp))
            }
            Row(modifier = Modifier.padding(top = 8.dp)) {
                currentMovie.year?.let { Text("Ano: $it  ") }
                currentMovie.genre?.let { Text("Gênero: $it") }
            }
            Spacer(Modifier.height(16.dp))
            CLMFPlayFab(
                onClick = {
                    navController.navigate(
                        Routes.player(ContentType.MOVIE.name, currentMovie.id, currentMovie.streamUrl, currentMovie.name)
                    )
                },
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
