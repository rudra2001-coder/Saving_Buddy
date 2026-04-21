package com.rudra.savingbuddy.data

import android.content.Context
import android.os.Environment
import com.rudra.savingbuddy.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(private val context: Context) {
    
    private val json = Json { 
        prettyPrint = true 
        encodeDefaults = true 
    }
    
    suspend fun createBackup(data: BackupData): BackupResult = withContext(Dispatchers.IO) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val timestamp = dateFormat.format(Date(data.timestamp))
            val filename = "savings_backup_$timestamp.json"
            
            val jsonString = json.encodeToString(BackupData.serializer(), data)
            val backupFile = getBackupFile(filename, data.settings.backupLocation)
            backupFile.parentFile?.mkdirs()
            backupFile.writeText(jsonString)
            
            val updatedSettings = data.settings.copy(lastBackupTime = System.currentTimeMillis())
            saveSettingsInternal(updatedSettings)
            
            BackupResult.Success(backupFile.absolutePath)
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "Unknown error")
        }
    }
    
    private fun saveSettingsInternal(settings: BackupSettings) {
        val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("backup_enabled", settings.isEnabled)
            putString("backup_frequency", settings.frequency.name)
            settings.backupDay?.let { putString("backup_day", it.name) }
            putString("backup_location", settings.backupLocation.name)
            putLong("last_backup_time", settings.lastBackupTime)
            apply()
        }
    }
    
    suspend fun restoreBackup(filePath: String): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext RestoreResult.Error("Backup file not found")
            }
            
            val jsonString = file.readText()
            val backupData = json.decodeFromString(BackupData.serializer(), jsonString)
            
            RestoreResult.Success(backupData)
        } catch (e: Exception) {
            RestoreResult.Error("Failed to restore: ${e.message}")
        }
    }
    
    suspend fun listBackups(): List<BackupFileInfo> = withContext(Dispatchers.IO) {
        val backupDir = getBackupDirectory(BackupLocation.DOWNLOADS)
        val internalDir = getBackupDirectory(BackupLocation.INTERNAL)
        
        val files = mutableListOf<File>()
        if (backupDir.exists()) files.addAll(backupDir.listFiles()?.toList() ?: emptyList())
        if (internalDir.exists()) files.addAll(internalDir.listFiles()?.toList() ?: emptyList())
        
        files
            .filter { it.name.startsWith("savings_backup_") && it.extension == "json" }
            .map { file ->
                BackupFileInfo(
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    modifiedDate = file.lastModified()
                )
            }
            .sortedByDescending { it.modifiedDate }
    }
    
    suspend fun deleteBackup(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun loadSettings(): BackupSettings = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        BackupSettings(
            isEnabled = prefs.getBoolean("backup_enabled", false),
            frequency = try {
                BackupFrequency.valueOf(prefs.getString("backup_frequency", "DAILY") ?: "DAILY")
            } catch (e: Exception) { BackupFrequency.DAILY },
            backupDay = try {
                prefs.getString("backup_day", null)?.let { BackupDay.valueOf(it) }
            } catch (e: Exception) { null },
            backupFormat = BackupFormat.JSON,
            backupLocation = try {
                BackupLocation.valueOf(prefs.getString("backup_location", "DOWNLOADS") ?: "DOWNLOADS")
            } catch (e: Exception) { BackupLocation.DOWNLOADS },
            lastBackupTime = prefs.getLong("last_backup_time", 0)
        )
    }
    
    private fun getBackupFile(filename: String, location: BackupLocation): File {
        val directory = getBackupDirectory(location)
        return File(directory, filename)
    }
    
    private fun getBackupDirectory(location: BackupLocation): File {
        return when (location) {
            BackupLocation.INTERNAL -> File(context.filesDir, "backups")
            BackupLocation.DOWNLOADS -> {
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                File(downloads, "SavingBuddy Backups").apply { mkdirs() }
            }
        }
    }
}

sealed class BackupResult {
    data class Success(val path: String) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(val data: BackupData) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

data class BackupFileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val modifiedDate: Long
)