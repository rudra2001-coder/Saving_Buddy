package com.rudra.savingbuddy.data.local.converter

import com.rudra.savingbuddy.data.local.entity.AccountEntity
import com.rudra.savingbuddy.data.local.entity.TransferEntity
import com.rudra.savingbuddy.data.local.entity.AccountBalanceHistoryEntity
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.AccountType
import com.rudra.savingbuddy.domain.model.Transfer
import com.rudra.savingbuddy.domain.model.TransferStatus
import com.rudra.savingbuddy.domain.model.AccountBalanceHistory

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    type = AccountType.valueOf(type),
    provider = provider,
    accountNumber = accountNumber,
    balance = balance,
    initialBalance = initialBalance,
    currency = currency,
    iconColor = iconColor,
    isActive = isActive,
    lastUpdated = lastUpdated,
    createdAt = createdAt,
    dailyLimit = dailyLimit,
    usedToday = usedToday,
    linkedGoalId = linkedGoalId,
    displayOrder = displayOrder
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    type = type.name,
    provider = provider,
    accountNumber = accountNumber,
    balance = balance,
    initialBalance = initialBalance,
    currency = currency,
    iconColor = iconColor,
    isActive = isActive,
    lastUpdated = lastUpdated,
    createdAt = createdAt,
    dailyLimit = dailyLimit,
    usedToday = usedToday,
    linkedGoalId = linkedGoalId,
    displayOrder = displayOrder
)

fun TransferEntity.toDomain(): Transfer = Transfer(
    id = id,
    fromAccountId = fromAccountId,
    toAccountId = toAccountId,
    amount = amount,
    fee = fee,
    note = note,
    timestamp = timestamp,
    status = TransferStatus.valueOf(status),
    reference = reference
)

fun Transfer.toEntity(): TransferEntity = TransferEntity(
    id = id,
    fromAccountId = fromAccountId,
    toAccountId = toAccountId,
    amount = amount,
    fee = fee,
    note = note,
    timestamp = timestamp,
    status = status.name,
    reference = reference
)

fun AccountBalanceHistoryEntity.toDomain(): AccountBalanceHistory = AccountBalanceHistory(
    accountId = accountId,
    date = date,
    balance = balance
)

fun AccountBalanceHistory.toEntity(): AccountBalanceHistoryEntity = AccountBalanceHistoryEntity(
    accountId = accountId,
    date = date,
    balance = balance
)