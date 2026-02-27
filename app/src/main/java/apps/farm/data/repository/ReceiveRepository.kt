package apps.farm.data.repository

import apps.farm.data.dao.ReceiveDao
import apps.farm.data.model.Receive
import kotlinx.coroutines.flow.Flow

class ReceiveRepository(private val receiveDao: ReceiveDao) {
    fun getAllActiveReceives(): Flow<List<Receive>> = receiveDao.getAllActiveReceives()
    fun getAllReceives(): Flow<List<Receive>> = receiveDao.getAllReceives()
    
    suspend fun getReceiveById(id: String): Receive? = receiveDao.getReceiveById(id)
    fun getReceivesByCustomer(customerId: String): Flow<List<Receive>> = 
        receiveDao.getReceivesByCustomer(customerId)
    
    suspend fun getTotalReceiveByCustomer(customerId: String): Double? = 
        receiveDao.getTotalReceiveByCustomer(customerId)
    
    suspend fun insertReceive(receive: Receive) = receiveDao.insertReceive(receive)
    suspend fun updateReceive(receive: Receive) = receiveDao.updateReceive(receive)
    
    suspend fun blockReceive(id: String) = receiveDao.blockReceive(id)
    suspend fun unblockReceive(id: String) = receiveDao.unblockReceive(id)
}
