package com.clmf.player.presentation.license

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clmf.player.data.local.LicenseManager
import com.clmf.player.data.local.LicenseStatus
import com.clmf.player.utils.DeviceIdentifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LicenseUiState(
    val deviceId: String = "",
    val macAddress: String = "",
    val isMacRestricted: Boolean = false,
    val keyInput: String = "",
    val status: LicenseStatus = LicenseStatus.Expired,
    val activationError: String? = null,
    val activated: Boolean = false
)

@HiltViewModel
class LicenseViewModel @Inject constructor(
    private val licenseManager: LicenseManager,
    private val deviceIdentifier: DeviceIdentifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LicenseUiState(
            deviceId = licenseManager.deviceId(),
            macAddress = deviceIdentifier.macAddress(),
            isMacRestricted = deviceIdentifier.isMacRestricted()
        )
    )
    val uiState: StateFlow<LicenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            licenseManager.ensureTrialStarted()
            licenseManager.status.collect { status ->
                _uiState.value = _uiState.value.copy(status = status)
            }
        }
    }

    fun onKeyChange(value: String) {
        _uiState.value = _uiState.value.copy(keyInput = value, activationError = null)
    }

    fun activate() {
        viewModelScope.launch {
            val success = licenseManager.activate(_uiState.value.keyInput)
            if (success) {
                _uiState.value = _uiState.value.copy(activated = true, activationError = null)
            } else {
                _uiState.value = _uiState.value.copy(
                    activationError = "Chave inválida para este aparelho. Confira o ID do dispositivo."
                )
            }
        }
    }
}
