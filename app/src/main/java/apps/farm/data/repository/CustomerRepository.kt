package apps.farm.data.repository

import apps.farm.data.dao.CustomerDao
import apps.farm.data.model.Customer
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val customerDao: CustomerDao) {
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val activeCustomers: Flow<List<Customer>> = customerDao.getActiveCustomers()

    suspend fun getCustomerById(id: String): Customer? {
        return customerDao.getCustomerById(id)
    }

    suspend fun insertCustomer(customer: Customer) {
        customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun toggleBlockStatus(customerId: String, blocked: Boolean) {
        customerDao.updateCustomerStatus(customerId, blocked)
    }
}
