package apps.farm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.R
import apps.farm.data.model.Cycle
import apps.farm.data.repository.CycleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CycleViewModel @Inject constructor(
    private val repository: CycleRepository,
    application: Application
) : AndroidViewModel(application) {

    fun getCyclesByFarmId(farmId: String): Flow<List<Cycle>> {
        return repository.getCyclesByFarmId(farmId)
    }

    fun getActiveCyclesByFarmId(farmId: String): Flow<List<Cycle>> {
        return repository.getActiveCyclesByFarmId(farmId)
    }

    suspend fun hasOverlappingCycles(farmId: String, startDate: Long, endDate: Long, excludeId: String = ""): Boolean {
        return repository.hasOverlappingCycles(farmId, startDate, endDate, excludeId)
    }

    fun insertCycle(cycle: Cycle, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        if (repository.hasOverlappingCycles(cycle.farmId, cycle.sd, cycle.ed)) {
            onResult(false, getApplication<Application>().getString(R.string.error_cycle_dates_overlap))
        } else {
            repository.insertCycle(cycle)
            onResult(true, getApplication<Application>().getString(R.string.success_add_cycle))
        }
    }

    fun updateCycle(cycle: Cycle, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        if (repository.hasOverlappingCycles(cycle.farmId, cycle.sd, cycle.ed, cycle.id)) {
            onResult(false, getApplication<Application>().getString(R.string.error_cycle_dates_overlap))
        } else {
            repository.updateCycle(cycle)
            onResult(true, getApplication<Application>().getString(R.string.success_update_cycle))
        }
    }

    fun toggleActiveStatus(cycleId: String, isActive: Boolean) = viewModelScope.launch {
        repository.toggleActiveStatus(cycleId, isActive)
    }

    suspend fun getCycleById(id: String): Cycle? {
        return repository.getCycleById(id)
    }
}
