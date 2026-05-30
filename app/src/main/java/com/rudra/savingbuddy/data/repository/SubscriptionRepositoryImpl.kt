package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.converter.SubscriptionMapper
import com.rudra.savingbuddy.data.local.dao.SubscriptionDao
import com.rudra.savingbuddy.domain.model.Subscription
import com.rudra.savingbuddy.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) : SubscriptionRepository {

    override fun getAllSubscriptions(): Flow<List<Subscription>> =
        subscriptionDao.getAllSubscriptions().map { entities ->
            entities.map { SubscriptionMapper.toDomain(it) }
        }

    override fun getActiveSubscriptions(): Flow<List<Subscription>> =
        subscriptionDao.getActiveSubscriptions().map { entities ->
            entities.map { SubscriptionMapper.toDomain(it) }
        }

    override suspend fun getSubscriptionById(id: Long): Subscription? =
        subscriptionDao.getSubscriptionById(id)?.let { SubscriptionMapper.toDomain(it) }

    override suspend fun getSubscriptionsDueBetween(startDate: Long, endDate: Long): List<Subscription> =
        subscriptionDao.getSubscriptionsDueBetween(startDate, endDate).map { SubscriptionMapper.toDomain(it) }

    override suspend fun getSubscriptionsForNotification(): List<Subscription> =
        subscriptionDao.getSubscriptionsForNotification().map { SubscriptionMapper.toDomain(it) }

    override suspend fun insertSubscription(subscription: Subscription): Long =
        subscriptionDao.insertSubscription(SubscriptionMapper.toEntity(subscription))

    override suspend fun updateSubscription(subscription: Subscription) =
        subscriptionDao.updateSubscription(SubscriptionMapper.toEntity(subscription))

    override suspend fun deleteSubscription(id: Long) =
        subscriptionDao.deleteSubscriptionById(id)

    override suspend fun updateSubscriptionActiveStatus(id: Long, isActive: Boolean) =
        subscriptionDao.updateSubscriptionActiveStatus(id, isActive)

    override suspend fun updateNextBillingDate(id: Long, nextBillingDate: Long) =
        subscriptionDao.updateNextBillingDate(id, nextBillingDate)
}