package apps.farm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.R
import apps.farm.data.model.*
import apps.farm.data.repository.CustomerRepository
import apps.farm.data.repository.CycleRepository
import apps.farm.data.repository.FarmRepository
import apps.farm.data.repository.SaleInvoiceRepository
import apps.farm.data.repository.SafeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SaleInvoiceViewModel @Inject constructor(
    private val saleInvoiceRepository: SaleInvoiceRepository,
    private val cycleRepository: CycleRepository,
    private val customerRepository: CustomerRepository,
    private val safeRepository: SafeRepository,
    private val farmRepository: FarmRepository,
    application: Application
) : AndroidViewModel(application) {
    
    private val _allInvoices = MutableStateFlow<List<SaleInvoice>>(emptyList())
    val allInvoices: StateFlow<List<SaleInvoice>> = _allInvoices.asStateFlow()
    
    private val _activeInvoices = MutableStateFlow<List<SaleInvoice>>(emptyList())
    val activeInvoices: StateFlow<List<SaleInvoice>> = _activeInvoices.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _selectedFarm = MutableStateFlow<Farm?>(null)
    val selectedFarm: StateFlow<Farm?> = _selectedFarm.asStateFlow()
    
    private val _selectedCycle = MutableStateFlow<Cycle?>(null)
    val selectedCycle: StateFlow<Cycle?> = _selectedCycle.asStateFlow()
    
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()
    
    private val _selectedSafe = MutableStateFlow<Safe?>(null)
    val selectedSafe: StateFlow<Safe?> = _selectedSafe.asStateFlow()
    
    private val _invoiceDate = MutableStateFlow<Long?>(null)
    val invoiceDate: StateFlow<Long?> = _invoiceDate.asStateFlow()
    
    private val _receiveAmount = MutableStateFlow(0.0)
    val receiveAmount: StateFlow<Double> = _receiveAmount.asStateFlow()
    
    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()
    
    private val _additionAmount = MutableStateFlow(0.0)
    val additionAmount: StateFlow<Double> = _additionAmount.asStateFlow()
    
    private val _price = MutableStateFlow(0.0)
    val price: StateFlow<Double> = _price.asStateFlow()
    
    private val _emptyWeights = MutableStateFlow<List<EmptyWeight>>(emptyList())
    val emptyWeights: StateFlow<List<EmptyWeight>> = _emptyWeights.asStateFlow()
    
    private val _grossWeights = MutableStateFlow<List<GrossWeight>>(emptyList())
    val grossWeights: StateFlow<List<GrossWeight>> = _grossWeights.asStateFlow()
    
    // Selection data
    val activeFarms = farmRepository.activeFarms
    val activeCustomers = customerRepository.activeCustomers
    val activeSafes = safeRepository.activeSafes
    
    private val _cyclesByFarm = MutableStateFlow<List<Cycle>>(emptyList())
    val cyclesByFarm: StateFlow<List<Cycle>> = _cyclesByFarm.asStateFlow()
    
    init {
        loadInvoices()
    }
    
    private fun loadInvoices() {
        viewModelScope.launch {
            saleInvoiceRepository.getAllInvoices().collect { invoices ->
                _allInvoices.value = invoices
            }
        }
        viewModelScope.launch {
            saleInvoiceRepository.getAllActiveInvoices().collect { invoices ->
                _activeInvoices.value = invoices
            }
        }
    }
    
    fun selectFarm(farm: Farm) {
        _selectedFarm.value = farm
        _selectedCycle.value = null
        loadCyclesByFarm(farm.id)
    }
    
    private fun loadCyclesByFarm(farmId: String) {
        viewModelScope.launch {
            cycleRepository.getActiveCyclesByFarmId(farmId).collect { cycles ->
                _cyclesByFarm.value = cycles
            }
        }
    }
    
    fun selectCycle(cycle: Cycle) {
        _selectedCycle.value = cycle
    }
    
    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
    }
    
    fun selectSafe(safe: Safe) {
        _selectedSafe.value = safe
    }
    
    fun setInvoiceDate(date: Long) {
        _invoiceDate.value = date
    }
    
    fun setReceiveAmount(amount: Double) {
        _receiveAmount.value = amount
    }
    
    fun setDiscountAmount(amount: Double) {
        _discountAmount.value = amount
    }
    
    fun setAdditionAmount(amount: Double) {
        _additionAmount.value = amount
    }
    
    fun setPrice(price: Double) {
        _price.value = price
    }
    
    fun addEmptyWeight(weight: Double, crateCount: Int) {
        val newWeight = EmptyWeight(
            id = UUID.randomUUID().toString(),
            invoiceId = "",
            weight = weight,
            crateCount = crateCount
        )
        _emptyWeights.value = _emptyWeights.value + newWeight
    }
    
    fun addGrossWeight(weight: Double, crateCount: Int) {
        val newWeight = GrossWeight(
            id = UUID.randomUUID().toString(),
            invoiceId = "",
            weight = weight,
            crateCount = crateCount
        )
        _grossWeights.value = _grossWeights.value + newWeight
    }
    
    fun removeEmptyWeight(weight: EmptyWeight) {
        _emptyWeights.value -= weight
    }
    
    fun removeGrossWeight(weight: GrossWeight) {
        _grossWeights.value -= weight
    }
    
    fun isInvoiceDateValid(): Boolean {
        val date = _invoiceDate.value ?: return false
        val cycle = _selectedCycle.value ?: return false
        return date >= cycle.sd && date <= cycle.ed
    }
    
    fun blockInvoice(id: String) {
        viewModelScope.launch {
            try {
                saleInvoiceRepository.blockInvoice(id)
            } catch (e: Exception) {
                _errorMessage.value = getApplication<Application>().getString(R.string.error_block_invoice, e.message)
            }
        }
    }
    
    fun unblockInvoice(id: String) {
        viewModelScope.launch {
            try {
                saleInvoiceRepository.unblockInvoice(id)
            } catch (e: Exception) {
                _errorMessage.value = getApplication<Application>().getString(R.string.error_unblock_invoice, e.message)
            }
        }
    }
    
    fun toggleBlockStatus(id: String, isCurrentlyBlocked: Boolean) {
        viewModelScope.launch {
            try {
                if (isCurrentlyBlocked) {
                    saleInvoiceRepository.unblockInvoice(id)
                } else {
                    saleInvoiceRepository.blockInvoice(id)
                }
            } catch (e: Exception) {
                _errorMessage.value = getApplication<Application>().getString(R.string.error_update_invoice_status, e.message)
            }
        }
    }
    
    fun createInvoice(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val farm = _selectedFarm.value
                val cycle = _selectedCycle.value
                val customer = _selectedCustomer.value
                val safe = _selectedSafe.value
                val date = _invoiceDate.value
                
                if (farm == null) {
                    onError(getApplication<Application>().getString(R.string.error_select_farm))
                    return@launch
                }
                if (cycle == null) {
                    onError(getApplication<Application>().getString(R.string.error_select_cycle))
                    return@launch
                }
                if (customer == null) {
                    onError(getApplication<Application>().getString(R.string.error_select_customer_invoice))
                    return@launch
                }
                if (safe == null) {
                    onError(getApplication<Application>().getString(R.string.error_select_safe))
                    return@launch
                }
                if (date == null) {
                    onError(getApplication<Application>().getString(R.string.error_select_invoice_date))
                    return@launch
                }
                if (!isInvoiceDateValid()) {
                    onError(getApplication<Application>().getString(R.string.error_invoice_date_out_of_cycle))
                    return@launch
                }
                if (_emptyWeights.value.isEmpty()) {
                    onError(getApplication<Application>().getString(R.string.error_add_empty_weight))
                    return@launch
                }
                if (_grossWeights.value.isEmpty()) {
                    onError(getApplication<Application>().getString(R.string.error_add_gross_weight))
                    return@launch
                }
                
                val totalEmptyWeight = _emptyWeights.value.sumOf { it.weight }
                val totalGrossWeight = _grossWeights.value.sumOf { it.weight }
                val netWeight = totalGrossWeight - totalEmptyWeight
                val totalPrice = _price.value * netWeight
                val totalInvoice = totalPrice + _additionAmount.value - _discountAmount.value - _receiveAmount.value
                
                val invoice = SaleInvoice(
                    id = UUID.randomUUID().toString(),
                    farmId = farm.id,
                    cycleId = cycle.id,
                    invoiceDate = date,
                    customerId = customer.id,
                    safeId = safe.id,
                    receiveAmount = _receiveAmount.value,
                    discount = _discountAmount.value,
                    addition = _additionAmount.value,
                    totalEmptyWeight = totalEmptyWeight,
                    totalGrossWeight = totalGrossWeight,
                    netWeight = netWeight,
                    price = _price.value,
                    totalPrice = totalPrice,
                    totalInvoice = totalInvoice
                )
                
                val finalEmptyWeights = _emptyWeights.value.map { it.copy(invoiceId = invoice.id) }
                val finalGrossWeights = _grossWeights.value.map { it.copy(invoiceId = invoice.id) }
                
                saleInvoiceRepository.insertInvoiceWithWeights(
                    invoice, finalEmptyWeights, finalGrossWeights
                )
                
                clearForm()
                onSuccess()
            } catch (e: Exception) {
                onError(getApplication<Application>().getString(R.string.error_create_invoice, e.message))
            }
        }
    }
    
    private fun clearForm() {
        _selectedFarm.value = null
        _selectedCycle.value = null
        _selectedCustomer.value = null
        _selectedSafe.value = null
        _invoiceDate.value = null
        _receiveAmount.value = 0.0
        _discountAmount.value = 0.0
        _additionAmount.value = 0.0
        _price.value = 0.0
        _emptyWeights.value = emptyList()
        _grossWeights.value = emptyList()
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
