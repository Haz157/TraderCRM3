package apps.farm.data.repository

import apps.farm.data.dao.SaleInvoiceDao
import apps.farm.data.model.SaleInvoice
import apps.farm.data.model.EmptyWeight
import apps.farm.data.model.GrossWeight
import apps.farm.utils.BackupManager
import kotlinx.coroutines.flow.Flow

class SaleInvoiceRepository(
    private val saleInvoiceDao: SaleInvoiceDao,
    private val backupManager: BackupManager
) {
    fun getAllActiveInvoices(): Flow<List<SaleInvoice>> = saleInvoiceDao.getAllActiveInvoices()
    fun getAllInvoices(): Flow<List<SaleInvoice>> = saleInvoiceDao.getAllInvoices()
    
    suspend fun getInvoiceById(id: String): SaleInvoice? = saleInvoiceDao.getInvoiceById(id)
    suspend fun getMaxInvoiceNo(): Int = saleInvoiceDao.getMaxInvoiceNo() ?: 0
    fun getInvoicesByCycle(cycleId: String): Flow<List<SaleInvoice>> = 
        saleInvoiceDao.getInvoicesByCycle(cycleId)
    suspend fun getInvoicesByCycleSync(cycleId: String): List<SaleInvoice> =
        saleInvoiceDao.getInvoicesByCycleSync(cycleId)
    fun getInvoicesByCustomer(customerId: String): Flow<List<SaleInvoice>> = 
        saleInvoiceDao.getInvoicesByCustomer(customerId)
    
    suspend fun getInvoicesByCustomerAndDateRange(customerId: String, startDate: Long, endDate: Long): List<SaleInvoice> =
        saleInvoiceDao.getInvoicesByCustomerAndDateRange(customerId, startDate, endDate)
        
    fun getInvoicesBySafe(safeId: String): Flow<List<SaleInvoice>> = 
        saleInvoiceDao.getInvoicesBySafe(safeId)
    
    suspend fun insertInvoice(invoice: SaleInvoice): Long {
        val result = saleInvoiceDao.insertInvoice(invoice)
        backupManager.scheduleBackup()
        return result
    }

    suspend fun updateInvoice(invoice: SaleInvoice) {
        saleInvoiceDao.updateInvoice(invoice)
        backupManager.scheduleBackup()
    }

    suspend fun deleteInvoice(invoice: SaleInvoice) {
        saleInvoiceDao.deleteInvoice(invoice)
        backupManager.scheduleBackup()
    }
    
    suspend fun blockInvoice(id: String) {
        saleInvoiceDao.blockInvoice(id)
        backupManager.scheduleBackup()
    }

    suspend fun unblockInvoice(id: String) {
        saleInvoiceDao.unblockInvoice(id)
        backupManager.scheduleBackup()
    }
    
    suspend fun insertInvoiceWithWeights(
        invoice: SaleInvoice,
        emptyWeights: List<EmptyWeight>,
        grossWeights: List<GrossWeight>
    ) {
        saleInvoiceDao.insertInvoiceWithWeights(invoice, emptyWeights, grossWeights)
        backupManager.scheduleBackup()
    }

    suspend fun updateInvoiceWithWeights(
        invoice: SaleInvoice,
        emptyWeights: List<EmptyWeight>,
        grossWeights: List<GrossWeight>
    ) {
        saleInvoiceDao.updateInvoiceWithWeights(invoice, emptyWeights, grossWeights)
        backupManager.scheduleBackup()
    }
    
    suspend fun getEmptyWeightsByInvoice(invoiceId: String): List<EmptyWeight> = 
        saleInvoiceDao.getEmptyWeightsByInvoice(invoiceId)
    suspend fun getGrossWeightsByInvoice(invoiceId: String): List<GrossWeight> = 
        saleInvoiceDao.getGrossWeightsByInvoice(invoiceId)
    
    suspend fun deleteWeightsByInvoice(invoiceId: String) {
        saleInvoiceDao.deleteEmptyWeightsByInvoice(invoiceId)
        saleInvoiceDao.deleteGrossWeightsByInvoice(invoiceId)
    }

    suspend fun insertInvoices(invoices: List<SaleInvoice>) = saleInvoiceDao.insertInvoices(invoices)
    suspend fun insertEmptyWeights(weights: List<EmptyWeight>) = saleInvoiceDao.insertEmptyWeights(weights)
    suspend fun insertGrossWeights(weights: List<GrossWeight>) = saleInvoiceDao.insertGrossWeights(weights)
    suspend fun getAllInvoicesSync() = saleInvoiceDao.getAllInvoicesSync()
    suspend fun getAllEmptyWeights() = saleInvoiceDao.getAllEmptyWeights()
    suspend fun getAllGrossWeights() = saleInvoiceDao.getAllGrossWeights()
    suspend fun clearAllTables() = saleInvoiceDao.clearAllTables()

    suspend fun fullDataRestore(
        farms: List<apps.farm.data.model.Farm>,
        cycles: List<apps.farm.data.model.Cycle>,
        customers: List<apps.farm.data.model.Customer>,
        safes: List<apps.farm.data.model.Safe>,
        invoices: List<SaleInvoice>,
        receives: List<apps.farm.data.model.Receive>,
        emptyWeights: List<EmptyWeight>,
        grossWeights: List<GrossWeight>
    ) = saleInvoiceDao.fullDataRestore(farms, cycles, customers, safes, invoices, receives, emptyWeights, grossWeights)
}
