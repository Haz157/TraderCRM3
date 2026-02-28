package apps.farm.data.dao

import androidx.room.*
import apps.farm.data.model.SaleInvoice
import apps.farm.data.model.EmptyWeight
import apps.farm.data.model.GrossWeight
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleInvoiceDao {
    @Query("SELECT * FROM saleinvoicetbl WHERE isBlocked = 0 ORDER BY createdDate DESC")
    fun getAllActiveInvoices(): Flow<List<SaleInvoice>>

    @Query("SELECT * FROM saleinvoicetbl ORDER BY createdDate DESC")
    fun getAllInvoices(): Flow<List<SaleInvoice>>

    @Query("SELECT * FROM saleinvoicetbl WHERE id = :id")
    suspend fun getInvoiceById(id: String): SaleInvoice?

    @Query("SELECT MAX(invoiceNo) FROM saleinvoicetbl")
    suspend fun getMaxInvoiceNo(): Int?

    @Query("SELECT * FROM saleinvoicetbl WHERE cycleId = :cycleId AND isBlocked = 0 ORDER BY createdDate DESC")
    fun getInvoicesByCycle(cycleId: String): Flow<List<SaleInvoice>>

    @Query("SELECT * FROM saleinvoicetbl WHERE cycleId = :cycleId AND isBlocked = 0 ORDER BY createdDate DESC")
    suspend fun getInvoicesByCycleSync(cycleId: String): List<SaleInvoice>

    @Query("SELECT * FROM saleinvoicetbl WHERE customerId = :customerId AND isBlocked = 0 ORDER BY createdDate DESC")
    fun getInvoicesByCustomer(customerId: String): Flow<List<SaleInvoice>>

    @Query("SELECT * FROM saleinvoicetbl WHERE customerId = :customerId AND createdDate >= :startDate AND createdDate <= :endDate AND isBlocked = 0 ORDER BY createdDate ASC")
    suspend fun getInvoicesByCustomerAndDateRange(customerId: String, startDate: Long, endDate: Long): List<SaleInvoice>

    @Query("SELECT * FROM saleinvoicetbl WHERE safeId = :safeId AND isBlocked = 0 ORDER BY createdDate DESC")
    fun getInvoicesBySafe(safeId: String): Flow<List<SaleInvoice>>

    @Insert
    suspend fun insertInvoice(invoice: SaleInvoice): Long

    @Update
    suspend fun updateInvoice(invoice: SaleInvoice)

    @Delete
    suspend fun deleteInvoice(invoice: SaleInvoice)


    @Query("UPDATE saleinvoicetbl SET isBlocked = 1 WHERE id = :id")
    suspend fun blockInvoice(id: String)

    @Query("UPDATE saleinvoicetbl SET isBlocked = 0 WHERE id = :id")
    suspend fun unblockInvoice(id: String)

    @Transaction
    suspend fun insertInvoiceWithWeights(
        invoice: SaleInvoice,
        emptyWeights: List<EmptyWeight>,
        grossWeights: List<GrossWeight>
    ) {
        insertInvoice(invoice)
        emptyWeights.forEach { insertEmptyWeight(it) }
        grossWeights.forEach { insertGrossWeight(it) }
    }

    @Transaction
    suspend fun updateInvoiceWithWeights(
        invoice: SaleInvoice,
        emptyWeights: List<EmptyWeight>,
        grossWeights: List<GrossWeight>
    ) {
        updateInvoice(invoice)
        deleteEmptyWeightsByInvoice(invoice.id)
        deleteGrossWeightsByInvoice(invoice.id)
        emptyWeights.forEach { insertEmptyWeight(it.copy(invoiceId = invoice.id)) }
        grossWeights.forEach { insertGrossWeight(it.copy(invoiceId = invoice.id)) }
    }

    @Query("SELECT * FROM emptyweighttbl WHERE invoiceId = :invoiceId")
    suspend fun getEmptyWeightsByInvoice(invoiceId: String): List<EmptyWeight>

    @Query("SELECT * FROM grossweighttbl WHERE invoiceId = :invoiceId")
    suspend fun getGrossWeightsByInvoice(invoiceId: String): List<GrossWeight>

    @Insert
    suspend fun insertEmptyWeight(emptyWeight: EmptyWeight)

    @Insert
    suspend fun insertGrossWeight(grossWeight: GrossWeight)

    @Query("DELETE FROM emptyweighttbl WHERE invoiceId = :invoiceId")
    suspend fun deleteEmptyWeightsByInvoice(invoiceId: String)

    @Query("DELETE FROM grossweighttbl WHERE invoiceId = :invoiceId")
    suspend fun deleteGrossWeightsByInvoice(invoiceId: String)
}
