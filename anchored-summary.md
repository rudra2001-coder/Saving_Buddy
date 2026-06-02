# Anchored Summary: Premium UI Upgrade

## Goal
- Upgrade all screens to match the premium dashboard UI design language.

## Constraints & Preferences
- Keep all existing functionality and logic intact
- Use rounded cards (RoundedCornerShape 16-24dp), subtle borders (0.5dp outlineVariant), gradient background accents, section headers with icons, colored pills/chips, emoji icons, consistent spacing (14-16dp), and theme colors (IncomeGreen, ExpenseRed, SavingsBlue, etc.)
- All theme colors already defined in Color.kt (PrimaryGreen, ExpenseRed, TextPrimary, BackgroundCard, etc.)

## Progress
### Done — All Screens Upgraded
- **Dashboard**: Conflict resolved (`DashboardCards.kt` deleted, kept `DashboardComponents.kt`)
- **DashboardViewModel**: Inefficient `combine` loop replaced with direct `.first()` calls
- **MainNavigation.kt**: Removed 4 duplicate/wrong routes
- **FeaturesScreen.kt**: Removed `add_goal` duplicate, fixed `advanced` route, replaced 22 hardcoded colors with theme colors
- **AddIncomeScreen.kt**: Hero amount card, emoji category chips, account selector, recurring toggle
- **AddExpenseScreen.kt**: Matching AddIncome pattern with expense-red scheme, recurring interval row
- **IncomeScreen.kt**: Header card with gradient, search bar, horizontal category chips, styled list cards with dropdown
- **ExpenseScreen.kt**: Header card with gradient, search bar, 8 category chips, styled list with delete button, quick-add dialog
- **AccountsScreen.kt**: Net worth hero card, QuickAction cards, sectioned by type, empty state
- **AddAccountScreen.kt**: Account type selector cards, provider chooser with emoji chips, custom provider field
- **TransferScreen.kt**: Hero amount card, source/destination cards with swap button, quick amount chips (100/500/1000/5000), summary card, styled dialogs
- **GoalsScreen.kt**: Stats card, filter tabs (Active/Completed), premium GoalCard with animated progress, contribution dialog
- **BudgetScreen.kt**: Tabbed layout (Budget/Subscriptions/Reminders), premium budget card with gradient progress, subscription/reminder cards, premium dialogs
- **RecurringScreen.kt**: Insight cards, net monthly card, smart suggestions, premium item cards with bulk mode, details bottom sheet
- **ReportsScreen.kt**: Top bar with subtitle, premium pill-style tab row (Overview/Logs)
- **AnalyticsScreen.kt**: Top bar with subtitle, premium period selector, card borders/icons on all 7 card composables
- **CalendarScreen.kt**: Top bar with subtitle, premium MonthlySummaryCard with gradient accent + border + icon header; made fully scrollable via single LazyColumn (nested scroll fixed)
- **TransactionHistoryScreen.kt**: Subtle borders on TransactionCard, refined header
- **SettingsScreen.kt**: Borders added to SettingsSectionCard, stats card, and footer card
- **FusionScreen.kt**: Top bar subtitle, pill-style tab row replacing TabRow, borders on SimpleAssetItem/SimpleEmptyCard/SimpleEmptyInsights
- **BackupScreen.kt & ExportScreen.kt**: Top bar subtitles, premium card shapes/borders, theme color replacements
- **BillRemindersScreen.kt, CalculatorScreen.kt, CurrencyConverterScreen.kt, GamificationScreen.kt**: Top bar subtitles, premium card styling, section header icons, theme color replacements
- **InvestmentScreen.kt, LanguageScreen.kt, MusicScreen.kt, NotificationsScreen.kt, ReceiptScannerScreen.kt, SubscriptionManagerScreen.kt**: Top bar subtitles, premium card borders/shapes, theme color replacements
- **AccountDetailScreen.kt, ExpenseDetailScreen.kt, ChangelogScreen.kt, AdvancedFeaturesScreen.kt**: Top bar subtitles, border/shape/icon upgrades, theme color replacements

### Account Daily Limit Upgrade
- **EditAccountScreen**: Added inline toggle (Switch) to enable/disable daily limit + numeric text field for custom limit amount
- **AccountDetailScreen**: Limit card now always visible (shows "No limit set — tap to add one" when disabled); added edit icon button inline; added limit edit dialog with toggle + amount + save; shows red "Limit reached" warning banner when usage hits 100%
- **AddAccountViewModel**: Added `dailyLimit`, `limitEnabled` to UI state; `updateDailyLimit()`, `toggleLimit()` functions; save logic uses user-set limit or null
- **AccountDetailViewModel**: Added `showLimitDialog`, `limitEditAmount`, `limitEditEnabled` state; `showLimitDialog()`, `toggleLimitEdit()`, `saveLimit()` functions

### Bill Reminder Payment System Upgrade
- **Pay from Account**: Each bill card has a payments button; user selects an account and amount is deducted from that account's balance
- **Multi-Month Payment**: User can choose 1-12 months to pay at once via +/- controls in the Pay dialog
- **Paid/Unpaid Month Tracking**: Bills track paid months (yyyy-MM) in `paidMonths` column; UI shows "Paid" badge and paid month history
- **Auto-Silence**: Paid bills skip notifications for that month; unpaid bills keep notifying every cycle
- **Account Deduction**: `BillReminderRepositoryImpl.payBill()` validates balance and calls `AccountDao.updateBalance()`
- **Database**: New `MIGRATION_7_8` adds `payFromAccountId`, `paidMonths`, `lastProcessedMonth` to `bill_reminders`
- **Migration**: Room database updated from v7 → v8

### Calendar Screen Fix
- **Before**: Root `Column` was non-scrollable with nested `LazyColumn` for transactions (would throw)
- **After**: Single `LazyColumn` wrapping all sections; transactions rendered via inline `items()` instead of nested lazy list

### Blocked
- Gradle build cannot be verified due to Java 25 ↔ Maven Central TLS handshake error (`Tag mismatch` on `symbol-processing-aa-embeddable-2.3.6.jar`)

## Key Decisions
- Kept `DashboardComponents.kt` (newer design) and deleted `DashboardCards.kt` (older duplicate)
- All upgraded screens use: RoundedCornerShape(20-24dp) for cards, 0.5dp border with outlineVariant(0.4f), green-tinged gradient backgrounds, 14-16dp card padding, section headers with icon + titleSmall/SemiBold
- Category chips use emoji + displayName with selected state (colored background at 0.15f alpha, border 1.5dp colored)
- Quick amount chips used in both AddExpense (preset amounts) and Transfer (100/500/1000/5000)
- Subscription/Recurring items use accent color based on status (Gray=paused, ExpenseRed=overdue, WarningOrange=near due, IncomeGreen/ExpenseRed=type)
- Paid months stored as comma-separated yyyy-MM strings in SQLite for simple querying
- Pay dialog uses inline +/- month count selector instead of date picker for simplicity

## Relevant Files
- `app/src/main/java/com/rudra/savingbuddy/ui/theme/Color.kt` — all theme colors
- `app/src/main/java/com/rudra/savingbuddy/ui/theme/PremiumComponents.kt` — shared GlassCard, PremiumTextField, PremiumButton
- `app/src/main/java/com/rudra/savingbuddy/util/CurrencyFormatter.kt` — formatBDT, formatCompact, format
- `app/src/main/java/com/rudra/savingbuddy/ui/navigation/MainNavigation.kt` — routes cleaned up
- All 35+ screen files under `ui/screens/` — upgraded
- **New/Modified (Bill Payment Upgrade):**
  - `domain/model/BillReminder.kt` — added `payFromAccountId`, `paidMonths`, `lastProcessedMonth`
  - `data/local/entity/IncomeEntity.kt` — `BillReminderEntity` 3 new columns
  - `data/local/converter/BillReminderMapper.kt` — maps new fields
  - `data/local/dao/BillReminderDao.kt` — `updatePaidMonths`, `updatePayFromAccount` queries
  - `domain/repository/BillReminderRepository.kt` — `payBill()`, `updatePaidMonths()` methods
  - `data/repository/BillReminderRepositoryImpl.kt` — `payBill()` deducts from account via `AccountDao`
  - `di/RepositoryModule.kt` — injects `AccountDao` into `BillReminderRepositoryImpl`
  - `ui/screens/bills/BillRemindersViewModel.kt` — pay dialog state, `confirmPayBill()`, account loading
  - `ui/screens/bills/BillRemindersScreen.kt` — `PayBillDialog`, paid badges, month count selector
  - `util/BillNotificationWorker.kt` — skips paid months
  - `data/local/SavingBuddyDatabase.kt` — `MIGRATION_7_8`
  - `data/models/BackupModels.kt` + `BackupManager.kt` — new fields in backup/restore
- **Modified (Calendar Fix):**
  - `ui/screens/calendar/CalendarScreen.kt` — replaced fixed Column + nested LazyColumn with single scrollable LazyColumn
