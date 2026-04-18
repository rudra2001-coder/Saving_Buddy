package com.rudra.savingbuddy.data.local.converter

import com.rudra.savingbuddy.data.local.entity.IncomeEntity
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.model.IncomeCategory
import com.rudra.savingbuddy.domain.model.RecurringInterval

fun IncomeEntity.toDomain(): Income = Income(
    id = id,
    source = source,
    amount = amount,
    category = try { IncomeCategory.valueOf(category) } catch (e: Exception) { IncomeCategory.OTHERS },
    date = date,
    isRecurring = isRecurring,
    recurringInterval = recurringInterval?.let { 
        try { RecurringInterval.valueOf(it) } catch (e: Exception) { null }
    },
    notes = notes,
    createdAt = createdAt
)

fun Income.toEntity(): IncomeEntity = IncomeEntity(
    id = id,
    source = source,
    amount = amount,
    category = category.name,
    date = date,
    isRecurring = isRecurring,
    recurringInterval = recurringInterval?.name,
    notes = notes,
    createdAt = createdAt
)