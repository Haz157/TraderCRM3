package apps.farm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.data.model.Farm
import apps.farm.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmViewModel @Inject constructor(
    private val repository: FarmRepository,
    application: android.app.Application
) : androidx.lifecycle.AndroidViewModel(application) {
    val allFarms: Flow<List<Farm>> = repository.allFarms
    val activeFarms: Flow<List<Farm>> = repository.activeFarms

    fun insertFarm(farm: Farm, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        try {
            repository.insertFarm(farm)
            onResult(true, getApplication<android.app.Application>().getString(apps.farm.R.string.success_add_farm))
        } catch (e: Exception) {
            onResult(false, getApplication<android.app.Application>().getString(apps.farm.R.string.error_add_farm, e.message))
        }
    }

    fun updateFarm(farm: Farm, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        try {
            repository.updateFarm(farm)
            onResult(true, getApplication<android.app.Application>().getString(apps.farm.R.string.success_update_farm))
        } catch (e: Exception) {
            onResult(false, getApplication<android.app.Application>().getString(apps.farm.R.string.error_update_farm, e.message))
        }
    }

    fun toggleBlockStatus(farmId: String, blocked: Boolean) = viewModelScope.launch {
        repository.toggleBlockStatus(farmId, blocked)
    }

    suspend fun getFarmById(id: String): Farm? {
        return repository.getFarmById(id)
    }
}
