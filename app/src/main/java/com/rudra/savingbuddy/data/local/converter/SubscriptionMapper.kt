package com.rudra.savingbuddy.data.local.converter

import com.rudra.savingbuddy.data.local.entity.SubscriptionEntity
import com.rudra.savingbuddy.domain.model.BillingCycle
import com.rudra.savingbuddy.domain.model.Subscription

object SubscriptionMapper {
    fun toEntity(subscription: Subscription): SubscriptionEntity {
        return SubscriptionEntity(
            id = subscription.id,
            name = subscription.name,
            amount = subscription.amount,
            billingCycle = subscription.billingCycle.name,
            nextBillingDate = subscription.nextBillingDate,
            category = subscription.category,
            isActive = subscription.isActive,
            notifyDaysBefore = 3,
            notes = subscription.notes,
            accountId = null,
            createdAt = subscription.createdAt
        )
    }

    fun toDomain(entity: SubscriptionEntity): Subscription {
        return Subscription(
            id = entity.id,
            name = entity.name,
            amount = entity.amount,
            billingCycle = try { 
                BillingCycle.valueOf(entity.billingCycle) 
            } catch (e: Exception) { 
                BillingCycle.MONTHLY 
            },
            nextBillingDate = entity.nextBillingDate,
            category = entity.category,
            isActive = entity.isActive,
            notes = entity.notes,
            createdAt = entity.createdAt
        )
    }
}