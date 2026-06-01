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
            isPaid = bill.paidMonths.isNotEmpty(),
            lastPaidDate = null,
            nextDueDate = calculateNextDueDate(bill.billingDay, bill.billingCycle, bill.createdAt),
            accountId = bill.payFromAccountId,
            autoPay = false,
            remindDaysBefore = bill.notifyDaysBefore.firstOrNull() ?: 3,
            notes = bill.notes,
            createdAt = bill.createdAt,
            payFromAccountId = bill.payFromAccountId,
            paidMonths = if (bill.paidMonths.isEmpty()) null else bill.paidMonths.joinToString(","),
            lastProcessedMonth = bill.lastProcessedMonth
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
            createdAt = entity.createdAt,
            payFromAccountId = entity.payFromAccountId ?: entity.accountId,
            paidMonths = entity.paidMonths?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            lastProcessedMonth = entity.lastProcessedMonth
        )
    }

    private fun calculateNextDueDate(billingDay: Int, billingCycle: BillCycle, createdAt: Long): Long {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = createdAt
            set(java.util.Calendar.DAY_OF_MONTH, billingDay.coerceAtMost(28))
        }
        return cal.timeInMillis
    }
}
