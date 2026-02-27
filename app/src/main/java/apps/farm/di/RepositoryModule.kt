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
    fun provideFarmRepository(farmDao: FarmDao): FarmRepository {
        return FarmRepository(farmDao)
    }

    @Provides
    @Singleton
    fun provideCycleRepository(cycleDao: CycleDao): CycleRepository {
        return CycleRepository(cycleDao)
    }

    @Provides
    @Singleton
    fun provideCustomerRepository(customerDao: CustomerDao): CustomerRepository {
        return CustomerRepository(customerDao)
    }

    @Provides
    @Singleton
    fun provideSafeRepository(safeDao: SafeDao): SafeRepository {
        return SafeRepository(safeDao)
    }

    @Provides
    @Singleton
    fun provideSaleInvoiceRepository(saleInvoiceDao: SaleInvoiceDao): SaleInvoiceRepository {
        return SaleInvoiceRepository(saleInvoiceDao)
    }

    @Provides
    @Singleton
    fun provideReceiveRepository(receiveDao: ReceiveDao): ReceiveRepository {
        return ReceiveRepository(receiveDao)
    }
}
