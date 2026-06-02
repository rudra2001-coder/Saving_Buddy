package com.rudra.savingbuddy.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.rudra.savingbuddy.MainActivity
import com.rudra.savingbuddy.R
import com.rudra.savingbuddy.data.local.SavingBuddyDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class BalanceWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.balance_widget)
            views.setOnClickPendingIntent(R.id.balance_widget_container, pendingIntent)

            runBlocking {
                try {
                    val db = SavingBuddyDatabase.getInstance(context)
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val startOfDay = calendar.timeInMillis
                    val endOfDay = startOfDay + 86400000L

                    val accounts = db.accountDao().getAllAccounts().first()
                    val totalBalance = accounts.sumOf { it.balance }
                    val todayIncome = db.incomeDao()
                        .getTotalIncomeByDateRange(startOfDay, endOfDay).first() ?: 0.0
                    val todayExpense = db.expenseDao()
                        .getTotalExpensesByDateRange(startOfDay, endOfDay).first() ?: 0.0

                    views.setTextViewText(R.id.widget_balance_amount, formatAmount(totalBalance))
                    views.setTextViewText(R.id.widget_income_amount, formatAmount(todayIncome))
                    views.setTextViewText(R.id.widget_expense_amount, formatAmount(todayExpense))
                } catch (e: Exception) {
                    views.setTextViewText(R.id.widget_balance_amount, "---")
                    views.setTextViewText(R.id.widget_income_amount, "---")
                    views.setTextViewText(R.id.widget_expense_amount, "---")
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun formatAmount(amount: Double): String {
            return if (amount == 0.0) "৳0" else "৳${String.format("%,.0f", amount)}"
        }
    }
}
