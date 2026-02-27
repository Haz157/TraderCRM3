package apps.farm.di

import android.content.Context
import apps.farm.data.local.AppDatabase
import apps.farm.data.dao.CycleDao
import apps.farm.data.dao.CustomerDao
import apps.farm.data.dao.FarmDao
import apps.farm.data.dao.SafeDao
import apps.farm.data.dao.SaleInvoiceDao
import apps.farm.data.dao.ReceiveDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideFarmDao(database: AppDatabase): FarmDao {
        return database.farmDao()
    }

    @Provides
    @Singleton
    fun provideCycleDao(database: AppDatabase): CycleDao {
        return database.cycleDao()
    }

    @Provides
    @Singleton
    fun provideCustomerDao(database: AppDatabase): CustomerDao {
        return database.customerDao()
    }

    @Provides
    @Singleton
    fun provideSafeDao(database: AppDatabase): SafeDao {
        return database.safeDao()
    }

    @Provides
    @Singleton
    fun provideSaleInvoiceDao(database: AppDatabase): SaleInvoiceDao {
        return database.saleInvoiceDao()
    }

    @Provides
    @Singleton
    fun provideReceiveDao(database: AppDatabase): ReceiveDao {
        return database.receiveDao()
    }
}
