package apps.farm.data.repository

import apps.farm.data.dao.CustomerDao
import apps.farm.data.model.Customer
import apps.farm.data.model.CustomerWithBalance
import apps.farm.utils.BackupManager
import kotlinx.coroutines.flow.Flow

class CustomerRepository(
    private val customerDao: CustomerDao,
    private val backupManager: BackupManager
) {
    val allCustomers: Flow<List<CustomerWithBalance>> = customerDao.getAllCustomersWithBalance()
    val activeCustomers: Flow<List<CustomerWithBalance>> = customerDao.getActiveCustomersWithBalance()

    suspend fun getCustomerById(id: String): Customer? {
        return customerDao.getCustomerById(id)
    }

    suspend fun insertCustomer(customer: Customer) {
        customerDao.insertCustomer(customer)
        backupManager.scheduleBackup()
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
        backupManager.scheduleBackup()
    }

    suspend fun toggleBlockStatus(customerId: String, blocked: Boolean) {
        customerDao.updateCustomerStatus(customerId, blocked)
        backupManager.scheduleBackup()
    }

    suspend fun getCustomerBalance(customerId: String): Double {
        return customerDao.getCustomerBalance(customerId)
    }

    suspend fun getCustomerBalanceBeforeDate(customerId: String, date: Long): Double {
        return customerDao.getCustomerBalanceBeforeDate(customerId, date)
    }
}
