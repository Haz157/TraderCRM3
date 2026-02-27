package apps.farm.data.dao

import androidx.room.*
import apps.farm.data.model.Safe
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeDao {
    @Query("SELECT * FROM safetbl WHERE blocked = 0 ORDER BY name ASC")
    fun getAllActiveSafes(): Flow<List<Safe>>

    @Query("SELECT * FROM safetbl ORDER BY name ASC")
    fun getAllSafes(): Flow<List<Safe>>

    @Query("SELECT * FROM safetbl WHERE id = :id")
    suspend fun getSafeById(id: String): Safe?

    @Query("SELECT * FROM safetbl WHERE name = :name AND id != :excludeId")
    suspend fun getSafeByName(name: String, excludeId: String = ""): Safe?

    @Insert
    suspend fun insertSafe(safe: Safe)

    @Update
    suspend fun updateSafe(safe: Safe)


    @Query("UPDATE safetbl SET blocked = 1 WHERE id = :id")
    suspend fun blockSafe(id: String)

    @Query("UPDATE safetbl SET blocked = 0 WHERE id = :id")
    suspend fun unblockSafe(id: String)
}
