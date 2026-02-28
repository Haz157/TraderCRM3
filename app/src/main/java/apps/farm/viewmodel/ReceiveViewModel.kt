package apps.farm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.R
import apps.farm.data.model.Receive
import apps.farm.data.model.Customer
import apps.farm.data.repository.CustomerRepository
import apps.farm.data.repository.ReceiveRepository
import apps.farm.data.repository.SafeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val receiveRepository: ReceiveRepository,
    private val customerRepository: CustomerRepository,
    private val safeRepository: SafeRepository,
    application: Application
) : AndroidViewModel(application) {
    
    private val _allReceives = MutableStateFlow<List<Receive>>(emptyList())
    val allReceives: StateFlow<List<Receive>> = _allReceives.asStateFlow()
    
    private val _activeReceives = MutableStateFlow<List<Receive>>(emptyList())
    val activeReceives: StateFlow<List<Receive>> = _activeReceives.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()
    
    private val _receiveAmount = MutableStateFlow(0.0)
    val receiveAmount: StateFlow<Double> = _receiveAmount.asStateFlow()
    
    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()
    
    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()
    
    private val _customerBalance = MutableStateFlow(0.0)
    val customerBalance: StateFlow<Double> = _customerBalance.asStateFlow()
    
    private val _newBalance = MutableStateFlow(0.0)
    val newBalance: StateFlow<Double> = _newBalance.asStateFlow()
    
    private val _selectedSafe = MutableStateFlow<apps.farm.data.model.Safe?>(null)
    val selectedSafe: StateFlow<apps.farm.data.model.Safe?> = _selectedSafe.asStateFlow()

    private var originalReceive: Receive? = null
    
    val activeSafes = safeRepository.activeSafes
    
    init {
        loadReceives()
    }
    
    fun loadReceive(receiveId: String) {
        viewModelScope.launch {
            receiveRepository.getReceiveById(receiveId)?.let { receive ->
                originalReceive = receive
                _receiveAmount.value = receive.receive
                _discountAmount.value = receive.discount
                _note.value = receive.note
                
                // Load customer
                customerRepository.getCustomerById(receive.customerId)?.let { customer ->
                    _selectedCustomer.value = customer
                    val currentBalance = customerRepository.getCustomerBalance(customer.id)
                    _customerBalance.value = currentBalance
                }
                
                // Load safe
                safeRepository.getSafeById(receive.safeId)?.let { safe ->
                    _selectedSafe.value = safe
                }
                
                calculateNewBalance()
            }
        }
    }

    private fun loadReceives() {
        viewModelScope.launch {
            receiveRepository.getAllReceives().collect { receives ->
                _allReceives.value = receives
            }
        }
        viewModelScope.launch {
            receiveRepository.getAllActiveReceives().collect { receives ->
                _activeReceives.value = receives
            }
        }
    }
    
    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
        viewModelScope.launch {
            val currentBalance = customerRepository.getCustomerBalance(customer.id)
            _customerBalance.value = currentBalance
            calculateNewBalance()
        }
    }
    
    fun setReceiveAmount(amount: Double) {
        _receiveAmount.value = amount
        calculateNewBalance()
    }
    
    fun setDiscountAmount(amount: Double) {
        _discountAmount.value = amount
        calculateNewBalance()
    }
    
    fun setNote(note: String) {
        _note.value = note
    }
    
    fun selectSafe(safe: apps.farm.data.model.Safe) {
        _selectedSafe.value = safe
    }
    
    private fun calculateNewBalance() {
        val currentBalance = _customerBalance.value
        val receiveAmount = _receiveAmount.value
        val discountAmount = _discountAmount.value
        val totalReduction = receiveAmount + discountAmount
        
        _newBalance.value = currentBalance - totalReduction
    }
    
    fun createReceive(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val customer = _selectedCustomer.value
                val safe = _selectedSafe.value
                val receiveAmount = _receiveAmount.value
                
                if (customer == null) {
                    onError(getApplication<Application>().getString(R.string.error_select_customer_receive))
                    return@launch
                }
                if (safe == null) {
                    onError("يرجى اختيار الخزنة")
                    return@launch
                }
                if (receiveAmount <= 0) {
                    onError(getApplication<Application>().getString(R.string.error_invalid_receive_amount))
                    return@launch
                }
                
                val maxNo = receiveRepository.getMaxReceiveNo()
                val receive = Receive(
                    id = java.util.UUID.randomUUID().toString(),
                    customerId = customer.id,
                    safeId = safe.id,
                    receive = receiveAmount,
                    discount = _discountAmount.value,
                    receiveNo = maxNo + 1,
                    note = _note.value
                )
                
                receiveRepository.insertReceive(receive)
                
                // Update safe balance
                val updatedSafe = safe.copy(
                    balance = safe.balance + receiveAmount
                )
                safeRepository.updateSafe(updatedSafe)
                
                clearForm()
                onSuccess()
            } catch (e: Exception) {
                onError(getApplication<Application>().getString(R.string.error_create_receive, e.message))
            }
        }
    }

    fun updateReceive(receiveId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val customer = _selectedCustomer.value
                val safe = _selectedSafe.value
                val receiveAmount = _receiveAmount.value
                val original = originalReceive ?: return@launch
                
                if (customer == null || safe == null || receiveAmount <= 0) {
                    onError("بيانات غير مكتملة")
                    return@launch
                }

                // 1. Reverse old safe impact
                val oldSafe = safeRepository.getSafeById(original.safeId)
                if (oldSafe != null) {
                    safeRepository.updateSafe(oldSafe.copy(balance = oldSafe.balance - original.receive))
                }

                // 2. Apply new safe impact
                val currentSafe = if (safe.id == original.safeId && oldSafe != null) {
                    // Refetch if same safe to get updated balance after reversal
                    safeRepository.getSafeById(safe.id) ?: safe
                } else {
                    safe
                }
                safeRepository.updateSafe(currentSafe.copy(balance = currentSafe.balance + receiveAmount))

                // 3. Update receive
                val updatedReceive = original.copy(
                    customerId = customer.id,
                    safeId = safe.id,
                    receive = receiveAmount,
                    discount = _discountAmount.value,
                    note = _note.value
                )
                receiveRepository.updateReceive(updatedReceive)
                
                clearForm()
                onSuccess()
            } catch (e: Exception) {
                onError("خطأ في تحديث التحصيل: ${e.message}")
            }
        }
    }
    
    fun deleteReceive(receive: Receive, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // Customer balance update removed, DAO handles it.

                // 2. Fetch current safe
                val safe = safeRepository.getSafeById(receive.safeId)
                if (safe != null) {
                    // Reverse the safe impact:
                    // When created: safe.balance = safe.balance + receive
                    // When deleting: safe.balance = safe.balance - receive
                    val updatedSafe = safe.copy(balance = safe.balance - receive.receive)
                    safeRepository.updateSafe(updatedSafe)
                }

                // 3. Delete receive
                receiveRepository.deleteReceive(receive)
                onResult(true, "تم حذف التحصيل بنجاح")
            } catch (e: Exception) {
                onResult(false, "خطأ في حذف التحصيل: ${e.message}")
            }
        }
    }
    
    private fun clearForm() {
        _selectedCustomer.value = null
        _selectedSafe.value = null
        _receiveAmount.value = 0.0
        _discountAmount.value = 0.0
        _note.value = ""
        _customerBalance.value = 0.0
        _newBalance.value = 0.0
    }
    
    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun getCustomerName(customerId: String): String? {
        return customerRepository.getCustomerById(customerId)?.name
    }

    suspend fun getSafeName(safeId: String): String? {
        return safeRepository.getSafeById(safeId)?.name
    }
}
