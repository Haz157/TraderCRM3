package apps.farm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import apps.farm.data.model.Cycle
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Query("SELECT * FROM Cycletbl WHERE farmId = :farmId ORDER BY sd DESC")
    fun getCyclesByFarmId(farmId: String): Flow<List<Cycle>>

    @Query("SELECT * FROM Cycletbl WHERE id = :id")
    suspend fun getCycleById(id: String): Cycle?

    @Query("SELECT * FROM Cycletbl WHERE farmId = :farmId AND isActive = 1")
    fun getActiveCyclesByFarmId(farmId: String): Flow<List<Cycle>>

    @Query("SELECT COUNT(*) FROM Cycletbl WHERE farmId = :farmId AND id != :excludeId AND ((sd <= :newEd AND ed >= :newSd))")
    suspend fun countOverlappingCycles(farmId: String, newSd: Long, newEd: Long, excludeId: String = ""): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: Cycle)

    @Update
    suspend fun updateCycle(cycle: Cycle)

    @Query("UPDATE Cycletbl SET isActive = :isActive WHERE id = :cycleId")
    suspend fun updateCycleStatus(cycleId: String, isActive: Boolean)
}
