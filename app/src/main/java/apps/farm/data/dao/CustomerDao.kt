package apps.farm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import apps.farm.data.model.Customer
import apps.farm.data.model.CustomerWithBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("""
        SELECT *, 
            (balance + 
            IFNULL((SELECT SUM(totalPrice + addition - receiveAmount - discount) FROM saleinvoicetbl WHERE customerId = Customertbl.id AND isBlocked = 0), 0) -
            IFNULL((SELECT SUM(receive + discount) FROM receivetbl WHERE customerId = Customertbl.id AND isBlocked = 0), 0)) as currentBalance
        FROM Customertbl 
        ORDER BY name ASC
    """)
    fun getAllCustomersWithBalance(): Flow<List<CustomerWithBalance>>

    @Query("SELECT * FROM Customertbl WHERE id = :id")
    suspend fun getCustomerById(id: String): Customer?

    @Query("""
        SELECT *, 
            (balance + 
            IFNULL((SELECT SUM(totalPrice + addition - receiveAmount - discount) FROM saleinvoicetbl WHERE customerId = Customertbl.id AND isBlocked = 0), 0) -
            IFNULL((SELECT SUM(receive + discount) FROM receivetbl WHERE customerId = Customertbl.id AND isBlocked = 0), 0)) as currentBalance
        FROM Customertbl 
        WHERE blocked = 0 
        ORDER BY name ASC
    """)
    fun getActiveCustomersWithBalance(): Flow<List<CustomerWithBalance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE Customertbl SET blocked = :blocked WHERE id = :customerId")
    suspend fun updateCustomerStatus(customerId: String, blocked: Boolean)

    @Query("""
        SELECT 
            IFNULL((SELECT balance FROM Customertbl WHERE id = :customerId), 0) +
            IFNULL((SELECT SUM(totalPrice + addition - receiveAmount - discount) FROM saleinvoicetbl WHERE customerId = :customerId AND isBlocked = 0), 0) -
            IFNULL((SELECT SUM(receive + discount) FROM receivetbl WHERE customerId = :customerId AND isBlocked = 0), 0)
    """)
    suspend fun getCustomerBalance(customerId: String): Double

    @Query("""
        SELECT 
            IFNULL((SELECT balance FROM Customertbl WHERE id = :customerId), 0) +
            IFNULL((SELECT SUM(totalPrice + addition - receiveAmount - discount) FROM saleinvoicetbl WHERE customerId = :customerId AND createdDate < :date AND isBlocked = 0), 0) -
            IFNULL((SELECT SUM(receive + discount) FROM receivetbl WHERE customerId = :customerId AND createdDate < :date AND isBlocked = 0), 0)
    """)
    suspend fun getCustomerBalanceBeforeDate(customerId: String, date: Long): Double
    @Query("SELECT * FROM Customertbl")
    suspend fun getAllCustomersSync(): List<Customer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)
}
