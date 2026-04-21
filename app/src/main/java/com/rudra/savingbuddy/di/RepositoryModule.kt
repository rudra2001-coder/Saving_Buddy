package com.rudra.savingbuddy.di

import com.rudra.savingbuddy.data.local.dao.BudgetDao
import com.rudra.savingbuddy.data.local.dao.BillReminderDao
import com.rudra.savingbuddy.data.local.dao.ExpenseDao
import com.rudra.savingbuddy.data.local.dao.GoalDao
import com.rudra.savingbuddy.data.local.dao.IncomeDao
import com.rudra.savingbuddy.data.local.dao.UserSettingsDao
import com.rudra.savingbuddy.data.local.dao.AccountDao
import com.rudra.savingbuddy.data.local.dao.TransferDao
import com.rudra.savingbuddy.data.local.dao.AccountBalanceHistoryDao
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.rudra.savingbuddy.data.local.SavingBuddyDatabase
import com.rudra.savingbuddy.data.repository.BudgetRepositoryImpl
import com.rudra.savingbuddy.data.repository.BillReminderRepositoryImpl
import com.rudra.savingbuddy.data.repository.ExpenseRepositoryImpl
import com.rudra.savingbuddy.data.repository.GoalRepositoryImpl
import com.rudra.savingbuddy.data.repository.IncomeRepositoryImpl
import com.rudra.savingbuddy.data.repository.SettingsRepositoryImpl
import com.rudra.savingbuddy.data.repository.AccountRepositoryImpl
import com.rudra.savingbuddy.data.repository.TransferRepositoryImpl
import com.rudra.savingbuddy.data.repository.FusionRepositoryImpl
import com.rudra.savingbuddy.domain.repository.BudgetRepository
import com.rudra.savingbuddy.domain.repository.BillReminderRepository
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.GoalRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import com.rudra.savingbuddy.domain.repository.SettingsRepository
import com.rudra.savingbuddy.domain.repository.AccountRepository
import com.rudra.savingbuddy.domain.repository.TransferRepository
import com.rudra.savingbuddy.domain.repository.FusionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideIncomeRepository(incomeDao: IncomeDao, accountDao: AccountDao): IncomeRepository = IncomeRepositoryImpl(incomeDao, accountDao)

    @Provides
    @Singleton
    fun provideExpenseRepository(expenseDao: ExpenseDao, accountDao: AccountDao): ExpenseRepository = ExpenseRepositoryImpl(expenseDao, accountDao)

    @Provides
    @Singleton
    fun provideBudgetRepository(budgetDao: BudgetDao): BudgetRepository = BudgetRepositoryImpl(budgetDao)

    @Provides
    @Singleton
    fun provideSettingsRepository(userSettingsDao: UserSettingsDao): SettingsRepository = SettingsRepositoryImpl(userSettingsDao)

    @Provides
    @Singleton
    fun provideGoalRepository(goalDao: GoalDao): GoalRepository = GoalRepositoryImpl(goalDao)

    @Provides
    @Singleton
    fun provideBillReminderRepository(billReminderDao: BillReminderDao): BillReminderRepository = BillReminderRepositoryImpl(billReminderDao)

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
        transferDao: TransferDao,
        balanceHistoryDao: AccountBalanceHistoryDao
    ): AccountRepository = AccountRepositoryImpl(accountDao, transferDao, balanceHistoryDao)

    @Provides
    @Singleton
    fun provideTransferRepository(transferDao: TransferDao): TransferRepository = TransferRepositoryImpl(transferDao)

    @Provides
    @Singleton
    fun provideFusionRepository(
        accountDao: AccountDao,
        incomeDao: IncomeDao,
        expenseDao: ExpenseDao,
        transferDao: TransferDao,
        goalDao: GoalDao
    ): FusionRepository = FusionRepositoryImpl(accountDao, incomeDao, expenseDao, transferDao, goalDao)

    }