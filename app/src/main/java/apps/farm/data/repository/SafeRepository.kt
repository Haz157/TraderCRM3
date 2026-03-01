package apps.farm.data.repository

import apps.farm.data.dao.SafeDao
import apps.farm.data.model.Safe
import apps.farm.utils.BackupManager
import kotlinx.coroutines.flow.Flow

class SafeRepository(
    private val safeDao: apps.farm.data.dao.SafeDao,
    private val saleInvoiceDao: apps.farm.data.dao.SaleInvoiceDao,
    private val receiveDao: apps.farm.data.dao.ReceiveDao,
    private val backupManager: BackupManager
) {
    val activeSafes: Flow<List<Safe>> = safeDao.getAllActiveSafes()
    val allSafes: Flow<List<Safe>> = safeDao.getAllSafes()
    
    suspend fun getSafeById(id: String): Safe? = safeDao.getSafeById(id)
    suspend fun getSafeByName(name: String, excludeId: String = ""): Safe? = 
        safeDao.getSafeByName(name, excludeId)
    
    suspend fun insertSafe(safe: Safe) {
        safeDao.insertSafe(safe)
        backupManager.scheduleBackup()
    }

    suspend fun updateSafe(safe: Safe) {
        safeDao.updateSafe(safe)
        backupManager.scheduleBackup()
    }
    
    suspend fun blockSafe(id: String) {
        safeDao.blockSafe(id)
        backupManager.scheduleBackup()
    }

    suspend fun unblockSafe(id: String) {
        safeDao.unblockSafe(id)
        backupManager.scheduleBackup()
    }
    
    suspend fun isSafeNameUnique(name: String, excludeId: String = ""): Boolean {
        return getSafeByName(name, excludeId) == null
    }

    suspend fun getAllSafesSync(): List<Safe> = safeDao.getAllSafesSync()
    suspend fun insertSafes(safes: List<Safe>) = safeDao.insertSafes(safes)

    suspend fun deleteSafe(safe: Safe) {
        safeDao.deleteSafe(safe)
        backupManager.scheduleBackup()
    }

    suspend fun deleteSafeCompletely(safe: Safe) {
        saleInvoiceDao.deleteEmptyWeightsBySafeId(safe.id)
        saleInvoiceDao.deleteGrossWeightsBySafeId(safe.id)
        saleInvoiceDao.deleteInvoicesBySafeId(safe.id)
        receiveDao.deleteReceivesBySafeId(safe.id)
        safeDao.deleteSafe(safe)
        backupManager.scheduleBackup()
    }

    suspend fun deleteAllSafes() {
        saleInvoiceDao.deleteAllEmptyWeights()
        saleInvoiceDao.deleteAllGrossWeights()
        saleInvoiceDao.deleteAllInvoices()
        saleInvoiceDao.deleteAllReceives() // Using SaleInvoiceDao's copy since it exists there
        safeDao.deleteAllSafes()
        backupManager.scheduleBackup()
    }
}
