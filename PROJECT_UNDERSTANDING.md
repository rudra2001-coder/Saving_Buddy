# Saving Buddy — Full Project Understanding

> **Last Updated:** 2026-05-20
> **Package:** `com.rudra.savingbuddy`
> **Min SDK:** 28 | **Target SDK:** 36 | **Kotlin:** 2.3.20

---

## Table of Contents
1. [App Overview](#1-app-overview)
2. [Tech Stack & Dependencies](#2-tech-stack--dependencies)
3. [Architecture](#3-architecture)
4. [Project Structure](#4-project-structure)
5. [Database (Room)](#5-database-room)
6. [Dependency Injection (Hilt)](#6-dependency-injection-hilt)
7. [Navigation](#7-navigation)
8. [Theme & UI System](#8-theme--ui-system)
9. [Domain Models](#9-domain-models)
10. [Data Layer](#10-data-layer)
11. [Repository Layer](#11-repository-layer)
12. [UI Screens](#12-ui-screens)
13. [Utility Layer](#13-utility-layer)
14. [Background Workers (WorkManager)](#14-background-workers-workmanager)
15. [Widget](#15-widget)
16. [Data Flow Example](#16-data-flow-example)
17. [Backup & Restore System](#17-backup--restore-system)
18. [Gamification System](#18-gamification-system)
19. [Key Features Matrix](#19-key-features-matrix)

---

## 1. App Overview

**Saving Buddy** is a full-featured personal finance management Android app built with Jetpack Compose. It is designed for the Bangladesh market (default currency BDT, bKash/Nagad/Rocket support) but supports multiple currencies.

### Core Purpose
Track income, expenses, budgets, savings goals, bills, transfers between accounts, investments, subscriptions, and generate reports — all in one app with a premium dark-fintech UI.

### Target User
- Individuals managing personal finances
- Users of Bangladesh mobile financial services (bKash, Nagad, Rocket, Upay)
- Anyone needing a comprehensive expense tracker with multi-account support

---

## 2. Tech Stack & Dependencies

| Category | Library | Version |
|---|---|---|
| **UI** | Jetpack Compose (Material 3) | BOM 2026.03.01 |
| **Navigation** | Navigation Compose | 2.9.7 |
| **Database** | Room | 2.8.4 |
| **DI** | Dagger Hilt | 2.59.2 |
| **Background** | WorkManager | 2.11.2 |
| **Lifecycle** | Lifecycle (ViewModel) | 2.10.0 |
| **Networking** | Manual URL (Exchange rates) | — |
| **Serialization** | kotlinx-serialization-json | 1.6.0 |
| **Coroutines** | kotlinx-coroutines | 1.10.2 |
| **ML** | ML Kit Text Recognition | 16.0.0 |
| **Biometric** | AndroidX Biometric | 1.1.0 |
| **Build** | AGP 9.2.1, Kotlin 2.3.20, KSP 2.3.6 |
| **Compose Compiler** | Kotlin Compose plugin | Bundled |

---

## 3. Architecture

**Clean Architecture + MVVM pattern with Hilt DI.**

```
Presentation (UI Layer)
    └─ Compose Screens + ViewModels (StateFlow)
           │
    Domain Layer (pure Kotlin)
        ├─ Models (data classes)
        └─ Repository Interfaces
           │
    Data Layer
        ├─ Repository Implementations
        ├─ Room Database → DAOs → Entities
        └─ Backup Manager (JSON serialization)
```

### Key Architectural Decisions
- **Single Activity** (`MainActivity`) with Compose navigation
- **StateFlow** for reactive UI state management
- **Hilt** for dependency injection throughout
- **Repository pattern** with interfaces in domain layer
- **Room** with 6 migrations (v1 → v7)
- **WorkManager** for background notifications and backup
- **Compose Material 3** theming with dark/light/AMOLED modes

---

## 4. Project Structure

```
com.rudra.savingbuddy/
├── SavingBuddyApp.kt          # @HiltAndroidApp, WorkManager config
├── MainActivity.kt             # Single activity, edge-to-edge, theme init
│
├── di/
│   ├── DatabaseModule.kt       # Provides Room DB + all DAOs + SharedPrefs
│   └── RepositoryModule.kt     # Binds all Repository interfaces → Impls
│
├── data/
│   ├── BackupManager.kt        # Full JSON export/import of all entities
│   ├── BackupWorker.kt         # WorkManager periodic backup worker
│   ├── models/
│   │   └── BackupModels.kt     # Serializable backup DTOs + enums
│   └── local/
│       ├── SavingBuddyDatabase.kt  # Room DB (v7, 11 entities, 6 migrations)
│       ├── dao/
│       │   ├── IncomeDao.kt
│       │   ├── ExpenseDao.kt
│       │   ├── BudgetDao.kt
│       │   ├── GoalDao.kt
│       │   ├── BillReminderDao.kt
│       │   ├── AccountDao.kt
│       │   ├── TransferDao.kt
│       │   ├── InvestmentDao.kt
│       │   ├── SubscriptionDao.kt
│       │   ├── UserSettingsDao.kt
│       │   └── (AccountBalanceHistoryDao, CategoryTotal)
│       ├── entity/
│       │   ├── AccountEntity.kt       (+ TransferEntity, AccountBalanceHistoryEntity)
│       │   ├── IncomeEntity.kt        (+ ExpenseEntity, BudgetEntity, GoalEntity,
│       │   │                            UserSettingsEntity, BillReminderEntity)
│       │   ├── InvestmentEntity.kt
│       │   └── SubscriptionEntity.kt
│       └── converter/
│           ├── AccountMapper.kt
│           ├── ExpenseMapper.kt
│           ├── IncomeMapper.kt
│           ├── BillReminderMapper.kt
│           ├── InvestmentMapper.kt
│           └── SubscriptionMapper.kt
│   └── repository/
│       ├── AccountRepositoryImpl.kt
│       ├── BillReminderRepositoryImpl.kt
│       ├── BudgetRepositoryImpl.kt
│       ├── ExpenseRepositoryImpl.kt
│       ├── FusionRepositoryImpl.kt      # ~600 lines — unified transactions, insights, health
│       ├── GoalRepositoryImpl.kt
│       ├── IncomeRepositoryImpl.kt
│       ├── InvestmentRepositoryImpl.kt
│       ├── SettingsRepositoryImpl.kt
│       └── SubscriptionRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Account.kt           # Account, AccountType, AccountProvider, Transfer, etc.
│   │   ├── Income.kt            # Income, IncomeCategory, RecurringInterval, Tag, etc.
│   │   ├── Expense.kt           # Expense, ExpenseCategory, LateFeeRule, DryRunResult
│   │   ├── Budget.kt            # Budget, BudgetAlert
│   │   ├── Goal.kt              # Goal, GoalCategory
│   │   ├── BillReminder.kt      # BillReminder, BillCycle
│   │   ├── UserSettings.kt      # UserSettings (all app preferences)
│   │   ├── Investment.kt        # Investment domain model
│   │   ├── Subscription.kt      # Subscription, BillingCycle
│   │   ├── FusionModels.kt      # UnifiedTransaction, NetWorthSummary, AccountHealth,
│   │   │                        # FusionInsight, TransferPattern, GoalFundingSuggestion
│   │   ├── Gamification.kt      # Achievement, SavingsStreak, SavingsScore
│   │   ├── Currency.kt          # SupportedCurrencies
│   │   ├── RolloverBudget.kt    # RolloverBudget logic
│   │   └── AppNotification.kt   # Notification model
│   └── repository/
│       ├── AccountRepository.kt
│       ├── BillReminderRepository.kt
│       ├── BudgetRepository.kt
│       ├── ExpenseRepository.kt
│       ├── FusionRepository.kt
│       ├── GoalRepository.kt
│       ├── IncomeRepository.kt
│       ├── InvestmentRepository.kt
│       ├── SettingsRepository.kt
│       ├── SubscriptionRepository.kt
│       └── TransferRepository.kt
│
├── ui/
│   ├── theme/
│   │   ├── Color.kt             # Full premium fintech color palette
│   │   ├── Theme.kt             # Light/Dark/AMOLED color schemes
│   │   ├── AmoledTheme.kt       # Pure black dark theme
│   │   ├── Type.kt              # Full typography scale
│   │   └── PremiumComponents.kt # GlassCard, GradientCard, PremiumButton, etc.
│   ├── components/
│   │   ├── AnimatedNumber.kt    # Counting animation composable
│   │   ├── Charts.kt            # Pie/donut/bar chart composables
│   │   ├── ExpenseDialog.kt     # Add expense dialog
│   │   ├── IncomeDialog.kt      # Add income dialog
│   │   └── SummaryCard.kt       # Summary statistics card
│   ├── navigation/
│   │   ├── Navigation.kt        # Screen sealed class (60+ routes)
│   │   └── MainNavigation.kt    # NavHost, bottom bar, all route registrations
│   └── screens/
│       ├── dashboard/
│       │   ├── DashboardScreen.kt       # Main dashboard (1037 lines)
│       │   ├── DashboardViewModel.kt    # Dashboard logic (316 lines)
│       │   └── DashboardComponents.kt   # Reusable cards/charts (790 lines)
│       ├── add/
│       │   ├── AddExpenseScreen.kt      # Full expense entry form (932 lines)
│       │   └── AddIncomeScreen.kt       # Full income entry form (892 lines)
│       ├── accounts/
│       │   ├── AccountsScreen.kt
│       │   ├── AccountsViewModel.kt
│       │   ├── AccountDetailScreen.kt
│       │   ├── AccountDetailViewModel.kt
│       │   ├── AddAccountScreen.kt
│       │   ├── AddAccountViewModel.kt
│       │   ├── TransferScreen.kt
│       │   └── TransferViewModel.kt
│       ├── backup/BackupScreen.kt + BackupViewModel.kt
│       ├── bills/BillRemindersScreen.kt + BillRemindersViewModel.kt
│       ├── budget/BudgetScreen.kt + BudgetViewModel.kt
│       ├── calculator/CalculatorScreen.kt + CalculatorViewModel.kt
│       ├── calendar/CalendarScreen.kt + CalendarViewModel.kt
│       ├── changelog/ChangelogScreen.kt
│       ├── currency/CurrencyConverterScreen.kt
│       ├── expense/ExpenseScreen.kt + ExpenseViewModel.kt + ExpenseDetailScreen.kt
│       ├── export/ExportScreen.kt
│       ├── features/FeaturesScreen.kt + AdvancedFeaturesScreen.kt
│       ├── fusion/FusionScreen.kt + FusionViewModel.kt
│       ├── gamification/GamificationScreen.kt + GamificationModels.kt
│       ├── goals/GoalsScreen.kt + GoalsViewModel.kt
│       ├── income/IncomeScreen.kt + IncomeViewModel.kt
│       ├── investment/InvestmentScreen.kt + InvestmentViewModel.kt
│       ├── language/LanguageScreen.kt
│       ├── notifications/NotificationsScreen.kt
│       ├── onboarding/OnboardingScreen.kt
│       ├── recurring/RecurringScreen.kt + RecurringViewModel.kt
│       ├── reports/ReportsScreen.kt + ReportsViewModel.kt + AnalyticsScreen.kt + AnalyticsViewModel.kt
│       ├── scanner/ReceiptScannerScreen.kt
│       ├── settings/SettingsScreen.kt + SettingsViewModel.kt + LegalScreens.kt
│       ├── subscriptions/SubscriptionManagerScreen.kt
│       └── transactionhistory/TransactionHistoryScreen.kt + TransactionHistoryViewModel.kt
│
└── util/
    ├── CurrencyFormatter.kt     # Multi-currency formatting (BDT/USD/EUR etc.)
    ├── DateUtils.kt             # Date range calculations, formatting
    ├── ExchangeManager.kt       # Live forex rates via Frankfurter API
    ├── ExportManager.kt         # CSV/TEXT/JSON export
    ├── ReportGenerator.kt       # PDF financial report generation
    ├── BiometricHelper.kt       # Fingerprint authentication
    ├── PrivacyHelper.kt         # Amount hiding, haptic feedback
    ├── LanguageManager.kt       # Multi-language support, AppStrings
    ├── VoiceInputHelper.kt      # Speech recognition + amount parsing
    ├── SmsTransactionParser.kt  # SMS-based transaction detection
    ├── NotificationWorker.kt    # Daily reminder + weekly digest + emergency
    ├── BillNotificationWorker.kt   # Bill due date notifications
    └── SubscriptionNotificationWorker.kt  # Subscription renewal notifications

├── widget/
    └── ExpenseWidget.kt         # Home screen widget (minimal)
```

---

## 5. Database (Room)

### Database: `saving_buddy_database` (version 7)

### Entities (11 tables)

| Table | Key Fields | Purpose |
|---|---|---|
| **income** | id, source, amount, category, date, isRecurring, recurringInterval, accountId, tags, isApproved | All income records |
| **expense** | id, amount, category, date, isRecurring, recurringInterval, accountId, paymentMethod, receiptImagePath, isApproved | All expense records |
| **budgets** | id, category, monthlyLimit, spent, month, year, rollover, alertThreshold | Budget limits per category |
| **goals** | id, name, targetAmount, currentAmount, category, deadline, isCompleted, autoAllocate, notes, iconEmoji, colorHex | Savings goals |
| **bill_reminders** | id, name, amount, billingDay, billingCycle, isActive, isPaid, accountId, autoPay, remindDaysBefore | Recurring bill reminders |
| **accounts** | id, name, type, provider, accountNumber, balance, dailyLimit, linkedGoalId, isArchived, displayOrder, metadata | Financial accounts (Wallet, Bank, bKash etc.) |
| **transfers** | id, fromAccountId, toAccountId, amount, fee, status, reference, category | Money transfers between accounts |
| **account_balance_history** | id, accountId, date, balance, changeAmount, changeType | Daily balance snapshots |
| **subscriptions** | id, name, amount, billingCycle, nextBillingDate, category, isActive, notifyDaysBefore | Recurring subscription tracking |
| **investments** | id, name, type, amount, currentValue, purchaseDate, notes | Investment portfolio |
| **user_settings** | key (PK), value, updatedAt | Key-value settings store |

### Migration History (1→7)

| Migration | Changes |
|---|---|
| **1→2** | Added `isArchived`, `metadata` to accounts; created `transfers` + `account_balance_history` tables |
| **2→3** | Added approval/workflow fields to income/expense; enhanced goals (notes, iconEmoji, colorHex, allocation, completedAt) |
| **3→4** | Added `recurringEndDate` to income/expense; `displayOrder` to accounts; `category` to transfers; enhanced balance history |
| **4→5** | Added `accountId`, `autoPay`, `remindDaysBefore` to bill_reminders; `rollover`, `alertThreshold` to budgets |
| **5→6** | Created `subscriptions` table |
| **6→7** | Created `investments` table |

### Key DAO Features
- **IncomeDao/ExpenseDao**: Date range queries, category grouping, totals, recurring item queries
- **AccountDao**: Balance updates, balance history recording, active/archived filtering
- **TransferDao**: Sent/received totals, daily limits, pattern detection
- **GoalDao**: Active goal queries, amount updates, allocation source
- **BillReminderDao**: Notification queries, last notified date updates
- **SubscriptionDao**: Upcoming billing queries, notification filtering

---

## 6. Dependency Injection (Hilt)

### `DatabaseModule` (`@InstallIn(SingletonComponent::class)`)
- **Provides** `SavingBuddyDatabase` (Room builder with migrations)
- **Provides** all 11 DAOs from the database
- **Provides** `SharedPreferences` ("dashboard_prefs")

### `RepositoryModule` (`@InstallIn(SingletonComponent::class)`)
- **Binds all 10 repository interfaces → implementations**
- `IncomeRepositoryImpl(incomeDao, accountDao)`
- `ExpenseRepositoryImpl(expenseDao, accountDao)`
- `FusionRepositoryImpl(accountDao, incomeDao, expenseDao, transferDao, goalDao)`
- `AccountRepositoryImpl(accountDao, transferDao, balanceHistoryDao)`
- And others...

### Injection Points
- `SavingBuddyApp` — `HiltWorkerFactory`
- `MainActivity` — `SettingsRepository`
- All ViewModels — `@HiltViewModel` with `@Inject constructor`
- All Workers — `@HiltWorker` with `@AssistedInject`

---

## 7. Navigation

### Route System
- `Screen` sealed class with 60+ route objects
- Each route has: `route` (String), `title`, `icon` (ImageVector)
- NavHost in `MainNavigation.kt` registers ~35 active routes

### Bottom Navigation (5 tabs)
1. **Dashboard** — Main overview
2. **Features** — Feature hub
3. **Reports** — Financial reports
4. **Investment Tracker** — Portfolio tracking
5. **Settings** — App configuration

### Onboarding Flow
- Checks `SharedPreferences("onboarding")` for `"completed"` flag
- If not completed → shows `OnboardingScreen` first
- After completion → navigates to `Dashboard` and clears back stack

---

## 8. Theme & UI System

### Color System (`Color.kt`)
- **Premium Dark Fintech Theme**
- Primary: Green (`#22C55E`), Red (`#EF4444`), Orange (`#F59E0B`)
- Background: Dark blue (`#0B1220`), Card (`#111827`)
- Full category color palette (Food, Transport, Bills, Shopping, etc.)
- Account colors (bKash Purple, Nagad Orange, Bank Blue)
- Gradient presets (greenGradient, tealGradient, purpleGradient, etc.)

### Theme Modes (`Theme.kt`)
- **Light Mode** — Clean white/gray theme
- **Dark Mode** — Premium dark fintech look
- **AMOLED Mode** — Pure black (`#000000`) backgrounds
- **Scheduled Dark Mode** — Auto-switch based on configured hours

### Premium Components (`PremiumComponents.kt`)
- `GlassCard` — Translucent card with glassmorphism effect
- `GradientCard` — Card with gradient background + shadow
- `PremiumButton` — Animated gradient button
- `PremiumOutlineButton` — Styled outlined button
- `PremiumTextField` — Styled input field
- `EmptyStateView` — Icon + message + action
- `SkeletonBox` — Loading skeleton animation
- `AnimatedAmount` — Counting number animation
- `TrustBadge` — Security indicator
- `SuccessAnimation` — Checkmark animation

### Typography (`Type.kt`)
Full Material 3 typography scale from `displayLarge` to `labelSmall` with premium fintech styling.

---

## 9. Domain Models

### Core Financial Models

| Model | Key Fields |
|---|---|
| **Income** | id, source, amount, category (IncomeCategory), date, isRecurring, recurringInterval, notes, accountId, tags, currency, status, isApproved |
| **Expense** | id, amount, category (ExpenseCategory), date, notes, isRecurring, recurringInterval, accountId, paymentMethod, tags, receiptImagePath, isApproved |
| **Account** | id, name, type (AccountType), provider, accountNumber, balance, dailyLimit, linkedGoalId |
| **Transfer** | id, fromAccountId, toAccountId, amount, fee, note, status, reference |
| **Goal** | id, name, targetAmount, currentAmount, category, deadline, progress, daysRemaining |
| **Budget** | id, monthlyLimit, month, year, categoryLimits, enableRollover, alertThreshold |
| **BillReminder** | id, name, amount, billingDay, billingCycle, isActive, notifyDaysBefore |
| **Subscription** | id, name, amount, billingCycle, nextBillingDate, category, isActive |
| **Investment** | id, name, type, amount, currentValue, purchaseDate |

### Account System (Bangladesh-focused)
```kotlin
AccountType: WALLET, BANK, MOBILE_BANKING, DIGITAL_WALLET, CREDIT_CARD

AccountProvider (with daily limits):
  - BKASH (25000 BDT/day)
  - NAGAD (25000 BDT/day)
  - ROCKET (25000 BDT/day)
  - UPAY (20000 BDT/day)
  - DBBL, CITY_BANK, BRAC_BANK, STANDARD_CHARTERED, HSBC
  - CASH, PAYPAL, PAYONEER, STRIPE
```

### Expense Categories (15)
```
FOOD, TRANSPORT, BILLS, SHOPPING, ENTERTAINMENT, HEALTH, EDUCATION,
GIFTS, TRAVEL, SUBSCRIPTIONS, RENT, UTILITY, INSURANCE, TAX, EMI
```

### Income Categories (7)
```
SALARY, FREELANCE, INVESTMENTS, BUSINESS, GIFTS, RENTAL, REFUND, OTHERS
```

### Recurring Intervals
```
DAILY, WEEKLY, BI_WEEKLY, MONTHLY, QUARTERLY, YEARLY,
LAST_DAY_OF_MONTH, LAST_WEEKDAY_OF_MONTH, EVERY_45_DAYS, EVERY_2_MONTHS
```

### Fusion Models (Unified Data)
- `UnifiedTransaction` — Combined income/expense/transfer view
- `NetWorthSummary` — Assets vs liabilities breakdown
- `AccountHealth` — Account health status with recommendations
- `FusionInsight` — Smart insights with priority levels
- `TransferPattern` — Frequent transfer route detection
- `GoalFundingSuggestion` — Suggested goal allocations

### Gamification Models
- `Achievement` — 7 predefined achievements
- `SavingsStreak` — Consecutive days under budget tracking
- `SavingsScore` — Grade (A+ to F) based on savings rate, budget adherence, goal progress, streak

---

## 10. Data Layer

### Mapper Pattern
Each entity has a corresponding mapper in `data/local/converter/`:
- Entity → Domain model conversion
- Used by repository implementations
- Covers: Account, BillReminder, Expense, Income, Investment, Subscription

### Backup System (`BackupManager.kt`)
- Full export/import of ALL 11 Room entities to JSON via `kotlinx.serialization`
- Backup covers: income, expense, accounts, goals, budgets, bill_reminders, transfers, subscriptions, investments, **user_settings**, **account_balance_history** — restoring returns app to exact backup-time state
- Backup locations: INTERNAL, DOWNLOADS, **CUSTOM** (user picks any folder via SAF `OpenDocumentTree`)
- Backup frequencies: DAILY, WEEKLY, MONTHLY
- Creates shareable JSON files via FileProvider
- Backup data includes versioning (v2), all entities + settings
- Auto-backup via WorkManager (`BackupWorker`) — uses saved location settings (not hardcoded)
- Custom folder persisted via SAF tree URI with `takePersistableUriPermission`; writes via `DocumentFile`
- **Manual export**: "Quick Backup" saves to configured location; "Export to Folder" lets user pick destination via SAF
- **Manual import**: "Import Backup (JSON)" uses `OpenDocument` to pick any JSON file from any folder
- **Success message**: "backup is complete application is now ready to use" shown on completion
- Dependencies: `androidx.documentfile:documentfile:1.0.1`
- DAOs updated: `deleteAll()` added to `BillReminderDao`, `SubscriptionDao`, `AccountBalanceHistoryDao`; `getAllBalanceHistory()` + `insertAllBalanceHistory()` added to `AccountBalanceHistoryDao`

---

## 11. Repository Layer

| Repository | Dependencies | Key Operations |
|---|---|---|
| **IncomeRepositoryImpl** | IncomeDao, AccountDao | CRUD, date range totals, recurring detection |
| **ExpenseRepositoryImpl** | ExpenseDao, AccountDao | CRUD, date range totals, category aggregation |
| **BudgetRepositoryImpl** | BudgetDao | Get/set budget, category limits |
| **GoalRepositoryImpl** | GoalDao | CRUD, active goals, amount updates |
| **BillReminderRepositoryImpl** | BillReminderDao | CRUD, notification queries |
| **AccountRepositoryImpl** | AccountDao, TransferDao, BalanceHistoryDao | CRUD, balance updates, history |
| **TransferRepositoryImpl** | TransferDao | CRUD, sent/received totals |
| **SubscriptionRepositoryImpl** | SubscriptionDao | CRUD, upcoming billing |
| **InvestmentRepositoryImpl** | InvestmentDao | CRUD, portfolio summary |
| **SettingsRepositoryImpl** | UserSettingsDao | Get/update all preferences |
| **FusionRepositoryImpl** | AccountDao, IncomeDao, ExpenseDao, TransferDao, GoalDao | Unified transactions, net worth, account health, smart insights, goal funding suggestions, transfer processing with daily limit checks |

### FusionRepositoryImpl — The "Brain" of the App
- Merges income, expense, and transfer data into unified timeline
- Calculates net worth:
  ```
  totalAssets = sum of positive balances
  totalLiabilities = sum of negative balances (absolute)
  netWorth = sum of all balances
  assetsByType = breakdown by AccountType
  ```
- **Account Health** — Daily limit usage percentage → GOOD/MEDIUM/LOW/CRITICAL
- **Fusion Insights** — 8 types of smart alerts:
  1. SPENDING_WARNING (>90% income spent)
  2. TRANSFER_HABIT (5+ transfers/day)
  3. BALANCE_ALERT (low balance / daily limit warning for BD providers)
  4. TREND_INFO (high weekly spending >5000 BDT)
  5. SAVING_OPPORTUNITY (<70% income spent)
  6. GOAL_SUGGESTION (goal progress tracking)
  7. Frequent transfer route detection
  8. Provider-specific daily limits (bKash/Nagad: 25000, Rocket: 25000, etc.)
- **Transfer Processing**: Balance validation, daily limit check, fee calculation (bKash→Bank: 10 BDT, Nagad→Bank: 5 BDT), generates reference numbers
- **Goal Allocation**: Deducts from account, adds to goal

---

## 12. UI Screens

### Dashboard (`DashboardScreen.kt` — 1037 lines)
The main screen — most complex UI in the app:
- **Header** — Date display, sync icon
- **Net Balance Card** — Hero card with gradient, account picker, income/expense strip
- **Quick Actions Row** — 8 action chips (Income, Expense, Goals, Budget, Bills, Export, Calendar, Fusion)
- **Today + Budget 2-column card** — Today stats + animated budget progress bar
- **Account Health Card** — List of accounts with health status (GOOD/LOW/CRITICAL)
- **Net Worth Card** — Net worth + total assets
- **Monthly Summary Card** — Income/Expenses/Savings pills + budget warning
- **Savings Goal Card** — Animated progress indicator
- **Category Breakdown Card** — Top 5 categories with colors
- **Monthly Trend Card** — Sparkline chart (Canvas-based)
- **Upcoming Bills Card** — Due date tracking with color coding
- **Insights Card** — Smart financial insights
- **Recent Transactions Card** — Last 5 transactions with type icons
- **Animated FAB Menu** — Rotating FAB with 3 options (Add Income, Add Expense, Add Account)
- **Pull-to-refresh** — PullToRefreshBox
- **Account Picker Dialog** — Switch between accounts

### Add Expense Screen (`AddExpenseScreen.kt` — 932 lines)
- Hero amount card with gradient background
- Quick amount chips (100, 200, 500, 1000, 2000, 5000)
- Account selector with dropdown
- Category selector (horizontal scroll, 15 categories with emojis)
- Date/time pickers
- Recurring toggle with interval selector (8 intervals)
- Advanced section: Notes, Currency selector
- Validation + save with haptic feedback

### Add Income Screen (`AddIncomeScreen.kt` — 892 lines)
- Same structure as Add Expense
- Additional source/title field
- Income-specific categories with emojis
- Quick amounts: 500, 1000, 2000, 5000, 10000, 20000

### Settings Screen (`SettingsScreen.kt` — 399 lines)
- Organized in collapsible sections with filter chips
- Sections: Appearance, Budget & Goals, Accounts, Notifications, Data & Privacy, Language, Smart Features, About & Support
- Quick stats header (Accounts, Goals, Bills count)
- Dialogs: Budget, Currency, Theme, Start of Week, Hour pickers
- Theme dialog with Dark Mode, AMOLED, Scheduled options

### Other Screens
| Screen | Purpose |
|---|---|
| **AccountsScreen** | List all accounts with balances |
| **AccountDetailScreen** | Single account view with transactions |
| **AddAccountScreen** | Create new account (Wallet/Bank/Mobile Banking etc.) |
| **TransferScreen** | Transfer money between accounts |
| **BudgetScreen** | Budget management with category limits |
| **BillRemindersScreen** | Manage recurring bills |
| **GoalsScreen** | Savings goals management |
| **ReportsScreen** | Financial reports |
| **AnalyticsScreen** | Advanced analytics/charts |
| **FusionScreen** | Unified transaction view + insights |
| **ExpenseScreen/IncomeScreen** | History lists with filters |
| **BackupScreen** | Backup/restore management |
| **CalculatorScreen** | Built-in calculator |
| **CalendarScreen** | Calendar view of transactions |
| **CurrencyConverterScreen** | Live forex conversion |
| **ReceiptScannerScreen** | ML Kit receipt OCR |
| **InvestmentScreen** | Portfolio tracking |
| **SubscriptionManagerScreen** | Subscription management |
| **TransactionHistoryScreen** | Full transaction history |
| **OnboardingScreen** | First-run welcome flow |
| **GamificationScreen** | Achievements, streaks, score |
| **LanguageScreen** | Language selection (14 languages) |
| **NotificationsScreen** | Notification history |
| **ChangelogScreen** | What's new |
| **AdvancedFeaturesScreen** | Feature hub |
| **FeaturesScreen** | Feature grid |

---

## 13. Utility Layer

### CurrencyFormatter
- Singleton object
- Supports 6 currencies: BDT (৳), USD ($), EUR (€), GBP (£), INR (₹), PKR (₨)
- Functions: `format()` (full), `formatCompact()` (1K/1M), `formatBDT()`
- Currency configurable via `setCurrency()`

### DateUtils
- Singleton object
- Functions: `getStartOfDay()`, `getEndOfDay()`, `getStartOfMonth()`, `getEndOfMonth()`, `getStartOfWeek()`, `getEndOfWeek()`
- Formatting: `formatDate()`, `formatTime()`, `formatMonthYear()`, `formatShortDate()`
- Grouping: `getDateGroup()` (Today/Yesterday/This Week/Month)

### ExchangeManager
- Live forex rates from Frankfurter API (`api.frankfurter.app`)
- 14 supported currencies (BDT, USD, EUR, GBP, INR, PKR, JPY, CNY, KRW, SAR, AED, MYR, SGD, AUD)
- Functions: `convert()`, `getRate()`, `getRateText()`
- Falls back to hardcoded default rates
- Listener pattern for rate updates

### ExportManager
- Supports CSV, TEXT, JSON formats
- Export types: INCOME, EXPENSE, ALL
- Generates formatted files with headers and summaries
- Shares via Intent with FileProvider

### ReportGenerator
- Generates PDF financial reports (Android Canvas API)
- Report types: Monthly Summary, Annual, Custom Range, Category Analysis, Full Statement
- Report data includes: totals, savings rate, category breakdown, monthly trends, insights
- Generates formatted PDF with title, summary, stats, category analysis, trend data, insights

### BiometricHelper
- Checks biometric availability and status
- Shows BiometricPrompt (fingerprint)
- Returns: AVAILABLE, NO_HARDWARE, NOT_ENROLLED, UNAVAILABLE

### VoiceInputHelper
- Android SpeechRecognizer integration
- Parses spoken text for amounts (digits + word numbers)
- Detection patterns: "500 rupees", "fifty rupees", "five hundred"
- Returns parsed `amount: Double?` and `description: String`

### SmsTransactionParser
- Regex-based SMS parsing for bank transactions
- Detects expense patterns (debited, paid, spent, UPI, ₹)
- Detects income patterns (credited, received, deposited, salary, NEFT, IMPS)
- Auto-categorization via keyword matching (Zomato→Food, Uber→Transport, Amazon→Shopping)
- Filters valid amounts (expense: 10-10M, income: min 100)

### LanguageManager
- 14 supported languages: English, Bengali, Hindi, Arabic, Spanish, French, German, Japanese, Korean, Chinese, Portuguese, Russian, Turkish, Urdu
- `AppStrings` object with key-value translations (EN + BN implemented)
- `StringResources` with constants for common keys

### PrivacyHelper
- Toggle privacy mode (hide amounts as "••••••")
- Haptic feedback utilities (light, medium, heavy)

---

## 14. Background Workers (WorkManager)

| Worker | Schedule | Purpose |
|---|---|---|
| **BillNotificationWorker** | Daily periodic | Checks bill due dates, sends notifications at 3/2/1/0 days before due |
| **SubscriptionNotificationWorker** | Daily periodic | Checks subscription renewal dates, sends reminders |
| **NotificationWorker** | Daily at configurable time | Daily expense logging reminder |
| **WeeklyDigestWorker** | Weekly | Weekly recurring transaction summary |
| **BackupWorker** | Daily/Weekly/Monthly | Periodic auto-backup to Downloads |

### Notification Channels
| Channel ID | Name | Importance |
|---|---|---|
| `bill_reminders` | Bill Reminders | HIGH |
| `subscription_reminders` | Subscription Reminders | HIGH |
| `daily_reminders` | Daily Reminders | DEFAULT |
| `weekly_digest` | Weekly Digest | DEFAULT |
| `emergency_alerts` | Emergency Alerts | HIGH |

### NotificationHelper
- Schedules/cancels daily reminders at configurable time
- Schedules/cancels weekly digest
- Emergency pause notification (pause all recurring expenses for 30 days)

---

## 15. Widget

### `ExpenseWidget.kt`
- Home screen app widget
- Shows total expenses for current month
- "+ Add" button to quickly add expense
- Updates every 24 hours
- Minimal layout defined in `expense_widget.xml`

---

## 16. Data Flow Example

### Creating an Expense

1. **User** taps FAB → "Add expense" on Dashboard
2. **Navigation** → `DashboardScreen` → `navController.navigate("add_expense")`
3. **Screen** → `AddExpenseScreen` composable rendered
4. **User** fills amount, selects category, account, date, notes
5. **Save** → `ExpenseViewModel.saveExpense(...)`
6. **Repository** → `ExpenseRepositoryImpl.insertExpense(expense)`
7. **DAO** → `ExpenseDao.insertExpense(entity)`
8. **Room** → SQLite INSERT
9. **Navigation** → `navController.popBackStack()` returns to Dashboard
10. **Dashboard** → `DashboardViewModel` `init` block re-collects flows → UI updates reactively

### Data Flow: Dashboard Loading

```
DashboardViewModel.init()
  ├─ loadAccounts() → accountRepository.getAllAccounts()
  │                   → AccountDao.getAllAccounts() → Flow<List<Account>>
  │
  ├─ loadDashboardData()
  │   ├─ combine(incomeToday, expenseToday, incomeMonth, expenseMonth, categories, budget)
  │   │   → updates uiState with today/month stats, categories, insights
  │   ├─ loadRecentTransactions(startMonth, endMonth)
  │   │   → combine(incomeRepo, expenseRepo) → sorted merged list
  │   ├─ loadGoalsAndBills()
  │   │   → goalRepo.getActiveGoals() + billRepo.getActiveBillReminders()
  │   └─ loadMonthlyTrend()
  │       → loop 7 months: income - expense per month
  │
  ├─ loadAccountHealth()
      → fusionRepository.getNetWorthSummary() + getAccountHealthList()
```

---

## 17. Backup & Restore System

### Export Flow
1. `BackupManager.exportAllData(location)`
2. Reads ALL data from all DAOs (income, expense, accounts, goals, budgets, bills, transfers, subscriptions, investments)
3. Converts entities to serializable backup DTOs
4. Serializes to JSON using `kotlinx.serialization`
5. Writes to file (`savings_backup_yyyy-MM-dd_HH-mm-ss.json`)
6. Saves timestamp to SharedPreferences

### Import Flow
1. `BackupManager.importAllData(backupData, replaceExisting)`
2. If `replaceExisting` → deletes all existing data from all tables
3. Iterates through backup data → inserts into respective DAOs
4. Restores settings to SharedPreferences

### Auto-Backup
- `BackupWorker` scheduled via WorkManager
- Configurable frequency: DAILY, WEEKLY, MONTHLY
- Configured in BackupScreen

---

## 18. Gamification System

### Achievements (7 total)
| ID | Title | Requirement |
|---|---|---|
| `first_save` | First Saver | Make first savings |
| `budget_master` | Budget Master | Stay under budget for 7 days |
| `goal_getter` | Goal Getter | Complete first goal |
| `consistent` | Consistent | Log expenses for 30 consecutive days |
| `saver_extreme` | Saver Extreme | Save 50% of income |
| `early_bird` | Early Bird | Log expenses before noon |
| `variety` | Variety | Track 5+ categories |

### Savings Score Calculation
```
Total Score (100 max) = Savings Rate (40) + Budget Adherence (30) + Goal Progress (20) + Streak Bonus (10)

Grade:
  90+ → A+  |  80+ → A  |  70+ → B+  |  60+ → B  |  50+ → C  |  30+ → D  |  <30 → F
```

### Savings Streak
- Tracks consecutive days under budget
- Stores: longest streak, total days, last updated

---

## 19. Key Features Matrix

| Feature | Status | Location |
|---|---|---|
| Income Tracking | ✅ Complete | `IncomeEntity`, `IncomeScreen`, `AddIncomeScreen` |
| Expense Tracking | ✅ Complete | `ExpenseEntity`, `ExpenseScreen`, `AddExpenseScreen` |
| Budget Management | ✅ Complete | `BudgetEntity`, `BudgetScreen`, `SettingsScreen` |
| Savings Goals | ✅ Complete | `GoalEntity`, `GoalsScreen` |
| Bill Reminders | ✅ Complete | `BillReminderEntity`, `BillRemindersScreen` |
| Multi-Account | ✅ Complete | `AccountEntity`, `AccountsScreen` |
| Account Transfers | ✅ Complete | `TransferEntity`, `TransferScreen` |
| Subscriptions | ✅ Complete | `SubscriptionEntity`, `SubscriptionManagerScreen` |
| Investments | ✅ Complete | `InvestmentEntity`, `InvestmentScreen` |
| Currency Converter | ✅ Complete | `ExchangeManager`, `CurrencyConverterScreen` |
| Receipt Scanner | ✅ Complete | ML Kit, `ReceiptScannerScreen` |
| Voice Input | ✅ Complete | `VoiceInputHelper` |
| SMS Parsing | ✅ Complete | `SmsTransactionParser` |
| PDF Reports | ✅ Complete | `ReportGenerator` |
| CSV/TEXT/JSON Export | ✅ Complete | `ExportManager` |
| Full Backup/Restore | ✅ Complete | `BackupManager`, `BackupWorker` |
| Biometric Lock | ✅ Complete | `BiometricHelper` |
| Privacy Mode | ✅ Complete | `PrivacyHelper` |
| Dark/AMOLED Theme | ✅ Complete | `Theme.kt`, `AmoledTheme.kt` |
| Scheduled Dark Mode | ✅ Complete | `MainActivity.kt` |
| Multi-Language | ✅ Partial (EN/BN) | `LanguageManager`, `LanguageScreen` |
| Dashboard | ✅ Complete | `DashboardScreen` (1037 lines) |
| Calendar View | ✅ Complete | `CalendarScreen` |
| Analytics | ✅ Complete | `AnalyticsScreen` |
| Fusion Insights | ✅ Complete | `FusionRepositoryImpl` (623 lines) |
| Gamification | ✅ Complete | `Gamification.kt`, `GamificationScreen` |
| Onboarding | ✅ Complete | `OnboardingScreen` |
| Notification Workers | ✅ Complete | 3 workers + helper |
| Home Widget | ✅ Complete | `ExpenseWidget` |
| Calculator | ✅ Complete | `CalculatorScreen` |
| Transaction History | ✅ Complete | `TransactionHistoryScreen` |
| Changelog | ✅ Complete | `ChangelogScreen` |
| Bank-Specific Limits | ✅ Complete | bKash/Nagad/Rocket daily limits |
| Fee Calculation | ✅ Complete | bKash→Bank: 10 BDT, Nagad→Bank: 5 BDT |

---

> **Note:** This document should be updated whenever significant architectural changes are made to the project. For any new developer joining, start by reading this file, then explore the specific screens/modules relevant to your task.
