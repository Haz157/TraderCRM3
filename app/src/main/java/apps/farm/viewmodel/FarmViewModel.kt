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
    private val repository: FarmRepository
) : ViewModel() {
    val allFarms: Flow<List<Farm>> = repository.allFarms
    val activeFarms: Flow<List<Farm>> = repository.activeFarms

    fun insertFarm(farm: Farm) = viewModelScope.launch {
        repository.insertFarm(farm)
    }

    fun updateFarm(farm: Farm) = viewModelScope.launch {
        repository.updateFarm(farm)
    }

    fun toggleBlockStatus(farmId: String, blocked: Boolean) = viewModelScope.launch {
        repository.toggleBlockStatus(farmId, blocked)
    }

    suspend fun getFarmById(id: String): Farm? {
        return repository.getFarmById(id)
    }
}
