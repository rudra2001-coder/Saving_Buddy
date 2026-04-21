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
            isPaid = false,
            lastPaidDate = null,
            nextDueDate = bill.createdAt + (30L * 24 * 60 * 60 * 1000),
            accountId = null,
            autoPay = false,
            remindDaysBefore = bill.notifyDaysBefore.firstOrNull() ?: 3,
            notes = bill.notes,
            createdAt = bill.createdAt
        )
    }

    fun toDomain(entity: BillReminderEntity): BillReminder {
        return BillReminder(
            id = entity.id,
            name = entity.name,
            amount = entity.amount,
            billingDay = entity.billingDay,
            billingCycle = try { BillCycle.valueOf(entity.billingCycle) } catch (e: Exception) { BillCycle.MONTHLY },
            category = entity.category,
            isActive = entity.isActive,
            notifyDaysBefore = listOf(entity.remindDaysBefore),
            isNotificationEnabled = true,
            notes = entity.notes,
            lastNotifiedDate = null,
            createdAt = entity.createdAt
        )
    }
}