package com.rudra.savingbuddy.data.local.dao

import androidx.room.*
import com.rudra.savingbuddy.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY nextBillingDate ASC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY nextBillingDate ASC")
    fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: Long): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions WHERE isActive = 1 AND nextBillingDate BETWEEN :startDate AND :endDate")
    suspend fun getSubscriptionsDueBetween(startDate: Long, endDate: Long): List<SubscriptionEntity>

    @Query("SELECT * FROM subscriptions WHERE isActive = 1")
    suspend fun getSubscriptionsForNotification(): List<SubscriptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscriptionById(id: Long)

    @Query("UPDATE subscriptions SET isActive = :isActive WHERE id = :id")
    suspend fun updateSubscriptionActiveStatus(id: Long, isActive: Boolean)

    @Query("UPDATE subscriptions SET nextBillingDate = :nextBillingDate WHERE id = :id")
    suspend fun updateNextBillingDate(id: Long, nextBillingDate: Long)

    @Query("UPDATE subscriptions SET notifyDaysBefore = :days WHERE id = :id")
    suspend fun updateNotifyDaysBefore(id: Long, days: Int)
}