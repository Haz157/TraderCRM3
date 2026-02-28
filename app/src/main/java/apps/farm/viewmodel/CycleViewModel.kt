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


@HiltViewModel
class CycleViewModel @Inject constructor(
    private val repository: CycleRepository,
    private val farmRepository: FarmRepository,
    private val customerRepository: CustomerRepository,
    private val saleInvoiceRepository: SaleInvoiceRepository,
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
            val allCustomers = customerRepository.allCustomers.first().associateBy { it.customer.id }

            val items = mutableListOf<CycleReportPdfGenerator.CycleReportItem>()
            for (inv in invoices) {
                val customerName = allCustomers[inv.customerId]?.customer?.name ?: "تاجر غير معروف"
                // Row 1: Base invoice - credit = weight * price
                items.add(CycleReportPdfGenerator.CycleReportItem(
                    type = "فاتورة بيع رقم ${inv.invoiceNo}",
                    merchantName = customerName,
                    weight = inv.netWeight,
                    price = inv.price,
                    credit = inv.netWeight * inv.price,
                    debit = 0.0
                ))
                // Row 2: Addition (if any) - credit = addition
                if (inv.addition > 0) {
                    items.add(CycleReportPdfGenerator.CycleReportItem(
                        type = "تكلفة إضافية فاتورة ${inv.invoiceNo}",
                        merchantName = customerName,
                        weight = 0.0,
                        price = 0.0,
                        credit = inv.addition,
                        debit = 0.0
                    ))
                }
                // Row 3: Discount (if any) - debit = discount
                if (inv.discount > 0) {
                    items.add(CycleReportPdfGenerator.CycleReportItem(
                        type = "خصم فاتورة ${inv.invoiceNo}",
                        merchantName = customerName,
                        weight = 0.0,
                        price = 0.0,
                        credit = 0.0,
                        debit = inv.discount
                    ))
                }
            }

            val generator = CycleReportPdfGenerator(context)
            val file = generator.generatePdf(farm, cycle, items)
            onPdfGenerated(file)
        }
    }
}
