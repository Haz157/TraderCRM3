package apps.farm.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import apps.farm.data.repository.AuthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authRepository: AuthRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val email = authRepository.backupEmail.first()
        val isEnabled = authRepository.isAutoBackupEnabled.first()

        if (!isEnabled) return Result.success()

        return try {
            val dbFile = applicationContext.getDatabasePath("farm_management_db")
            if (!dbFile.exists()) return Result.failure()

            // Gather the file
            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.ENGLISH).format(java.util.Date())
            val backupFile = File(applicationContext.cacheDir, "نسخة_احتياطية_تلقائية_$timeStamp.db")
            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("BackupWorker", "Automatic backup prepared for $email at ${backupFile.absolutePath}")
            
            showBackupNotification(email)
            
            Result.success()
        } catch (e: Exception) {
            Log.e("BackupWorker", "Backup failed", e)
            Result.retry()
        }
    }

    private fun showBackupNotification(email: String) {
        val builder = NotificationCompat.Builder(applicationContext, "backup_channel")
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("نسخة احتياطية تلقائية")
            .setContentText("تم تجهيز النسخة الاحتياطية للإرسال إلى: $email")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(1001, builder.build())
            }
        }
    }
}
