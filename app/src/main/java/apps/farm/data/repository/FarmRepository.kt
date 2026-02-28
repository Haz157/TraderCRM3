package apps.farm.data.repository

import apps.farm.data.dao.FarmDao
import apps.farm.data.model.Farm
import apps.farm.utils.BackupManager
import kotlinx.coroutines.flow.Flow

class FarmRepository(
    private val farmDao: FarmDao,
    private val backupManager: BackupManager
) {
    val allFarms: Flow<List<Farm>> = farmDao.getAllFarms()
    val activeFarms: Flow<List<Farm>> = farmDao.getActiveFarms()

    suspend fun getFarmById(id: String): Farm? {
        return farmDao.getFarmById(id)
    }

    suspend fun insertFarm(farm: Farm) {
        farmDao.insertFarm(farm)
        backupManager.scheduleBackup()
    }

    suspend fun updateFarm(farm: Farm) {
        farmDao.updateFarm(farm)
        backupManager.scheduleBackup()
    }

    suspend fun toggleBlockStatus(farmId: String, blocked: Boolean) {
        farmDao.updateFarmStatus(farmId, blocked)
        backupManager.scheduleBackup()
    }
}
