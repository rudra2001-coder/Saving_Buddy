package com.rudra.savingbuddy.di

import com.rudra.savingbuddy.data.local.dao.BudgetDao
import com.rudra.savingbuddy.data.local.dao.ExpenseDao
import com.rudra.savingbuddy.data.local.dao.IncomeDao
import com.rudra.savingbuddy.data.local.dao.UserSettingsDao
import com.rudra.savingbuddy.data.repository.BudgetRepositoryImpl
import com.rudra.savingbuddy.data.repository.ExpenseRepositoryImpl
import com.rudra.savingbuddy.data.repository.IncomeRepositoryImpl
import com.rudra.savingbuddy.data.repository.SettingsRepositoryImpl
import com.rudra.savingbuddy.domain.repository.BudgetRepository
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import com.rudra.savingbuddy.domain.repository.SettingsRepository
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
    fun provideIncomeRepository(incomeDao: IncomeDao): IncomeRepository = IncomeRepositoryImpl(incomeDao)

    @Provides
    @Singleton
    fun provideExpenseRepository(expenseDao: ExpenseDao): ExpenseRepository = ExpenseRepositoryImpl(expenseDao)

    @Provides
    @Singleton
    fun provideBudgetRepository(budgetDao: BudgetDao): BudgetRepository = BudgetRepositoryImpl(budgetDao)

    @Provides
    @Singleton
    fun provideSettingsRepository(userSettingsDao: UserSettingsDao): SettingsRepository = SettingsRepositoryImpl(userSettingsDao)
}