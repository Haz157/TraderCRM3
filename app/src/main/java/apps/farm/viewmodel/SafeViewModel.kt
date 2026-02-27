package apps.farm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.R
import apps.farm.data.model.Safe
import apps.farm.data.repository.SafeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SafeViewModel @Inject constructor(
    private val safeRepository: SafeRepository,
    application: Application
) : AndroidViewModel(application) {
    
    private val _allSafes = MutableStateFlow<List<Safe>>(emptyList())
    val allSafes: StateFlow<List<Safe>> = _allSafes.asStateFlow()
    
    private val _activeSafes = MutableStateFlow<List<Safe>>(emptyList())
    val activeSafes: StateFlow<List<Safe>> = _activeSafes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadSafes()
    }
    
    private fun loadSafes() {
        viewModelScope.launch {
            safeRepository.allSafes.collect { safes ->
                _allSafes.value = safes
            }
        }
        viewModelScope.launch {
            safeRepository.activeSafes.collect { safes ->
                _activeSafes.value = safes
            }
        }
    }
    
    fun insertSafe(safe: Safe, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                if (!safeRepository.isSafeNameUnique(safe.name)) {
                    onResult(false, getApplication<Application>().getString(R.string.error_safe_name_exists_ar))
                    return@launch
                }
                safeRepository.insertSafe(safe)
                onResult(true, getApplication<Application>().getString(R.string.success_add_safe))
            } catch (e: Exception) {
                onResult(false, getApplication<Application>().getString(R.string.error_add_safe, e.message))
            }
        }
    }
    
    fun updateSafe(safe: Safe, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                if (!safeRepository.isSafeNameUnique(safe.name, safe.id)) {
                    onResult(false, getApplication<Application>().getString(R.string.error_safe_name_exists_ar))
                    return@launch
                }
                safeRepository.updateSafe(safe)
                onResult(true, getApplication<Application>().getString(R.string.success_update_safe))
            } catch (e: Exception) {
                onResult(false, getApplication<Application>().getString(R.string.error_update_safe, e.message))
            }
        }
    }
    
    fun toggleBlockStatus(id: String, isCurrentlyBlocked: Boolean) {
        viewModelScope.launch {
            try {
                if (isCurrentlyBlocked) {
                    safeRepository.unblockSafe(id)
                } else {
                    safeRepository.blockSafe(id)
                }
            } catch (e: Exception) {
                _errorMessage.value = getApplication<Application>().getString(R.string.error_update_safe_status, e.message)
            }
        }
    }
    
    suspend fun getSafeById(id: String): Safe? {
        return safeRepository.getSafeById(id)
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
