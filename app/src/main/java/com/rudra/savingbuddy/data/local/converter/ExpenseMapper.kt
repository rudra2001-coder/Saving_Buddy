package com.rudra.savingbuddy.data.local.converter

import com.rudra.savingbuddy.data.local.entity.ExpenseEntity
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.domain.model.RecurringInterval
import com.rudra.savingbuddy.domain.model.RecurringStatus

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    amount = amount,
    category = try { ExpenseCategory.valueOf(category) } catch (e: Exception) { ExpenseCategory.OTHERS },
    date = date,
    notes = notes,
    isRecurring = isRecurring,
    recurringInterval = recurringInterval?.let { try { RecurringInterval.valueOf(it) } catch (e: Exception) { null } },
    createdAt = createdAt,
    accountId = accountId,
    status = if (isRecurring) RecurringStatus.ACTIVE else RecurringStatus.ACTIVE,
    endDate = recurringEndDate
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    amount = amount,
    category = category.name,
    date = date,
    notes = notes,
    isRecurring = isRecurring,
    recurringInterval = recurringInterval?.name,
    recurringEndDate = endDate,
    createdAt = createdAt,
    accountId = accountId
)