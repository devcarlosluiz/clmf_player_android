package com.clmf.player.presentation.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.clmf.player.domain.model.ContentType
import com.clmf.player.domain.repository.ContentRepository
import com.clmf.player.domain.repository.SearchResults
import com.clmf.player.presentation.components.CLMFChannelCard
import com.clmf.player.presentation.components.CLMFPosterCard
import com.clmf.player.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val results: StateFlow<SearchResults> = _query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            kotlinx.coroutines.flow.flow { emit(contentRepository.search(q)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchResults())

    fun onQueryChange(value: String) { _query.value = value }
}

@Composable
fun SearchScreen(navController: NavHostController, viewModel: SearchViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Buscar conteúdo...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
                if (results.channels.isNotEmpty()) {
                    item { Text("TV", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp)) }
                    items(results.channels, key = { "c" + it.id }) { channel ->
                        CLMFChannelCard(
                            name = channel.name,
                            logoUrl = channel.logoUrl,
                            isFavorite = channel.isFavorite,
                            onClick = { navController.navigate(Routes.player(ContentType.LIVE.name, channel.id, channel.streamUrl, channel.name)) },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                if (results.movies.isNotEmpty()) {
                    item { Text("Filmes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp)) }
                    items(results.movies, key = { "m" + it.id }) { movie ->
                        CLMFPosterCard(
                            title = movie.name,
                            imageUrl = movie.posterUrl,
                            onClick = { navController.navigate(Routes.movieDetail(movie.id)) }
                        )
                    }
                }
                if (results.series.isNotEmpty()) {
                    item { Text("Séries", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp)) }
                    items(results.series, key = { "s" + it.id }) { series ->
                        CLMFPosterCard(
                            title = series.name,
                            imageUrl = series.posterUrl,
                            onClick = { navController.navigate(Routes.seriesDetail(series.id)) }
                        )
                    }
                }
            }
        }
    }
}
