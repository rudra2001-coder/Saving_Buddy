package com.rudra.savingbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.rudra.savingbuddy.domain.repository.SettingsRepository
import com.rudra.savingbuddy.ui.navigation.MainNavigation
import com.rudra.savingbuddy.ui.theme.SavingBuddyTheme
import com.rudra.savingbuddy.util.BillNotificationWorker
import com.rudra.savingbuddy.util.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        BillNotificationWorker.scheduleBillReminderCheck(this)

        initCurrency()

        setContent {
            val settings by settingsRepository.getSettings().collectAsState(initial = null)

            val darkMode = settings?.darkMode ?: false
            val amoledMode = settings?.amoledMode ?: false
            val darkModeScheduled = settings?.darkModeScheduled ?: false
            val startHour = settings?.darkModeStartHour ?: 18
            val endHour = settings?.darkModeEndHour ?: 6

            val effectiveDarkMode = if (darkModeScheduled) {
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (startHour <= endHour) {
                    currentHour in startHour until endHour
                } else {
                    currentHour >= startHour || currentHour < endHour
                }
            } else {
                darkMode
            }

            SavingBuddyTheme(
                darkTheme = effectiveDarkMode,
                amoledMode = amoledMode
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }

    private fun initCurrency() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
                val currency = prefs.getString("currency", "BDT") ?: "BDT"
                CurrencyFormatter.setCurrency(currency)
            } catch (e: Exception) {
                CurrencyFormatter.setCurrency("BDT")
            }
        }
    }
}
