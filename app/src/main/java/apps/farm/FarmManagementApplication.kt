package apps.farm

import android.app.Application
import android.content.Context
import android.os.LocaleList
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class FarmManagementApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Automatic Backup"
        val descriptionText = "Notifications for automatic background backups"
        val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
        val channel = android.app.NotificationChannel("backup_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(setLocale(base, "ar"))
    }

    companion object {
        fun setLocale(context: Context, languageCode: String): Context {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val resources = context.resources
            val config = android.content.res.Configuration(resources.configuration)
            config.setLocales(LocaleList(locale))
            
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)

            return context
        }
    }
}
