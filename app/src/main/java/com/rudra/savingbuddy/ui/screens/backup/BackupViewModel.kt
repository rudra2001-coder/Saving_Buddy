package com.rudra.savingbuddy.ui.screens.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.data.BackupFileInfo
import com.rudra.savingbuddy.data.BackupManager
import com.rudra.savingbuddy.data.BackupResult
import com.rudra.savingbuddy.data.BackupWorker
import com.rudra.savingbuddy.data.RestoreResult
import com.rudra.savingbuddy.data.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isEnabled: Boolean = false,
    val frequency: BackupFrequency = BackupFrequency.DAILY,
    val backupDay: BackupDay? = null,
    val backupFormat: BackupFormat = BackupFormat.JSON,
    val backupLocation: BackupLocation = BackupLocation.DOWNLOADS,
    val lastBackupTime: Long = 0,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _backupList = MutableStateFlow<List<BackupFileInfo>>(emptyList())
    val backupList: StateFlow<List<BackupFileInfo>> = _backupList.asStateFlow()

    init {
        loadSettings()
        loadBackupList()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = backupManager.loadSettings()
            _uiState.update {
                it.copy(
                    isEnabled = settings.isEnabled,
                    frequency = settings.frequency,
                    backupDay = settings.backupDay,
                    backupFormat = settings.backupFormat,
                    backupLocation = settings.backupLocation,
                    lastBackupTime = settings.lastBackupTime
                )
            }
        }
    }

    fun loadBackupList() {
        viewModelScope.launch {
            _backupList.value = backupManager.listBackups()
        }
    }

    fun toggleBackup(isEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEnabled = isEnabled) }
            val settings = _uiState.value.toBackupSettings()
            backupManager.saveSettings(settings)
            if (isEnabled) {
                BackupWorker.schedule(context, settings.frequency)
            } else {
                BackupWorker.cancel(context)
            }
        }
    }

    fun updateFrequency(frequency: BackupFrequency) {
        viewModelScope.launch {
            _uiState.update { it.copy(frequency = frequency) }
            val settings = _uiState.value.toBackupSettings()
            backupManager.saveSettings(settings)
            if (settings.isEnabled) {
                BackupWorker.schedule(context, frequency)
            }
        }
    }

    fun updateBackupDay(day: BackupDay) {
        viewModelScope.launch {
            _uiState.update { it.copy(backupDay = day) }
            val settings = _uiState.value.toBackupSettings()
            backupManager.saveSettings(settings)
        }
    }

    fun updateLocation(location: BackupLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(backupLocation = location) }
            val settings = _uiState.value.toBackupSettings()
            backupManager.saveSettings(settings)
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackingUp = true, error = null, success = null) }
            try {
                val result = backupManager.exportAllData(_uiState.value.backupLocation)
                when (result) {
                    is BackupResult.Success -> {
                        _uiState.update { it.copy(isBackingUp = false, success = "Backup saved to Downloads/SavingBuddy Backups/") }
                        loadBackupList()
                    }
                    is BackupResult.Error -> {
                        _uiState.update { it.copy(isBackingUp = false, error = result.message) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isBackingUp = false, error = e.message) }
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, error = null, success = null) }
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader()?.readText() ?: ""
                inputStream?.close()

                val backupData = backupManager.parseBackupFileFromString(jsonString)
                if (backupData == null) {
                    _uiState.update { it.copy(isRestoring = false, error = "Invalid backup file") }
                    return@launch
                }

                val result = backupManager.importAllData(backupData)
                when (result) {
                    is RestoreResult.Success -> {
                        _uiState.update { it.copy(isRestoring = false, success = "Backup restored successfully!") }
                    }
                    is RestoreResult.Error -> {
                        _uiState.update { it.copy(isRestoring = false, error = result.message) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRestoring = false, error = "Failed to import: ${e.message}") }
            }
        }
    }

    fun shareBackup(backupPath: String) {
        val intent = backupManager.createShareIntent(backupPath)
        context.startActivity(Intent.createChooser(intent, "Share Backup"))
    }

    fun restoreBackup(filePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, error = null, success = null) }
            try {
                val backupData = backupManager.parseBackupFile(filePath)
                if (backupData == null) {
                    _uiState.update { it.copy(isRestoring = false, error = "Invalid backup file") }
                    return@launch
                }
                val result = backupManager.importAllData(backupData)
                when (result) {
                    is RestoreResult.Success -> {
                        _uiState.update { it.copy(isRestoring = false, success = "Backup restored successfully!") }
                    }
                    is RestoreResult.Error -> {
                        _uiState.update { it.copy(isRestoring = false, error = result.message) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRestoring = false, error = e.message) }
            }
        }
    }

    fun deleteBackup(filePath: String) {
        viewModelScope.launch {
            backupManager.deleteBackup(filePath)
            loadBackupList()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, success = null) }
    }

    private fun BackupUiState.toBackupSettings() = BackupSettings(
        isEnabled = isEnabled,
        frequency = frequency,
        backupDay = backupDay,
        backupFormat = backupFormat,
        backupLocation = backupLocation,
        lastBackupTime = lastBackupTime
    )
}
