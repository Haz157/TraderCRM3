package apps.farm.data.repository

import apps.farm.data.dao.ReceiveDao
import apps.farm.data.model.Receive
import apps.farm.utils.BackupManager
import kotlinx.coroutines.flow.Flow

class ReceiveRepository(
    private val receiveDao: ReceiveDao,
    private val backupManager: BackupManager
) {
    fun getAllActiveReceives(): Flow<List<Receive>> = receiveDao.getAllActiveReceives()
    fun getAllReceives(): Flow<List<Receive>> = receiveDao.getAllReceives()
    
    suspend fun getReceiveById(id: String): Receive? = receiveDao.getReceiveById(id)
    fun getReceivesByCustomer(customerId: String): Flow<List<Receive>> = 
        receiveDao.getReceivesByCustomer(customerId)
    
    suspend fun getReceivesByCustomerAndDateRange(customerId: String, startDate: Long, endDate: Long): List<Receive> =
        receiveDao.getReceivesByCustomerAndDateRange(customerId, startDate, endDate)
    
    suspend fun getTotalReceiveByCustomer(customerId: String): Double? = 
        receiveDao.getTotalReceiveByCustomer(customerId)
    
    suspend fun getReceivesByDateRange(startDate: Long, endDate: Long): List<Receive> =
        receiveDao.getReceivesByDateRange(startDate, endDate)
    
    suspend fun insertReceive(receive: Receive) {
        receiveDao.insertReceive(receive)
        backupManager.scheduleBackup()
    }

    suspend fun updateReceive(receive: Receive) {
        receiveDao.updateReceive(receive)
        backupManager.scheduleBackup()
    }

    suspend fun deleteReceive(receive: Receive) {
        receiveDao.deleteReceive(receive)
        backupManager.scheduleBackup()
    }
    
    suspend fun blockReceive(id: String) {
        receiveDao.blockReceive(id)
        backupManager.scheduleBackup()
    }

    suspend fun unblockReceive(id: String) {
        receiveDao.unblockReceive(id)
        backupManager.scheduleBackup()
    }

    suspend fun getMaxReceiveNo(): Int = receiveDao.getMaxReceiveNo() ?: 0

    suspend fun getAllReceivesSync(): List<Receive> = receiveDao.getAllReceivesSync()
    suspend fun insertReceives(receives: List<Receive>) = receiveDao.insertReceives(receives)
}
