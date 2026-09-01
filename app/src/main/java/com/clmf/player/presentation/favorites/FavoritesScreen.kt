package com.clmf.player.presentation.favorites

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.clmf.player.domain.model.ContentType
import com.clmf.player.domain.model.FavoriteItem
import com.clmf.player.domain.repository.FavoritesRepository
import com.clmf.player.presentation.components.CLMFErrorView
import com.clmf.player.presentation.components.CLMFPosterCard
import com.clmf.player.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    favoritesRepository: FavoritesRepository
) : ViewModel() {
    val favorites: StateFlow<List<FavoriteItem>> = favoritesRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun FavoritesScreen(navController: NavHostController, viewModel: FavoritesViewModel = hiltViewModel()) {
    val favorites by viewModel.favorites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoritos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            CLMFErrorView(message = "Você ainda não adicionou favoritos.", modifier = Modifier.padding(padding))
        } else {
            LazyVerticalGrid(columns = GridCells.Adaptive(130.dp), contentPadding = PaddingValues(16.dp), modifier = Modifier.padding(padding)) {
                items(favorites, key = { it.contentId + it.contentType.name }) { item ->
                    CLMFPosterCard(
                        title = item.name,
                        imageUrl = item.imageUrl,
                        modifier = Modifier.padding(6.dp),
                        onClick = {
                            val destination = when (item.contentType) {
                                ContentType.MOVIE -> Routes.movieDetail(item.contentId)
                                ContentType.SERIES -> Routes.seriesDetail(item.contentId)
                                ContentType.LIVE -> Routes.LIVE_TV
                                ContentType.EPISODE -> Routes.LIVE_TV
                            }
                            navController.navigate(destination)
                        }
                    )
                }
            }
        }
    }
}
