package apps.farm.data.dao

import androidx.room.*
import apps.farm.data.model.Receive
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiveDao {
    @Query("SELECT * FROM receivetbl WHERE isBlocked = 0 ORDER BY createdDate DESC")
    fun getAllActiveReceives(): Flow<List<Receive>>

    @Query("SELECT * FROM receivetbl ORDER BY createdDate DESC")
    fun getAllReceives(): Flow<List<Receive>>

    @Query("SELECT * FROM receivetbl WHERE id = :id")
    suspend fun getReceiveById(id: String): Receive?

    @Query("SELECT * FROM receivetbl WHERE customerId = :customerId AND isBlocked = 0 ORDER BY createdDate DESC")
    fun getReceivesByCustomer(customerId: String): Flow<List<Receive>>

    @Query("SELECT SUM(receive - discount) FROM receivetbl WHERE customerId = :customerId AND isBlocked = 0")
    suspend fun getTotalReceiveByCustomer(customerId: String): Double?

    @Insert
    suspend fun insertReceive(receive: Receive): Long

    @Update
    suspend fun updateReceive(receive: Receive)


    @Query("UPDATE receivetbl SET isBlocked = 1 WHERE id = :id")
    suspend fun blockReceive(id: String)

    @Query("UPDATE receivetbl SET isBlocked = 0 WHERE id = :id")
    suspend fun unblockReceive(id: String)
}
