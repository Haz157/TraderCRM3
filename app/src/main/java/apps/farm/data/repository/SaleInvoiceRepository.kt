package apps.farm.data.repository

import apps.farm.data.dao.SaleInvoiceDao
import apps.farm.data.model.SaleInvoice
import apps.farm.data.model.EmptyWeight
import apps.farm.data.model.GrossWeight
import kotlinx.coroutines.flow.Flow

class SaleInvoiceRepository(private val saleInvoiceDao: SaleInvoiceDao) {
    fun getAllActiveInvoices(): Flow<List<SaleInvoice>> = saleInvoiceDao.getAllActiveInvoices()
    fun getAllInvoices(): Flow<List<SaleInvoice>> = saleInvoiceDao.getAllInvoices()
    
    suspend fun getInvoiceById(id: String): SaleInvoice? = saleInvoiceDao.getInvoiceById(id)
    fun getInvoicesByCycle(cycleId: String): Flow<List<SaleInvoice>> = 
        saleInvoiceDao.getInvoicesByCycle(cycleId)
    fun getInvoicesByCustomer(customerId: String): Flow<List<SaleInvoice>> = 
        saleInvoiceDao.getInvoicesByCustomer(customerId)
    fun getInvoicesBySafe(safeId: String): Flow<List<SaleInvoice>> = 
        saleInvoiceDao.getInvoicesBySafe(safeId)
    
    suspend fun insertInvoice(invoice: SaleInvoice): Long = saleInvoiceDao.insertInvoice(invoice)
    suspend fun updateInvoice(invoice: SaleInvoice) = saleInvoiceDao.updateInvoice(invoice)
    
    suspend fun blockInvoice(id: String) = saleInvoiceDao.blockInvoice(id)
    suspend fun unblockInvoice(id: String) = saleInvoiceDao.unblockInvoice(id)
    
    suspend fun insertInvoiceWithWeights(
        invoice: SaleInvoice,
        emptyWeights: List<EmptyWeight>,
        grossWeights: List<GrossWeight>
    ) = saleInvoiceDao.insertInvoiceWithWeights(invoice, emptyWeights, grossWeights)
    
    suspend fun getEmptyWeightsByInvoice(invoiceId: String): List<EmptyWeight> = 
        saleInvoiceDao.getEmptyWeightsByInvoice(invoiceId)
    suspend fun getGrossWeightsByInvoice(invoiceId: String): List<GrossWeight> = 
        saleInvoiceDao.getGrossWeightsByInvoice(invoiceId)
    
    suspend fun deleteWeightsByInvoice(invoiceId: String) {
        saleInvoiceDao.deleteEmptyWeightsByInvoice(invoiceId)
        saleInvoiceDao.deleteGrossWeightsByInvoice(invoiceId)
    }
}
