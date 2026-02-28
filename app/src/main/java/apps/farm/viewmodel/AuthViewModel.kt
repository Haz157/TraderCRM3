package apps.farm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val isAuthEnabled = authRepository.isAuthEnabled
    val useBiometric = authRepository.useBiometric
    val backupEmail = authRepository.backupEmail
    val isAutoBackupEnabled = authRepository.isAutoBackupEnabled

    // Trial and Activation
    private val ACTIVATION_KEY = "FARM-PRO-2026-HTS" // Shared activation key
    val isActivated = authRepository.isActivated
    val firstOpenDate = authRepository.firstOpenDate

    private val _trialExpired = MutableStateFlow(false)
    val trialExpired: StateFlow<Boolean> = _trialExpired.asStateFlow()

    init {
        checkTrialStatus()
    }

    private fun checkTrialStatus() {
        viewModelScope.launch {
            val firstOpen = authRepository.firstOpenDate.first()
            if (firstOpen == null) {
                authRepository.setFirstOpenDate(System.currentTimeMillis())
            } else {
                val activated = authRepository.isActivated.first()
                if (!activated) {
                    val threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L
                    if (System.currentTimeMillis() - firstOpen > threeDaysInMillis) {
                        _trialExpired.value = true
                    }
                }
            }
        }
    }

    fun verifyActivationKey(key: String): Boolean {
        return if (key == ACTIVATION_KEY) {
            viewModelScope.launch {
                authRepository.setActivated(true)
                _trialExpired.value = false
            }
            true
        } else {
            false
        }
    }

    fun checkPin(inputPin: String) {
        viewModelScope.launch {
            val savedPin = authRepository.savedPin.first()
            if (savedPin == inputPin) {
                _isAuthorized.value = true
                _authError.value = null
            } else {
                _authError.value = "خطأ في الرقم السري"
            }
        }
    }

    fun onBiometricSuccess() {
        _isAuthorized.value = true
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            authRepository.setPin(pin)
        }
    }

    fun setAuthEnabled(enabled: Boolean) {
        viewModelScope.launch {
            authRepository.setAuthEnabled(enabled)
        }
    }

    fun setUseBiometric(use: Boolean) {
        viewModelScope.launch {
            authRepository.setUseBiometric(use)
        }
    }

    fun setBackupEmail(email: String) {
        viewModelScope.launch {
            authRepository.setBackupEmail(email)
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            authRepository.setAutoBackupEnabled(enabled)
        }
    }

    fun resetAuthState() {
        _isAuthorized.value = false
        _authError.value = null
    }
}
