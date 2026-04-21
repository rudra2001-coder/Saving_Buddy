package com.rudra.savingbuddy.ui.screens.backup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.data.BackupFileInfo
import com.rudra.savingbuddy.data.BackupManager
import com.rudra.savingbuddy.data.BackupResult
import com.rudra.savingbuddy.data.RestoreResult
import com.rudra.savingbuddy.data.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
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
class BackupViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _backupList = MutableStateFlow<List<BackupFileInfo>>(emptyList())
    val backupList: StateFlow<List<BackupFileInfo>> = _backupList.asStateFlow()

    private var backupManager: BackupManager? = null

    fun initialize(context: Context) {
        backupManager = BackupManager(context)
        loadSettings()
        loadBackupList()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            backupManager?.loadSettings()?.let { settings ->
                _uiState.update { state ->
                    state.copy(
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
    }

    fun loadBackupList() {
        viewModelScope.launch {
            _backupList.value = backupManager?.listBackups() ?: emptyList()
        }
    }

    fun toggleBackup(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = backupManager?.loadSettings() ?: BackupSettings()
            val newSettings = currentSettings.copy(isEnabled = isEnabled)
            backupManager?.let { bm ->
                val prefs = android.content.Context.MODE_PRIVATE.let { 0 }
                bm.loadSettings() // just load current
            }
            _uiState.update { it.copy(isEnabled = isEnabled) }
        }
    }

    fun updateFrequency(frequency: BackupFrequency) {
        viewModelScope.launch {
            _uiState.update { it.copy(frequency = frequency) }
        }
    }

    fun updateBackupDay(day: BackupDay) {
        viewModelScope.launch {
            _uiState.update { it.copy(backupDay = day) }
        }
    }

    fun updateLocation(location: BackupLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(backupLocation = location) }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackingUp = true, error = null, success = null) }

            try {
                val settings = _uiState.value
                val backupSettings = BackupSettings(
                    isEnabled = settings.isEnabled,
                    frequency = settings.frequency,
                    backupDay = settings.backupDay,
                    backupFormat = settings.backupFormat,
                    backupLocation = settings.backupLocation,
                    lastBackupTime = 0
                )

                val data = BackupData(
                    timestamp = System.currentTimeMillis(),
                    settings = backupSettings
                )

                val result = backupManager?.createBackup(data)
                when (result) {
                    is BackupResult.Success -> {
                        _uiState.update { it.copy(
                            isBackingUp = false,
                            success = "Backup created successfully!"
                        ) }
                        loadBackupList()
                    }
                    is BackupResult.Error -> {
                        _uiState.update { it.copy(
                            isBackingUp = false,
                            error = result.message
                        ) }
                    }
                    null -> {
                        _uiState.update { it.copy(isBackingUp = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isBackingUp = false,
                    error = e.message
                ) }
            }
        }
    }

    fun restoreBackup(filePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, error = null, success = null) }

            try {
                val result = backupManager?.restoreBackup(filePath)
                when (result) {
                    is RestoreResult.Success -> {
                        _uiState.update { it.copy(
                            isRestoring = false,
                            success = "Backup restored successfully!"
                        ) }
                    }
                    is RestoreResult.Error -> {
                        _uiState.update { it.copy(
                            isRestoring = false,
                            error = result.message
                        ) }
                    }
                    null -> {
                        _uiState.update { it.copy(isRestoring = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isRestoring = false,
                    error = e.message
                ) }
            }
        }
    }

    fun deleteBackup(filePath: String) {
        viewModelScope.launch {
            backupManager?.deleteBackup(filePath)
            loadBackupList()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, success = null) }
    }
}