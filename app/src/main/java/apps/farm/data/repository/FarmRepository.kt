package apps.farm.data.repository

import apps.farm.data.dao.FarmDao
import apps.farm.data.model.Farm
import kotlinx.coroutines.flow.Flow

class FarmRepository(private val farmDao: FarmDao) {
    val allFarms: Flow<List<Farm>> = farmDao.getAllFarms()
    val activeFarms: Flow<List<Farm>> = farmDao.getActiveFarms()

    suspend fun getFarmById(id: String): Farm? {
        return farmDao.getFarmById(id)
    }

    suspend fun insertFarm(farm: Farm) {
        farmDao.insertFarm(farm)
    }

    suspend fun updateFarm(farm: Farm) {
        farmDao.updateFarm(farm)
    }

    suspend fun toggleBlockStatus(farmId: String, blocked: Boolean) {
        farmDao.updateFarmStatus(farmId, blocked)
    }
}
