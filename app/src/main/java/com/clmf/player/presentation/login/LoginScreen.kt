package com.clmf.player.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.clmf.player.domain.model.ConnectionType
import com.clmf.player.presentation.components.CLMFButton
import com.clmf.player.presentation.components.CLMFCategoryChip
import com.clmf.player.presentation.navigation.Routes
import com.clmf.player.presentation.theme.ClmfAccent
import com.clmf.player.presentation.theme.ClmfError

@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.connected) {
        if (state.connected) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("CLMF Player", style = MaterialTheme.typography.titleLarge, color = ClmfAccent)
        Text("Conecte-se via Xtream Codes ou lista M3U", style = MaterialTheme.typography.bodyMedium)

        androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))

        ConnectionFormFields(state = state, viewModel = viewModel)

        androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator(color = ClmfAccent)
        } else {
            CLMFButton(
                text = "CONECTAR",
                onClick = viewModel::connect,
                enabled = state.isFormValid,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ConnectionFormFields(state: LoginUiState, viewModel: LoginViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        CLMFCategoryChip(
            name = "Xtream Codes",
            selected = state.type == ConnectionType.XTREAM,
            onClick = { viewModel.onTypeChange(ConnectionType.XTREAM) },
            modifier = Modifier.padding(end = 8.dp)
        )
        CLMFCategoryChip(
            name = "Lista M3U (URL)",
            selected = state.type == ConnectionType.M3U,
            onClick = { viewModel.onTypeChange(ConnectionType.M3U) }
        )
    }
    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = state.name,
        onValueChange = viewModel::onNameChange,
        label = { Text("Nome da conexão") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))

    if (state.type == ConnectionType.XTREAM) {
        OutlinedTextField(
            value = state.serverUrl,
            onValueChange = viewModel::onServerChange,
            label = { Text("Servidor / URL") },
            placeholder = { Text("http://servidor.com:8080") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth()
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.username,
            onValueChange = viewModel::onUsernameChange,
            label = { Text("Usuário") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Senha") },
            singleLine = true,
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(
                        imageVector = if (state.passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Mostrar/ocultar senha"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        OutlinedTextField(
            value = state.playlistUrl,
            onValueChange = viewModel::onPlaylistUrlChange,
            label = { Text("URL da lista M3U") },
            placeholder = { Text("http://servidor.com/lista.m3u") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (state.errorMessage != null) {
        androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
        Text(state.errorMessage.orEmpty(), color = ClmfError, style = MaterialTheme.typography.bodyMedium)
    }
}
