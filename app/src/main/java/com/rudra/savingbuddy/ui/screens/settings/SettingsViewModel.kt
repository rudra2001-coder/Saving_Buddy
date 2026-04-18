package com.rudra.savingbuddy.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Budget
import com.rudra.savingbuddy.domain.model.UserSettings
import com.rudra.savingbuddy.domain.repository.BudgetRepository
import com.rudra.savingbuddy.domain.repository.SettingsRepository
import com.rudra.savingbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val darkMode: Boolean = false,
    val budget: Budget? = null,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderTime: String = "20:00",
    val billReminderEnabled: Boolean = true,
    val showBudgetDialog: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                settingsRepository.getSettings(),
                budgetRepository.getBudget()
            ) { settings, budget ->
                SettingsUiState(
                    darkMode = settings?.darkMode ?: false,
                    budget = budget,
                    dailyReminderEnabled = settings?.dailyReminderEnabled ?: true,
                    dailyReminderTime = settings?.dailyReminderTime ?: "20:00",
                    billReminderEnabled = settings?.billReminderEnabled ?: true,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = _uiState.value
            settingsRepository.updateSettings(
                UserSettings(
                    id = 1,
                    darkMode = enabled,
                    dailyReminderEnabled = currentSettings.dailyReminderEnabled,
                    dailyReminderTime = currentSettings.dailyReminderTime,
                    billReminderEnabled = currentSettings.billReminderEnabled
                )
            )
        }
    }

    fun setDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = _uiState.value
            settingsRepository.updateSettings(
                UserSettings(
                    id = 1,
                    darkMode = currentSettings.darkMode,
                    dailyReminderEnabled = enabled,
                    dailyReminderTime = currentSettings.dailyReminderTime,
                    billReminderEnabled = currentSettings.billReminderEnabled
                )
            )
        }
    }

    fun setDailyReminderTime(time: String) {
        viewModelScope.launch {
            val currentSettings = _uiState.value
            settingsRepository.updateSettings(
                UserSettings(
                    id = 1,
                    darkMode = currentSettings.darkMode,
                    dailyReminderEnabled = currentSettings.dailyReminderEnabled,
                    dailyReminderTime = time,
                    billReminderEnabled = currentSettings.billReminderEnabled
                )
            )
        }
    }

    fun setBillReminder(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = _uiState.value
            settingsRepository.updateSettings(
                UserSettings(
                    id = 1,
                    darkMode = currentSettings.darkMode,
                    dailyReminderEnabled = currentSettings.dailyReminderEnabled,
                    dailyReminderTime = currentSettings.dailyReminderTime,
                    billReminderEnabled = enabled
                )
            )
        }
    }

    fun showBudgetDialog() {
        _uiState.update { it.copy(showBudgetDialog = true) }
    }

    fun hideBudgetDialog() {
        _uiState.update { it.copy(showBudgetDialog = false) }
    }

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            val budget = Budget(
                id = 1,
                monthlyLimit = amount,
                month = DateUtils.getCurrentMonth(),
                year = DateUtils.getCurrentYear()
            )
            budgetRepository.setBudget(budget)
            hideBudgetDialog()
        }
    }
}