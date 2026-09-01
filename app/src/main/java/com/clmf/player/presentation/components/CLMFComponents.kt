package com.clmf.player.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.clmf.player.presentation.theme.ClmfAccent
import com.clmf.player.presentation.theme.ClmfOnSurfaceMuted
import com.clmf.player.presentation.theme.ClmfSurface
import com.clmf.player.presentation.theme.ClmfSurfaceVariant

@Composable
fun CLMFButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = modifier
    ) {
        Text(text)
    }
}

@Composable
fun CLMFLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = ClmfAccent)
    }
}

@Composable
fun CLMFErrorView(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = MaterialTheme.colorScheme.onSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (onRetry != null) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            CLMFButton(text = "Tentar novamente", onClick = onRetry)
        }
    }
}

@Composable
fun CLMFPosterCard(
    title: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    progress: Float? = null,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .width(130.dp)
            .clickable(onClick = onClick)
            .onFocusChanged { focused = it.isFocused }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(10.dp))
                .background(ClmfSurfaceVariant)
                .then(if (focused) Modifier.border(2.dp, ClmfAccent, RoundedCornerShape(10.dp)) else Modifier)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (progress != null && progress > 0f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    color = ClmfAccent,
                    trackColor = Color.Black.copy(alpha = 0.4f)
                )
            }
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(6.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ClmfOnSurfaceMuted, maxLines = 1)
        }
    }
}

@Composable
fun CLMFChannelCard(
    name: String,
    logoUrl: String?,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit,
    onToggleFavorite: (() -> Unit)? = null
) {
    var focused by remember { mutableStateOf(false) }
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (focused) ClmfSurfaceVariant else ClmfSurface)
            .then(if (focused) Modifier.border(1.dp, ClmfAccent, RoundedCornerShape(10.dp)) else Modifier)
            .clickable(onClick = onClick)
            .onFocusChanged { focused = it.isFocused }
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(ClmfSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(model = logoUrl, contentDescription = name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ClmfOnSurfaceMuted, maxLines = 1)
        }
        if (onToggleFavorite != null) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favorito",
                tint = if (isFavorite) ClmfAccent else ClmfOnSurfaceMuted,
                modifier = Modifier.clickable { onToggleFavorite() }
            )
        }
    }
}

@Composable
fun CLMFCategoryChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else ClmfSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(name, color = if (selected) Color.White else ClmfOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun CLMFPlayFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(ClmfAccent, MaterialTheme.colorScheme.primary)))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black)
            androidx.compose.foundation.layout.Spacer(Modifier.width(6.dp))
            Text("ASSISTIR", color = Color.Black, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
