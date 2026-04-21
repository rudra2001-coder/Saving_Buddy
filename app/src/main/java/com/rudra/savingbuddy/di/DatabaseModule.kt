package com.rudra.savingbuddy.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.rudra.savingbuddy.data.local.SavingBuddyDatabase
import com.rudra.savingbuddy.data.local.dao.AccountBalanceHistoryDao
import com.rudra.savingbuddy.data.local.dao.AccountDao
import com.rudra.savingbuddy.data.local.dao.BudgetDao
import com.rudra.savingbuddy.data.local.dao.BillReminderDao
import com.rudra.savingbuddy.data.local.dao.ExpenseDao
import com.rudra.savingbuddy.data.local.dao.GoalDao
import com.rudra.savingbuddy.data.local.dao.IncomeDao
import com.rudra.savingbuddy.data.local.dao.TransferDao
import com.rudra.savingbuddy.data.local.dao.UserSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SavingBuddyDatabase {
        return Room.databaseBuilder(
            context,
            SavingBuddyDatabase::class.java,
            SavingBuddyDatabase.DATABASE_NAME
        )
            .addMigrations(*SavingBuddyDatabase.getMigrations())
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideIncomeDao(database: SavingBuddyDatabase): IncomeDao = database.incomeDao()

    @Provides
    fun provideExpenseDao(database: SavingBuddyDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideBudgetDao(database: SavingBuddyDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideUserSettingsDao(database: SavingBuddyDatabase): UserSettingsDao = database.userSettingsDao()

    @Provides
    fun provideGoalDao(database: SavingBuddyDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideBillReminderDao(database: SavingBuddyDatabase): BillReminderDao = database.billReminderDao()

    @Provides
    fun provideAccountDao(database: SavingBuddyDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideTransferDao(database: SavingBuddyDatabase): TransferDao = database.transferDao()

    @Provides
    fun provideAccountBalanceHistoryDao(database: SavingBuddyDatabase): AccountBalanceHistoryDao = database.accountBalanceHistoryDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE)
    }
}