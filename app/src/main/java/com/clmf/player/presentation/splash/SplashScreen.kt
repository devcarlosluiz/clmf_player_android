package com.clmf.player.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.clmf.player.data.local.LicenseManager
import com.clmf.player.data.local.LicenseStatus
import com.clmf.player.domain.repository.ConnectionRepository
import com.clmf.player.presentation.navigation.Routes
import com.clmf.player.presentation.theme.ClmfBackground
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashResult(val hasConnection: Boolean, val licenseExpired: Boolean)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val licenseManager: LicenseManager
) : ViewModel() {

    private val _result = MutableStateFlow<SplashResult?>(null)
    val result: StateFlow<SplashResult?> = _result.asStateFlow()

    init {
        viewModelScope.launch {
            licenseManager.ensureTrialStarted()
            val status = licenseManager.status.first()
            _result.value = SplashResult(
                hasConnection = connectionRepository.getSelectedConnection() != null,
                licenseExpired = status is LicenseStatus.Expired
            )
        }
    }
}

@Composable
fun SplashScreen(navController: NavHostController, viewModel: SplashViewModel = hiltViewModel()) {
    val result by viewModel.result.collectAsState()

    LaunchedEffect(result) {
        val current = result ?: return@LaunchedEffect
        delay(2200)
        val destination = when {
            current.licenseExpired -> Routes.LICENSE
            current.hasConnection -> Routes.HOME
            else -> Routes.LOGIN
        }
        navController.navigate(destination) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(ClmfBackground).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(com.clmf.player.R.drawable.ic_clmf_logo),
            contentDescription = null,
            modifier = Modifier.height(200.dp)
        )
    }
}
