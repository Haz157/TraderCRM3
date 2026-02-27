package apps.farm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.data.model.Customer
import apps.farm.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {
    val allCustomers: Flow<List<Customer>> = repository.allCustomers
    val activeCustomers: Flow<List<Customer>> = repository.activeCustomers

    fun insertCustomer(customer: Customer) = viewModelScope.launch {
        repository.insertCustomer(customer)
    }

    fun updateCustomer(customer: Customer) = viewModelScope.launch {
        repository.updateCustomer(customer)
    }

    fun toggleBlockStatus(customerId: String, blocked: Boolean) = viewModelScope.launch {
        repository.toggleBlockStatus(customerId, blocked)
    }

    suspend fun getCustomerById(id: String): Customer? {
        return repository.getCustomerById(id)
    }
}
