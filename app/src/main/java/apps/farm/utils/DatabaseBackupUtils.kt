package apps.farm.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import apps.farm.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DatabaseBackupUtils {

    private const val DB_NAME = "farm_management_db"

    fun backupDatabase(context: Context) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) return

            // Create a timestamped backup file in the cache directory
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val backupFileName = "نسخة_احتياطية_بيانات_المزرعة_$timeStamp.db"
            val tempFile = File(context.cacheDir, backupFileName)

            // Copy DB to temp file
            FileInputStream(dbFile).use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Share the file
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية لتطبيق إدارة المزارع")
                putExtra(Intent.EXTRA_TEXT, "هذا الملف يحتوي على نسخة احتياطية من بيانات تطبيق إدارة المزارع. يرجى الاحتفاظ به لاستخدامه عند الحاجة لاستعادة البيانات.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "مشاركة النسخة الاحتياطية"))

            // Show Drive notification
            showDriveBackupNotification(context)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showDriveBackupNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, "backup_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use launcher icon for now
            .setContentTitle(context.getString(R.string.notification_backup_title))
            .setContentText(context.getString(R.string.notification_backup_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())
    }

    fun shareSelectiveExport(context: Context, content: String) {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val exportFileName = "تصدير_بيانات_مختارة_$timeStamp.txt"
            val tempFile = File(context.cacheDir, exportFileName)

            FileOutputStream(tempFile).use { output ->
                output.write(content.toByteArray())
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "تصدير بيانات مختارة")
                putExtra(Intent.EXTRA_TEXT, "هذا الملف يحتوي على تصدير لبيانات مختارة من تطبيق إدارة المزارع.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "مشاركة التصدير"))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restoreDatabase(context: Context, backupUri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            
            // 1. Copy backupUri to dbFile
            context.contentResolver.openInputStream(backupUri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw Exception("Could not open backup file")

            // 2. Clear WAL and SHM files if they exist (common for SQLite)
            val walFile = File("${dbFile.path}-wal")
            val shmFile = File("${dbFile.path}-shm")
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }

    fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}
