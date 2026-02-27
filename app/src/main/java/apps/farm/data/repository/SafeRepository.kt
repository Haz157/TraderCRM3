package apps.farm.data.repository

import apps.farm.data.dao.SafeDao
import apps.farm.data.model.Safe
import kotlinx.coroutines.flow.Flow

class SafeRepository(private val safeDao: SafeDao) {
    val activeSafes: Flow<List<Safe>> = safeDao.getAllActiveSafes()
    val allSafes: Flow<List<Safe>> = safeDao.getAllSafes()
    
    suspend fun getSafeById(id: String): Safe? = safeDao.getSafeById(id)
    suspend fun getSafeByName(name: String, excludeId: String = ""): Safe? = 
        safeDao.getSafeByName(name, excludeId)
    
    suspend fun insertSafe(safe: Safe) = safeDao.insertSafe(safe)
    suspend fun updateSafe(safe: Safe) = safeDao.updateSafe(safe)
    
    suspend fun blockSafe(id: String) = safeDao.blockSafe(id)
    suspend fun unblockSafe(id: String) = safeDao.unblockSafe(id)
    
    suspend fun isSafeNameUnique(name: String, excludeId: String = ""): Boolean {
        return getSafeByName(name, excludeId) == null
    }
}
