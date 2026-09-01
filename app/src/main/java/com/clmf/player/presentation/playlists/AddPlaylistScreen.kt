package com.clmf.player.presentation.playlists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.clmf.player.presentation.components.CLMFButton
import com.clmf.player.presentation.login.ConnectionFormFields
import com.clmf.player.presentation.login.LoginViewModel
import com.clmf.player.presentation.theme.ClmfAccent

@Composable
fun AddPlaylistScreen(navController: NavHostController, viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.connected) {
        if (state.connected) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova playlist") },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            ConnectionFormFields(state = state, viewModel = viewModel)

            androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator(color = ClmfAccent)
            } else {
                CLMFButton(
                    text = "ADICIONAR",
                    onClick = viewModel::connect,
                    enabled = state.isFormValid,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
