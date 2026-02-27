package apps.farm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.R
import apps.farm.data.model.Receive
import apps.farm.data.model.Customer
import apps.farm.data.repository.CustomerRepository
import apps.farm.data.repository.ReceiveRepository
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
    
    init {
        loadReceives()
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
        _customerBalance.value = customer.balance
        calculateNewBalance()
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
    
    private fun calculateNewBalance() {
        val currentBalance = _customerBalance.value
        val receiveAmount = _receiveAmount.value
        val discountAmount = _discountAmount.value
        val netReceive = receiveAmount - discountAmount
        
        _newBalance.value = currentBalance + netReceive
    }
    
    fun createReceive(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val customer = _selectedCustomer.value
                val receiveAmount = _receiveAmount.value
                
                if (customer == null) {
                    onError(getApplication<Application>().getString(R.string.error_select_customer_receive))
                    return@launch
                }
                if (receiveAmount <= 0) {
                    onError(getApplication<Application>().getString(R.string.error_invalid_receive_amount))
                    return@launch
                }
                
                val receive = Receive(
                    id = java.util.UUID.randomUUID().toString(),
                    customerId = customer.id,
                    receive = receiveAmount,
                    discount = _discountAmount.value,
                    note = _note.value
                )
                
                receiveRepository.insertReceive(receive)
                
                // Update customer balance
                val updatedCustomer = customer.copy(
                    balance = _newBalance.value
                )
                customerRepository.updateCustomer(updatedCustomer)
                
                clearForm()
                onSuccess()
            } catch (e: Exception) {
                onError(getApplication<Application>().getString(R.string.error_create_receive, e.message))
            }
        }
    }
    
    private fun clearForm() {
        _selectedCustomer.value = null
        _receiveAmount.value = 0.0
        _discountAmount.value = 0.0
        _note.value = ""
        _customerBalance.value = 0.0
        _newBalance.value = 0.0
    }
    
    fun toggleBlockStatus(id: String, isCurrentlyBlocked: Boolean) {
        viewModelScope.launch {
            try {
                if (isCurrentlyBlocked) {
                    receiveRepository.unblockReceive(id)
                } else {
                    receiveRepository.blockReceive(id)
                }
            } catch (e: Exception) {
                _errorMessage.value = getApplication<Application>().getString(R.string.error_update_receive_status, e.message)
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
