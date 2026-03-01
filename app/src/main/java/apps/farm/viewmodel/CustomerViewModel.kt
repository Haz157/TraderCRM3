package apps.farm.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.R
import apps.farm.data.model.Customer
import apps.farm.data.model.CustomerTransaction
import apps.farm.data.model.CustomerWithBalance
import apps.farm.data.repository.CustomerRepository
import apps.farm.data.repository.CycleRepository
import apps.farm.data.repository.FarmRepository
import apps.farm.data.repository.ReceiveRepository
import apps.farm.data.repository.SaleInvoiceRepository
import apps.farm.utils.CustomerStatementPdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository,
    private val receiveRepository: ReceiveRepository,
    private val saleInvoiceRepository: SaleInvoiceRepository,
    private val farmRepository: FarmRepository,
    private val cycleRepository: CycleRepository,
    application: android.app.Application
) : AndroidViewModel(application) {
    val allCustomers: Flow<List<CustomerWithBalance>> = repository.allCustomers
    val activeCustomers: Flow<List<CustomerWithBalance>> = repository.activeCustomers

    fun insertCustomer(customer: Customer, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        try {
            repository.insertCustomer(customer)
            onResult(true, getApplication<android.app.Application>().getString(apps.farm.R.string.success_add_customer))
        } catch (e: Exception) {
            onResult(false, getApplication<android.app.Application>().getString(apps.farm.R.string.error_add_customer, e.message))
        }
    }

    fun updateCustomer(customer: Customer, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        try {
            repository.updateCustomer(customer)
            onResult(true, getApplication<android.app.Application>().getString(apps.farm.R.string.success_update_customer))
        } catch (e: Exception) {
            onResult(false, getApplication<android.app.Application>().getString(apps.farm.R.string.error_update_customer, e.message))
        }
    }

    fun toggleBlockStatus(customerId: String, blocked: Boolean) = viewModelScope.launch {
        repository.toggleBlockStatus(customerId, blocked)
    }

    suspend fun getCustomerById(id: String): Customer? {
        return repository.getCustomerById(id)
    }

    fun generateCustomerStatement(
        context: Context,
        customerId: String,
        startDate: Long?,
        endDate: Long?,
        onPdfGenerated: (File?) -> Unit
    ) {
        viewModelScope.launch {
            val customer = repository.getCustomerById(customerId) ?: return@launch
            
            // 1. Calculate opening balance
            val openingBalance = if (startDate != null) {
                repository.getCustomerBalanceBeforeDate(customerId, startDate)
            } else {
                customer.balance // From customer tbl
                customer.balance // The initial balance field in DB
            }

            // 2. Fetch Transactions within the period
            val sDate = startDate ?: 0L
            val eDate = endDate ?: Long.MAX_VALUE
            
            val receives = receiveRepository.getReceivesByCustomerAndDateRange(customerId, sDate, eDate)
            val invoices = saleInvoiceRepository.getInvoicesByCustomerAndDateRange(customerId, sDate, eDate)

            // 3. Fetch Farm and Cycle names
            val allFarms = farmRepository.allFarms.first().associateBy { it.id }
            val allCycles = cycleRepository.getAllCyclesSync().associateBy { it.id }

            // 4. Map to detailed CustomerTransactions and Sort
            val transactions = mutableListOf<CustomerTransaction>()
            
            for (r in receives) {
                // Split Receive into Payment and Discount if both exist
                if (r.receive > 0) {
                    transactions.add(
                        CustomerTransaction(
                            date = r.createdDate,
                            typeName = "تحصيل مستقل",
                            operationId = r.receiveNo.toString(),
                            cycleName = null,
                            farmName = null,
                            netWeight = null,
                            price = null,
                            debit = 0.0,
                            credit = r.receive,
                            cumulativeBalance = 0.0
                        )
                    )
                }
                if (r.discount > 0) {
                    transactions.add(
                        CustomerTransaction(
                            date = r.createdDate,
                            typeName = "خصم مستقل",
                            operationId = r.receiveNo.toString(),
                            cycleName = null,
                            farmName = null,
                            netWeight = null,
                            price = null,
                            debit = 0.0,
                            credit = r.discount,
                            cumulativeBalance = 0.0
                        )
                    )
                }
            }
            
            for (inv in invoices) {
                val cycleName = allCycles[inv.cycleId]?.cycleName ?: "-"
                val farmName = allFarms[inv.farmId]?.farmName ?: "-"
                
                // 1. Base Invoice Amount
                transactions.add(
                    CustomerTransaction(
                        date = inv.createdDate,
                        typeName = "فاتورة بيع",
                        operationId = inv.invoiceNo.toString(),
                        cycleName = cycleName,
                        farmName = farmName,
                        netWeight = inv.netWeight,
                        price = inv.price,
                        debit = inv.totalPrice,
                        credit = 0.0,
                        cumulativeBalance = 0.0,
                        invoiceReceive = inv.receiveAmount,
                        invoiceRemaining = inv.totalInvoice - inv.receiveAmount
                    )
                )
                
                // 2. Addition (if exists)
                if (inv.addition > 0) {
                    transactions.add(
                        CustomerTransaction(
                            date = inv.createdDate + 1, // Slight offset for sorting priority
                            typeName = "تكلفة إضافية",
                            operationId = inv.invoiceNo.toString(),
                            cycleName = cycleName,
                            farmName = farmName,
                            netWeight = null,
                            price = null,
                            debit = inv.addition,
                            credit = 0.0,
                            cumulativeBalance = 0.0
                        )
                    )
                }
                
                // 3. Discount (if exists)
                if (inv.discount > 0) {
                    transactions.add(
                        CustomerTransaction(
                            date = inv.createdDate + 2,
                            typeName = "خصم فاتورة",
                            operationId = inv.invoiceNo.toString(),
                            cycleName = cycleName,
                            farmName = farmName,
                            netWeight = null,
                            price = null,
                            debit = 0.0,
                            credit = inv.discount,
                            cumulativeBalance = 0.0
                        )
                    )
                }
                
                // 4. Paid during Invoice (if exists)
                if (inv.receiveAmount > 0) {
                    transactions.add(
                        CustomerTransaction(
                            date = inv.createdDate + 3,
                            typeName = "تحصيل فاتورة",
                            operationId = inv.invoiceNo.toString(),
                            cycleName = cycleName,
                            farmName = farmName,
                            netWeight = null,
                            price = null,
                            debit = 0.0,
                            credit = inv.receiveAmount,
                            cumulativeBalance = 0.0
                        )
                    )
                }
            }

            transactions.sortBy { it.date }

            // 5. Calculate Cumulative Balance (Debt as Positive)
            var currentBalance = openingBalance
            val finalTransactions = transactions.map { t ->
                currentBalance = currentBalance + t.debit - t.credit
                t.copy(cumulativeBalance = currentBalance)
            }

            // 5. Generate PDF
            val generator = CustomerStatementPdfGenerator(context)
            val file = generator.generatePdf(customer, finalTransactions, openingBalance, startDate, endDate)
            onPdfGenerated(file)
        }
    }
}
