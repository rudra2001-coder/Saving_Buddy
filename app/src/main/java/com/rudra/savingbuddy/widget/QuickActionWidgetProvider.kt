package com.rudra.savingbuddy.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.rudra.savingbuddy.MainActivity
import com.rudra.savingbuddy.R

class QuickActionWidgetProvider : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, R.layout.quick_action_widget)

            val addIncomeIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("navigate_to", "add_income")
            }
            val addIncomePending = PendingIntent.getActivity(
                context, 0, addIncomeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_action_income, addIncomePending)

            val addExpenseIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("navigate_to", "add_expense")
            }
            val addExpensePending = PendingIntent.getActivity(
                context, 1, addExpenseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_action_expense, addExpensePending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
