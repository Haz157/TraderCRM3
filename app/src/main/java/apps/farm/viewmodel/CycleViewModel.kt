package apps.farm.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.R
import apps.farm.data.model.Cycle
import apps.farm.data.repository.CycleRepository
import apps.farm.data.repository.FarmRepository
import apps.farm.data.repository.CustomerRepository
import apps.farm.data.repository.SaleInvoiceRepository
import apps.farm.utils.CycleReportPdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import apps.farm.data.model.CustomerWithBalance
import apps.farm.data.repository.ReceiveRepository


@HiltViewModel
class CycleViewModel @Inject constructor(
    private val repository: CycleRepository,
    private val farmRepository: FarmRepository,
    private val customerRepository: CustomerRepository,
    private val saleInvoiceRepository: SaleInvoiceRepository,
    private val receiveRepository: ReceiveRepository,
    application: Application
) : AndroidViewModel(application) {

    fun getCyclesByFarmId(farmId: String): Flow<List<Cycle>> {
        return repository.getCyclesByFarmId(farmId)
    }

    fun getActiveCyclesByFarmId(farmId: String): Flow<List<Cycle>> {
        return repository.getActiveCyclesByFarmId(farmId)
    }

    suspend fun hasOverlappingCycles(farmId: String, startDate: Long, endDate: Long, excludeId: String = ""): Boolean {
        return repository.hasOverlappingCycles(farmId, startDate, endDate, excludeId)
    }

    fun insertCycle(cycle: Cycle, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        if (repository.hasOverlappingCycles(cycle.farmId, cycle.sd, cycle.ed)) {
            onResult(false, getApplication<Application>().getString(R.string.error_cycle_dates_overlap))
        } else {
            repository.insertCycle(cycle)
            onResult(true, getApplication<Application>().getString(R.string.success_add_cycle))
        }
    }

    fun updateCycle(cycle: Cycle, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        if (repository.hasOverlappingCycles(cycle.farmId, cycle.sd, cycle.ed, cycle.id)) {
            onResult(false, getApplication<Application>().getString(R.string.error_cycle_dates_overlap))
        } else {
            repository.updateCycle(cycle)
            onResult(true, getApplication<Application>().getString(R.string.success_update_cycle))
        }
    }

    fun toggleActiveStatus(cycleId: String, isActive: Boolean) = viewModelScope.launch {
        repository.toggleActiveStatus(cycleId, isActive)
    }

    suspend fun getCycleById(id: String): Cycle? {
        return repository.getCycleById(id)
    }

    fun generateDetailedCycleReport(
        context: Context,
        cycleId: String,
        onPdfGenerated: (File?) -> Unit
    ) {
        viewModelScope.launch {
            val cycle = repository.getCycleById(cycleId) ?: return@launch
            val farm = farmRepository.getFarmById(cycle.farmId) ?: return@launch
            val invoices = saleInvoiceRepository.getInvoicesByCycleSync(cycleId)
            val receives = receiveRepository.getReceivesByDateRange(cycle.sd, cycle.ed)
            val allCustomers = customerRepository.allCustomers.first().associateBy { it.customer.id }

            val items = mutableListOf<CycleReportPdfGenerator.CycleReportItem>()
            
            // Add Invoices (Debit) and their Additions/Discounts
            for (inv in invoices) {
                val customerName = allCustomers[inv.customerId]?.customer?.name ?: "تاجر غير معروف"
                // Row 1: Base invoice - debit = weight * price
                items.add(CycleReportPdfGenerator.CycleReportItem(
                    type = "فاتورة بيع رقم ${inv.invoiceNo}",
                    merchantName = customerName,
                    weight = inv.netWeight,
                    price = inv.price,
                    debit = inv.netWeight * inv.price,
                    credit = 0.0,
                    invoiceDate = inv.invoiceDate,
                    invoiceReceive = inv.receiveAmount,
                    invoiceRemaining = inv.totalInvoice - inv.receiveAmount
                ))
                // Row 2: Addition (if any) - debit = addition
                if (inv.addition > 0) {
                    items.add(CycleReportPdfGenerator.CycleReportItem(
                        type = "تكلفة إضافية فاتورة ${inv.invoiceNo}",
                        merchantName = customerName,
                        weight = 0.0,
                        price = 0.0,
                        debit = inv.addition,
                        credit = 0.0
                    ))
                }
                // Row 3: Discount (if any) - credit = discount
                if (inv.discount > 0) {
                    items.add(CycleReportPdfGenerator.CycleReportItem(
                        type = "خصم فاتورة ${inv.invoiceNo}",
                        merchantName = customerName,
                        weight = 0.0,
                        price = 0.0,
                        debit = 0.0,
                        credit = inv.discount
                    ))
                }
            }

            // Add Receives (Credit)
            for (receive in receives) {
                val customerName = allCustomers[receive.customerId]?.customer?.name ?: "تاجر غير معروف"
                // Receive entry
                items.add(CycleReportPdfGenerator.CycleReportItem(
                    type = "تحصيل رقم ${receive.receiveNo}",
                    merchantName = customerName,
                    weight = 0.0,
                    price = 0.0,
                    debit = 0.0,
                    credit = receive.receive
                ))
                // Discount entry inside Receive (if any)
                if (receive.discount > 0) {
                    items.add(CycleReportPdfGenerator.CycleReportItem(
                        type = "خصم تحصيل رقم ${receive.receiveNo}",
                        merchantName = customerName,
                        weight = 0.0,
                        price = 0.0,
                        debit = 0.0,
                        credit = receive.discount
                    ))
                }
            }

            val generator = CycleReportPdfGenerator(context)
            // Final calculation logic in generator: net = totalDebit - totalCredit
            val file = generator.generatePdf(farm, cycle, items)
            onPdfGenerated(file)
        }
    }
}
