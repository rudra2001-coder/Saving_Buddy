package com.rudra.savingbuddy.data.local.converter

import com.rudra.savingbuddy.data.local.entity.ExpenseEntity
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.ExpenseCategory

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    amount = amount,
    category = try { ExpenseCategory.valueOf(category) } catch (e: Exception) { ExpenseCategory.OTHERS },
    date = date,
    notes = notes,
    createdAt = createdAt,
    accountId = accountId
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    amount = amount,
    category = category.name,
    date = date,
    notes = notes,
    createdAt = createdAt,
    accountId = accountId
)