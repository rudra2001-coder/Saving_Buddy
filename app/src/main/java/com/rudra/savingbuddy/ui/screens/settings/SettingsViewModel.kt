package com.rudra.savingbuddy.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Budget
import com.rudra.savingbuddy.domain.model.UserSettings
import com.rudra.savingbuddy.domain.repository.BudgetRepository
import com.rudra.savingbuddy.domain.repository.SettingsRepository
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val darkMode: Boolean = false,
    val currency: String = "BDT",
    val startOfWeek: String = "Saturday",
    val budget: Budget? = null,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderTime: String = "20:00",
    val billReminderEnabled: Boolean = true,
    val budgetAlertEnabled: Boolean = true,
    val goalReminderEnabled: Boolean = true,
    val pushNotificationEnabled: Boolean = true,
    val weeklySummaryEnabled: Boolean = true,
    val goalProgressEnabled: Boolean = false,
    val privacyModeEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = false,
    val showBudgetDialog: Boolean = false,
    val showCurrencyDialog: Boolean = false,
    val isLoading: Boolean = false,
    val accountCount: Int = 0,
    val goalCount: Int = 0,
    val billCount: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val budgetRepository: BudgetRepository,
    @ApplicationContext private val context: Context
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
                    currency = settings?.currency ?: "BDT",
                    startOfWeek = settings?.startOfWeek ?: "Saturday",
                    budget = budget,
                    dailyReminderEnabled = settings?.dailyReminderEnabled ?: true,
                    dailyReminderTime = settings?.dailyReminderTime ?: "20:00",
                    billReminderEnabled = settings?.billReminderEnabled ?: true,
                    budgetAlertEnabled = settings?.budgetAlertEnabled ?: true,
                    goalReminderEnabled = settings?.goalReminderEnabled ?: true,
                    pushNotificationEnabled = settings?.pushNotificationEnabled ?: true,
                    weeklySummaryEnabled = settings?.weeklySummaryEnabled ?: true,
                    goalProgressEnabled = settings?.goalProgressEnabled ?: false,
                    privacyModeEnabled = settings?.privacyModeEnabled ?: false,
                    biometricLockEnabled = settings?.biometricLockEnabled ?: false,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun getCurrentUserSettings() = UserSettings(
        id = 1,
        darkMode = _uiState.value.darkMode,
        currency = _uiState.value.currency,
        startOfWeek = _uiState.value.startOfWeek,
        dailyReminderEnabled = _uiState.value.dailyReminderEnabled,
        dailyReminderTime = _uiState.value.dailyReminderTime,
        billReminderEnabled = _uiState.value.billReminderEnabled,
        budgetAlertEnabled = _uiState.value.budgetAlertEnabled,
        goalReminderEnabled = _uiState.value.goalReminderEnabled,
        pushNotificationEnabled = _uiState.value.pushNotificationEnabled,
        weeklySummaryEnabled = _uiState.value.weeklySummaryEnabled,
        goalProgressEnabled = _uiState.value.goalProgressEnabled,
        privacyModeEnabled = _uiState.value.privacyModeEnabled,
        biometricLockEnabled = _uiState.value.biometricLockEnabled
    )

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(darkMode = enabled))
        }
    }

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(currency = currency))
            _uiState.update { it.copy(currency = currency) }
            
            CurrencyFormatter.setCurrency(currency)
            
            try {
                val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                prefs.edit().putString("currency", currency).apply()
            } catch (e: Exception) {
                // Silently handle
            }
        }
    }

    fun setStartOfWeek(day: String) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(startOfWeek = day))
            _uiState.update { it.copy(startOfWeek = day) }
        }
    }

    fun setDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(dailyReminderEnabled = enabled))
            _uiState.update { it.copy(dailyReminderEnabled = enabled) }
        }
    }

    fun setDailyReminderTime(time: String) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(dailyReminderTime = time))
            _uiState.update { it.copy(dailyReminderTime = time) }
        }
    }

    fun setBillReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(billReminderEnabled = enabled))
            _uiState.update { it.copy(billReminderEnabled = enabled) }
        }
    }

    fun setBudgetAlert(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(budgetAlertEnabled = enabled))
            _uiState.update { it.copy(budgetAlertEnabled = enabled) }
        }
    }

    fun setGoalReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(goalReminderEnabled = enabled))
            _uiState.update { it.copy(goalReminderEnabled = enabled) }
        }
    }

    fun setPushNotification(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(pushNotificationEnabled = enabled))
            _uiState.update { it.copy(pushNotificationEnabled = enabled) }
        }
    }

    fun setWeeklySummary(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(weeklySummaryEnabled = enabled))
            _uiState.update { it.copy(weeklySummaryEnabled = enabled) }
        }
    }

    fun setGoalProgress(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(goalProgressEnabled = enabled))
            _uiState.update { it.copy(goalProgressEnabled = enabled) }
        }
    }

    fun setPrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(privacyModeEnabled = enabled))
            _uiState.update { it.copy(privacyModeEnabled = enabled) }
        }
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(getCurrentUserSettings().copy(biometricLockEnabled = enabled))
            _uiState.update { it.copy(biometricLockEnabled = enabled) }
        }
    }

    fun showBudgetDialog() {
        _uiState.update { it.copy(showBudgetDialog = true) }
    }

    fun hideBudgetDialog() {
        _uiState.update { it.copy(showBudgetDialog = false) }
    }

    fun showCurrencyDialog() {
        _uiState.update { it.copy(showCurrencyDialog = true) }
    }

    fun hideCurrencyDialog() {
        _uiState.update { it.copy(showCurrencyDialog = false) }
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

    fun shareApp() {
        viewModelScope.launch {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Saving Buddy")
                    putExtra(Intent.EXTRA_TEXT, "Check out Saving Buddy - Your Personal Finance Tracker!\n\nhttps://play.google.com/store/apps/details?id=${context.packageName}")
                }
                val chooser = Intent.createChooser(shareIntent, "Share via")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun rateApp() {
        viewModelScope.launch {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun contactSupport() {
        viewModelScope.launch {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@savingbuddy.app")
                    putExtra(Intent.EXTRA_SUBJECT, "Saving Buddy Support")
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}