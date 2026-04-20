package com.rudra.savingbuddy.data.local.converter

import com.rudra.savingbuddy.data.local.entity.BillReminderEntity
import com.rudra.savingbuddy.domain.model.BillCycle
import com.rudra.savingbuddy.domain.model.BillReminder

object BillReminderMapper {
    fun toEntity(bill: BillReminder): BillReminderEntity {
        return BillReminderEntity(
            id = bill.id,
            name = bill.name,
            amount = bill.amount,
            billingDay = bill.billingDay,
            billingCycle = bill.billingCycle.name,
            category = bill.category,
            isActive = bill.isActive,
            notifyDaysBefore = bill.notifyDaysBefore.joinToString(","),
            isNotificationEnabled = bill.isNotificationEnabled,
            notes = bill.notes,
            lastNotifiedDate = bill.lastNotifiedDate,
            createdAt = bill.createdAt
        )
    }

    fun toDomain(entity: BillReminderEntity): BillReminder {
        return BillReminder(
            id = entity.id,
            name = entity.name,
            amount = entity.amount,
            billingDay = entity.billingDay,
            billingCycle = BillCycle.valueOf(entity.billingCycle),
            category = entity.category,
            isActive = entity.isActive,
            notifyDaysBefore = entity.notifyDaysBefore.split(",").mapNotNull { it.toIntOrNull() },
            isNotificationEnabled = entity.isNotificationEnabled,
            notes = entity.notes,
            lastNotifiedDate = entity.lastNotifiedDate,
            createdAt = entity.createdAt
        )
    }
}