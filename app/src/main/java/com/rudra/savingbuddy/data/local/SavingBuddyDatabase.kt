package com.rudra.savingbuddy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rudra.savingbuddy.data.local.dao.AccountBalanceHistoryDao
import com.rudra.savingbuddy.data.local.dao.AccountDao
import com.rudra.savingbuddy.data.local.dao.BillReminderDao
import com.rudra.savingbuddy.data.local.dao.BudgetDao
import com.rudra.savingbuddy.data.local.dao.ExpenseDao
import com.rudra.savingbuddy.data.local.dao.GoalDao
import com.rudra.savingbuddy.data.local.dao.IncomeDao
import com.rudra.savingbuddy.data.local.dao.SubscriptionDao
import com.rudra.savingbuddy.data.local.dao.TransferDao
import com.rudra.savingbuddy.data.local.dao.UserSettingsDao
import com.rudra.savingbuddy.data.local.entity.AccountBalanceHistoryEntity
import com.rudra.savingbuddy.data.local.entity.AccountEntity
import com.rudra.savingbuddy.data.local.entity.BillReminderEntity
import com.rudra.savingbuddy.data.local.entity.BudgetEntity
import com.rudra.savingbuddy.data.local.entity.ExpenseEntity
import com.rudra.savingbuddy.data.local.entity.GoalEntity
import com.rudra.savingbuddy.data.local.entity.IncomeEntity
import com.rudra.savingbuddy.data.local.entity.SubscriptionEntity
import com.rudra.savingbuddy.data.local.entity.TransferEntity
import com.rudra.savingbuddy.data.local.entity.UserSettingsEntity

@Database(
    entities = [
        IncomeEntity::class,
        ExpenseEntity::class,
        BudgetEntity::class,
        UserSettingsEntity::class,
        GoalEntity::class,
        BillReminderEntity::class,
        SubscriptionEntity::class,
        AccountEntity::class,
        TransferEntity::class,
        AccountBalanceHistoryEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class SavingBuddyDatabase : RoomDatabase() {
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun goalDao(): GoalDao
    abstract fun billReminderDao(): BillReminderDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun accountDao(): AccountDao
    abstract fun transferDao(): TransferDao
    abstract fun accountBalanceHistoryDao(): AccountBalanceHistoryDao

    companion object {
        const val DATABASE_NAME = "saving_buddy_database"
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN isArchived INTEGER DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN metadata TEXT")
                db.execSQL("CREATE TABLE IF NOT EXISTS `transfers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fromAccountId` INTEGER NOT NULL, `toAccountId` INTEGER NOT NULL, `amount` REAL NOT NULL, `fee` REAL DEFAULT 0, `note` TEXT, `timestamp` INTEGER NOT NULL, `status` TEXT DEFAULT 'COMPLETED', `reference` TEXT)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transfers_fromAccountId` ON `transfers` (`fromAccountId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transfers_toAccountId` ON `transfers` (`toAccountId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `account_balance_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `accountId` INTEGER NOT NULL, `date` INTEGER NOT NULL, `balance` REAL NOT NULL)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE income ADD COLUMN accountId INTEGER")
                db.execSQL("ALTER TABLE income ADD COLUMN tags TEXT")
                db.execSQL("ALTER TABLE income ADD COLUMN updatedAt INTEGER")
                db.execSQL("ALTER TABLE income ADD COLUMN isApproved INTEGER DEFAULT 1")
                db.execSQL("ALTER TABLE income ADD COLUMN approvedBy TEXT")
                db.execSQL("ALTER TABLE income ADD COLUMN approvedAt INTEGER")
                db.execSQL("ALTER TABLE expense ADD COLUMN accountId INTEGER")
                db.execSQL("ALTER TABLE expense ADD COLUMN paymentMethod TEXT")
                db.execSQL("ALTER TABLE expense ADD COLUMN tags TEXT")
                db.execSQL("ALTER TABLE expense ADD COLUMN updatedAt INTEGER")
                db.execSQL("ALTER TABLE expense ADD COLUMN isApproved INTEGER DEFAULT 1")
                db.execSQL("ALTER TABLE expense ADD COLUMN approvedBy TEXT")
                db.execSQL("ALTER TABLE expense ADD COLUMN receiptImagePath TEXT")
                db.execSQL("ALTER TABLE goals ADD COLUMN completedAt INTEGER")
                db.execSQL("ALTER TABLE goals ADD COLUMN updatedAt INTEGER")
                db.execSQL("ALTER TABLE goals ADD COLUMN notes TEXT")
                db.execSQL("ALTER TABLE goals ADD COLUMN iconEmoji TEXT")
                db.execSQL("ALTER TABLE goals ADD COLUMN colorHex TEXT")
                db.execSQL("ALTER TABLE goals ADD COLUMN allocationSourceAccountId INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE income ADD COLUMN recurringEndDate INTEGER")
                db.execSQL("ALTER TABLE expense ADD COLUMN recurringEndDate INTEGER")
                db.execSQL("ALTER TABLE accounts ADD COLUMN displayOrder INTEGER DEFAULT 0")
                db.execSQL("ALTER TABLE transfers ADD COLUMN category TEXT")
                db.execSQL("ALTER TABLE account_balance_history ADD COLUMN changeAmount REAL")
                db.execSQL("ALTER TABLE account_balance_history ADD COLUMN changeType TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bill_reminders ADD COLUMN accountId INTEGER")
                db.execSQL("ALTER TABLE bill_reminders ADD COLUMN autoPay INTEGER DEFAULT 0")
                db.execSQL("ALTER TABLE bill_reminders ADD COLUMN remindDaysBefore INTEGER DEFAULT 3")
                db.execSQL("ALTER TABLE budgets ADD COLUMN rollover INTEGER DEFAULT 0")
                db.execSQL("ALTER TABLE budgets ADD COLUMN alertThreshold REAL DEFAULT 0.8")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `subscriptions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `billingCycle` TEXT NOT NULL,
                        `nextBillingDate` INTEGER NOT NULL,
                        `category` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL DEFAULT 1,
                        `notifyDaysBefore` INTEGER NOT NULL DEFAULT 3,
                        `notes` TEXT,
                        `accountId` INTEGER,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_subscriptions_nextBillingDate` ON `subscriptions` (`nextBillingDate`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_subscriptions_isActive` ON `subscriptions` (`isActive`)")
            }
        }

        fun getMigrations(): Array<Migration> = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6
        )
    }
}
