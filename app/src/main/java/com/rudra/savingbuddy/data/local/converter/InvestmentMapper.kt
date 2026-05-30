package com.rudra.savingbuddy.data.local.converter

import com.rudra.savingbuddy.data.local.entity.InvestmentEntity
import com.rudra.savingbuddy.domain.model.Investment
import com.rudra.savingbuddy.domain.model.InvestmentType

fun InvestmentEntity.toDomain(): Investment = Investment(
    id = id,
    name = name,
    type = try { InvestmentType.valueOf(type) } catch (e: Exception) { InvestmentType.OTHER },
    amount = amount,
    currentValue = currentValue,
    purchaseDate = purchaseDate,
    notes = notes,
    createdAt = createdAt
)

fun Investment.toEntity(): InvestmentEntity = InvestmentEntity(
    id = id,
    name = name,
    type = type.name,
    amount = amount,
    currentValue = currentValue,
    purchaseDate = purchaseDate,
    notes = notes,
    createdAt = createdAt
)
