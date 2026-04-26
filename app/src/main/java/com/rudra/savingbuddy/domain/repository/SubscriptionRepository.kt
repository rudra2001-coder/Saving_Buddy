package com.rudra.savingbuddy.domain.repository

import com.rudra.savingbuddy.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getAllSubscriptions(): Flow<List<Subscription>>
    fun getActiveSubscriptions(): Flow<List<Subscription>>
    suspend fun getSubscriptionById(id: Long): Subscription?
    suspend fun getSubscriptionsDueBetween(startDate: Long, endDate: Long): List<Subscription>
    suspend fun getSubscriptionsForNotification(): List<Subscription>
    suspend fun insertSubscription(subscription: Subscription): Long
    suspend fun updateSubscription(subscription: Subscription)
    suspend fun deleteSubscription(id: Long)
    suspend fun updateSubscriptionActiveStatus(id: Long, isActive: Boolean)
    suspend fun updateNextBillingDate(id: Long, nextBillingDate: Long)
}