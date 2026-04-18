package com.rudra.savingbuddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rudra.savingbuddy.data.local.dao.BudgetDao
import com.rudra.savingbuddy.data.local.dao.ExpenseDao
import com.rudra.savingbuddy.data.local.dao.IncomeDao
import com.rudra.savingbuddy.data.local.dao.UserSettingsDao
import com.rudra.savingbuddy.data.local.entity.BudgetEntity
import com.rudra.savingbuddy.data.local.entity.ExpenseEntity
import com.rudra.savingbuddy.data.local.entity.IncomeEntity
import com.rudra.savingbuddy.data.local.entity.UserSettingsEntity

@Database(
    entities = [
        IncomeEntity::class,
        ExpenseEntity::class,
        BudgetEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SavingBuddyDatabase : RoomDatabase() {
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userSettingsDao(): UserSettingsDao
}