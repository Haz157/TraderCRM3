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
            // Close database to merge WAL contents into the main DB file for a consistent backup
            // This is safer than just copying the live file
            apps.farm.data.local.AppDatabase.closeDatabase()

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
            .setSmallIcon(R.drawable.ic_launcher_foreground) 
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
            // 1. Close the current database connection
            apps.farm.data.local.AppDatabase.closeDatabase()

            val dbFile = context.getDatabasePath(DB_NAME)
            
            // 2. Delete the current database (and its segment files -wal, -shm)
            context.deleteDatabase(DB_NAME)
            
            // 3. Copy backupUri to dbFile
            context.contentResolver.openInputStream(backupUri)?.use { input ->
                // Check if the input stream is empty (common issue with some file pickers)
                if (input.available() == 0) {
                    // Note: available() might be 0 for some streams even if data is there, 
                    // but for local files/Drive files it's usually reliable or handled by copyTo
                }
                
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw Exception("Could not open backup file")

            // Double check if file was copied and has size
            if (!dbFile.exists() || dbFile.length() == 0L) {
                 throw Exception("Failed to copy database: file is empty")
            }

            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Unknown error")
        }
    }

    fun exportToJson(context: Context, data: apps.farm.data.model.DataBackupModel) {
        try {
            val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(data)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val exportFileName = "بيانات_المزرعة_احتياطية_$timeStamp.dmp"
            val tempFile = File(context.cacheDir, exportFileName)

            FileOutputStream(tempFile).use { output ->
                output.write(jsonString.toByteArray())
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية JSON (.dmp)")
                putExtra(Intent.EXTRA_TEXT, "نسخة احتياطية منظمة لبيانات تطبيق المزارع بصيغة JSON.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "تصدير البيانات (JSON)"))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun importFromJson(
        context: Context,
        backupUri: Uri,
        onDataParsed: (apps.farm.data.model.DataBackupModel) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val contentResolver = context.contentResolver
            contentResolver.openInputStream(backupUri)?.use { input ->
                val reader = input.bufferedReader()
                val jsonString = reader.readText()
                val gson = com.google.gson.Gson()
                val data = gson.fromJson(jsonString, apps.farm.data.model.DataBackupModel::class.java)
                
                if (data != null) {
                    onDataParsed(data)
                } else {
                    onError("Failed to parse JSON file")
                }
            } ?: onError("Could not open file")
        } catch (e: Exception) {
            e.printStackTrace()
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
