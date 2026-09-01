package com.clmf.player.presentation.license

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.clmf.player.data.local.LicenseStatus
import com.clmf.player.presentation.components.CLMFButton
import com.clmf.player.presentation.navigation.Routes
import com.clmf.player.presentation.theme.ClmfAccent
import com.clmf.player.presentation.theme.ClmfBackground
import com.clmf.player.presentation.theme.ClmfError
import com.clmf.player.presentation.theme.ClmfOnSurfaceMuted

@Composable
fun LicenseScreen(navController: NavHostController, viewModel: LicenseViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.activated) {
        if (state.activated) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LICENSE) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClmfBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("CLMF Player", style = MaterialTheme.typography.titleLarge, color = ClmfAccent)

        when (val status = state.status) {
            is LicenseStatus.Trial -> {
                Text(
                    "Período de avaliação: ${status.daysRemaining} dia(s) restante(s)",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            LicenseStatus.Expired -> {
                Text(
                    "Seu período de avaliação de 7 dias terminou. Insira uma chave de ativação para continuar.",
                    color = ClmfError,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            LicenseStatus.Licensed -> {
                Text("Aplicativo licenciado.", modifier = Modifier.padding(top = 8.dp))
            }
        }

        Divider(modifier = Modifier.padding(vertical = 24.dp))

        Text("Identificação do aparelho", style = MaterialTheme.typography.titleMedium)
        Text(
            "Informe estes dados a quem vendeu o aplicativo para receber sua chave de ativação.",
            color = ClmfOnSurfaceMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        LabeledValue(label = "ID do dispositivo", value = state.deviceId)

        if (state.status !is LicenseStatus.Licensed) {
            Divider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Ativar chave de licença", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.keyInput,
                onValueChange = viewModel::onKeyChange,
                label = { Text("Chave de ativação") },
                placeholder = { Text("XXXX-XXXX-XXXX-XXXX") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            state.activationError?.let {
                Text(it, color = ClmfError, modifier = Modifier.padding(top = 8.dp))
            }

            CLMFButton(
                text = "ATIVAR",
                onClick = viewModel::activate,
                enabled = state.keyInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            if (state.status is LicenseStatus.Trial) {
                CLMFButton(
                    text = "CONTINUAR AVALIAÇÃO",
                    onClick = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LICENSE) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String, note: String? = null) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = ClmfOnSurfaceMuted)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        note?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = ClmfOnSurfaceMuted, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
