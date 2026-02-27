package apps.farm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import apps.farm.data.model.Farm
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM Farmtbl ORDER BY farmName ASC")
    fun getAllFarms(): Flow<List<Farm>>

    @Query("SELECT * FROM Farmtbl WHERE id = :id")
    suspend fun getFarmById(id: String): Farm?

    @Query("SELECT * FROM Farmtbl WHERE blocked = 0 ORDER BY farmName ASC")
    fun getActiveFarms(): Flow<List<Farm>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: Farm)

    @Update
    suspend fun updateFarm(farm: Farm)

    @Query("UPDATE Farmtbl SET blocked = :blocked WHERE id = :farmId")
    suspend fun updateFarmStatus(farmId: String, blocked: Boolean)
}
