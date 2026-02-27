package apps.farm.data.repository

import apps.farm.data.dao.CycleDao
import apps.farm.data.model.Cycle
import kotlinx.coroutines.flow.Flow

class CycleRepository(private val cycleDao: CycleDao) {
    fun getCyclesByFarmId(farmId: String): Flow<List<Cycle>> {
        return cycleDao.getCyclesByFarmId(farmId)
    }

    fun getActiveCyclesByFarmId(farmId: String): Flow<List<Cycle>> {
        return cycleDao.getActiveCyclesByFarmId(farmId)
    }

    suspend fun getCycleById(id: String): Cycle? {
        return cycleDao.getCycleById(id)
    }

    suspend fun hasOverlappingCycles(farmId: String, startDate: Long, endDate: Long, excludeId: String = ""): Boolean {
        return cycleDao.countOverlappingCycles(farmId, startDate, endDate, excludeId) > 0
    }

    suspend fun insertCycle(cycle: Cycle) {
        cycleDao.insertCycle(cycle)
    }

    suspend fun updateCycle(cycle: Cycle) {
        cycleDao.updateCycle(cycle)
    }

    suspend fun toggleActiveStatus(cycleId: String, isActive: Boolean) {
        cycleDao.updateCycleStatus(cycleId, isActive)
    }
}
