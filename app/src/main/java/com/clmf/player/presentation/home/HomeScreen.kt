package com.clmf.player.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.clmf.player.presentation.navigation.Routes
import com.clmf.player.presentation.theme.ClmfAccent
import com.clmf.player.presentation.theme.ClmfBackground
import com.clmf.player.presentation.theme.ClmfOnSurfaceMuted
import com.clmf.player.presentation.theme.ClmfTileLive
import com.clmf.player.presentation.theme.ClmfTileMovies
import com.clmf.player.presentation.theme.ClmfTileSeries
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var now by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(30_000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClmfBackground)
    ) {
        // Top bar: brand + clock on the left, quick actions on the right.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(com.clmf.player.R.drawable.ic_clmf_logo),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
            Text(
                SimpleDateFormat("HH:mm, dd MMM yyyy", Locale("pt", "BR")).format(now),
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = Color.White)
            }
            IconButton(onClick = { navController.navigate(Routes.FAVORITES) }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Conta / Favoritos", tint = Color.White)
            }
            IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                Icon(Icons.Filled.Settings, contentDescription = "Configurações", tint = Color.White)
            }
            IconButton(onClick = viewModel::refresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Atualizar", tint = Color.White)
            }
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                HomeTile(
                    title = "TV ao vivo",
                    icon = Icons.Filled.LiveTv,
                    color = ClmfTileLive,
                    onClick = { navController.navigate(Routes.LIVE_TV) }
                )
                HomeTile(
                    title = "Filmes",
                    icon = Icons.Filled.Movie,
                    color = ClmfTileMovies,
                    onClick = { navController.navigate(Routes.MOVIES) }
                )
                HomeTile(
                    title = "Séries",
                    icon = Icons.Filled.Tv,
                    color = ClmfTileSeries,
                    onClick = { navController.navigate(Routes.SERIES) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Expiração: ${state.expirationDate ?: "—"}",
                color = ClmfOnSurfaceMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Versão: ${state.versionName}",
                color = ClmfOnSurfaceMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HomeTile(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    var focused by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .width(180.dp)
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(listOf(color, color.copy(alpha = 0.65f)))
            )
            .onFocusChanged { focused = it.isFocused }
            .then(
                if (focused) Modifier.border(3.dp, Color.White, RoundedCornerShape(18.dp))
                else Modifier
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (enabled) 0.92f else 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(36.dp))
        }
    }
}
