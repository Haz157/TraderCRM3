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

    private var originalInvoice: SaleInvoice? = null

    fun loadInvoice(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val invoice = saleInvoiceRepository.getInvoiceById(id)
                if (invoice != null) {
                    originalInvoice = invoice
                    _selectedFarm.value = farmRepository.getFarmById(invoice.farmId)
                    _selectedCycle.value = cycleRepository.getCycleById(invoice.cycleId)
                    _selectedCustomer.value = customerRepository.getCustomerById(invoice.customerId)
                    _selectedSafe.value = safeRepository.getSafeById(invoice.safeId)
                    _invoiceDate.value = invoice.invoiceDate
                    _receiveAmount.value = invoice.receiveAmount
                    _discountAmount.value = invoice.discount
                    _additionAmount.value = invoice.addition
                    _price.value = invoice.price
                    _emptyWeights.value = saleInvoiceRepository.getEmptyWeightsByInvoice(id)
                    _grossWeights.value = saleInvoiceRepository.getGrossWeightsByInvoice(id)
                    
                    _selectedFarm.value?.let { loadCyclesByFarm(it.id) }
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطأ في تحميل الفاتورة: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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
    

    fun deleteInvoice(invoice: SaleInvoice, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // Customer balance update removed as it's now dynamically calculated
                // 2. Fetch current safe
                val safe = safeRepository.getSafeById(invoice.safeId)
                if (safe != null) {
                    // Reverse the safe impact:
                    // When invoice was created: safe.balance = safe.balance + invoice.receiveAmount
                    // When deleting: safe.balance = safe.balance - invoice.receiveAmount
                    val updatedSafe = safe.copy(balance = safe.balance - invoice.receiveAmount)
                    safeRepository.updateSafe(updatedSafe)
                }

                // 3. Delete weights
                saleInvoiceRepository.deleteWeightsByInvoice(invoice.id)
                
                // 4. Delete invoice
                saleInvoiceRepository.deleteInvoice(invoice)
                onResult(true, "تم حذف الفاتورة بنجاح")
            } catch (e: Exception) {
                onResult(false, "خطأ في حذف الفاتورة: ${e.message}")
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
                val invoiceTotalAmount = totalPrice + _additionAmount.value - _discountAmount.value
                val remainingAmount = invoiceTotalAmount - _receiveAmount.value
                
                val maxNo = saleInvoiceRepository.getMaxInvoiceNo()
                
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
                    totalInvoice = invoiceTotalAmount,
                    invoiceNo = maxNo + 1
                )
                
                val finalEmptyWeights = _emptyWeights.value.map { it.copy(invoiceId = invoice.id) }
                val finalGrossWeights = _grossWeights.value.map { it.copy(invoiceId = invoice.id) }
                
                saleInvoiceRepository.insertInvoiceWithWeights(
                    invoice, finalEmptyWeights, finalGrossWeights
                )

                // Safe balance still needs updating
                val currentSafe = safeRepository.getSafeById(safe.id)
                currentSafe?.let {
                    val updatedSafe = it.copy(
                        balance = it.balance + _receiveAmount.value
                    )
                    safeRepository.updateSafe(updatedSafe)
                }
                
                clearForm()
                onSuccess()
            } catch (e: Exception) {
                onError(getApplication<Application>().getString(R.string.error_create_invoice, e.message))
            }
        }
    }

    fun updateInvoice(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val original = originalInvoice ?: return@launch
                val farm = _selectedFarm.value ?: return@launch
                val cycle = _selectedCycle.value ?: return@launch
                val customer = _selectedCustomer.value ?: return@launch
                val safe = _selectedSafe.value ?: return@launch
                val date = _invoiceDate.value ?: return@launch

                if (!isInvoiceDateValid()) {
                    onError(getApplication<Application>().getString(R.string.error_invoice_date_out_of_cycle))
                    return@launch
                }

                // 1. Reverse original impacts
                // Safe impact reversal
                val currentSafe = safeRepository.getSafeById(original.safeId)
                currentSafe?.let {
                    safeRepository.updateSafe(it.copy(balance = it.balance - original.receiveAmount))
                }

                // 2. Calculate new values
                val totalEmptyWeight = _emptyWeights.value.sumOf { it.weight }
                val totalGrossWeight = _grossWeights.value.sumOf { it.weight }
                val netWeight = totalGrossWeight - totalEmptyWeight
                val totalPrice = _price.value * netWeight
                val invoiceTotalAmount = totalPrice + _additionAmount.value - _discountAmount.value
                val remainingAmount = invoiceTotalAmount - _receiveAmount.value

                val updatedInvoice = original.copy(
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
                    totalInvoice = invoiceTotalAmount
                )

                // 3. Save to DB
                saleInvoiceRepository.updateInvoiceWithWeights(
                    updatedInvoice, _emptyWeights.value, _grossWeights.value
                )

                // 4. New Safe impact
                val newSafe = safeRepository.getSafeById(safe.id)
                newSafe?.let {
                    safeRepository.updateSafe(it.copy(balance = it.balance + _receiveAmount.value))
                }
                
                clearForm()
                onSuccess()
            } catch (e: Exception) {
                onError("خطأ في تعديل الفاتورة: ${e.message}")
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
