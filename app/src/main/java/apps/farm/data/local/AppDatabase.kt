package apps.farm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import apps.farm.data.dao.CustomerDao
import apps.farm.data.dao.CycleDao
import apps.farm.data.dao.FarmDao
import apps.farm.data.dao.ReceiveDao
import apps.farm.data.dao.SafeDao
import apps.farm.data.dao.SaleInvoiceDao
import apps.farm.data.model.Customer
import apps.farm.data.model.Cycle
import apps.farm.data.model.EmptyWeight
import apps.farm.data.model.Farm
import apps.farm.data.model.GrossWeight
import apps.farm.data.model.Receive
import apps.farm.data.model.Safe
import apps.farm.data.model.SaleInvoice

@Database(
    entities = [Farm::class, Cycle::class, Customer::class, Safe::class, SaleInvoice::class, EmptyWeight::class, GrossWeight::class, Receive::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
    abstract fun cycleDao(): CycleDao
    abstract fun customerDao(): CustomerDao
    abstract fun safeDao(): SafeDao
    abstract fun saleInvoiceDao(): SaleInvoiceDao
    abstract fun receiveDao(): ReceiveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "farm_management_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
