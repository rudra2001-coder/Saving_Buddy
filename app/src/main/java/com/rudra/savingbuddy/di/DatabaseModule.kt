package com.rudra.savingbuddy.di

import android.content.Context
import androidx.room.Room
import com.rudra.savingbuddy.data.local.SavingBuddyDatabase
import com.rudra.savingbuddy.data.local.dao.BudgetDao
import com.rudra.savingbuddy.data.local.dao.ExpenseDao
import com.rudra.savingbuddy.data.local.dao.IncomeDao
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
            "saving_buddy_database"
        ).build()
    }

    @Provides
    fun provideIncomeDao(database: SavingBuddyDatabase): IncomeDao = database.incomeDao()

    @Provides
    fun provideExpenseDao(database: SavingBuddyDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideBudgetDao(database: SavingBuddyDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideUserSettingsDao(database: SavingBuddyDatabase): UserSettingsDao = database.userSettingsDao()
}