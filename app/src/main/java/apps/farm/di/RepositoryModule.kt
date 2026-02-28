package apps.farm.di

import apps.farm.data.dao.CycleDao
import apps.farm.data.dao.CustomerDao
import apps.farm.data.dao.FarmDao
import apps.farm.data.dao.SafeDao
import apps.farm.data.dao.SaleInvoiceDao
import apps.farm.data.dao.ReceiveDao
import apps.farm.data.repository.CycleRepository
import apps.farm.data.repository.CustomerRepository
import apps.farm.data.repository.FarmRepository
import apps.farm.data.repository.SafeRepository
import apps.farm.data.repository.SaleInvoiceRepository
import apps.farm.data.repository.ReceiveRepository
import apps.farm.utils.BackupManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFarmRepository(farmDao: FarmDao, backupManager: BackupManager): FarmRepository {
        return FarmRepository(farmDao, backupManager)
    }

    @Provides
    @Singleton
    fun provideCycleRepository(cycleDao: CycleDao, backupManager: BackupManager): CycleRepository {
        return CycleRepository(cycleDao, backupManager)
    }

    @Provides
    @Singleton
    fun provideCustomerRepository(customerDao: CustomerDao, backupManager: BackupManager): CustomerRepository {
        return CustomerRepository(customerDao, backupManager)
    }

    @Provides
    @Singleton
    fun provideSafeRepository(safeDao: SafeDao, backupManager: BackupManager): SafeRepository {
        return SafeRepository(safeDao, backupManager)
    }

    @Provides
    @Singleton
    fun provideSaleInvoiceRepository(saleInvoiceDao: SaleInvoiceDao, backupManager: BackupManager): SaleInvoiceRepository {
        return SaleInvoiceRepository(saleInvoiceDao, backupManager)
    }

    @Provides
    @Singleton
    fun provideReceiveRepository(receiveDao: ReceiveDao, backupManager: BackupManager): ReceiveRepository {
        return ReceiveRepository(receiveDao, backupManager)
    }
}
