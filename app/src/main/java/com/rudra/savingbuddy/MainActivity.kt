package com.rudra.savingbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rudra.savingbuddy.ui.navigation.MainNavigation
import com.rudra.savingbuddy.ui.theme.SavingBuddyTheme
import com.rudra.savingbuddy.util.BillNotificationWorker
import com.rudra.savingbuddy.util.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        BillNotificationWorker.scheduleBillReminderCheck(this)
        
        initCurrency()
        
        setContent {
            SavingBuddyTheme {
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