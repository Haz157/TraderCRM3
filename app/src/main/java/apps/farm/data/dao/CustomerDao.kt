package apps.farm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import apps.farm.data.model.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM Customertbl ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM Customertbl WHERE id = :id")
    suspend fun getCustomerById(id: String): Customer?

    @Query("SELECT * FROM Customertbl WHERE blocked = 0 ORDER BY name ASC")
    fun getActiveCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE Customertbl SET blocked = :blocked WHERE id = :customerId")
    suspend fun updateCustomerStatus(customerId: String, blocked: Boolean)
}
