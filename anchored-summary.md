# Anchored Summary: Transaction Fees + Account-Based Recurring Transactions

## Goal
- Add transaction fee support to transfers: user inputs fee, deducted from source account alongside amount; destination receives amount only
- Make all recurring transactions account-linked: every recurring rule must have an account; recurring expenses fail gracefully if linked account has insufficient balance

## Constraints & Preferences
- Transfer fee is user-input, deducted from source; destination receives only the amount
- `fromAccount.balance >= amount + fee` validated before transfer; detailed shortfall shown in error
- Fee field is optional (default 0.0); empty or "0" treated as zero
- All recurring rules must link to an account (account picker in AddRecurringDialog)
- If linked account lacks sufficient balance for recurring expense: record occurrence with `[FAILED]` prefix in notes, do not deduct, send Android notification
- Recurring income always gets an account (auto-assigned wallet fallback during creation)
- New occurrences created by Worker have `isRecurring = false` to avoid infinite loops
- Duplicate prevention: skip if latest occurrence date >= calculated next due date
- Worker retries up to 3 times on failure

## Progress
### Done
- **Transfer Fee** (`TransferScreen.kt`, `TransferViewModel.kt`, `AccountRepositoryImpl.kt`):
  - Fee input card below amount with `Receipt` icon and red accent
  - `feeText` in `TransferUiState`, parsed as `fee: Double` (default 0.0)
  - Validation: `updateFee()` only accepts digits and decimal via regex `^\d*\.?\d*$`
  - `totalAmount = amount + fee` shown in hero card, summary card, confirmation dialog
  - `canTransfer` checks `fee >= 0`
  - `AccountRepositoryImpl.transferMoney()` accepts `fee` param, validates `amount + fee`, deducts total from source, credits amount to destination
  - Success dialog shows fee breakdown and new balances for both accounts
  - Daily transfer limit check uses `totalDeduction` (amount + fee)

- **Account-Based Recurring** (`RecurringViewModel.kt`, `RecurringScreen.kt`):
  - `saveRecurringIncome()` / `saveRecurringExpense()` accept `accountId: Long?`
  - `AddRecurringDialog` shows account picker dropdown (`ExposedDropdownMenuBox`) with name + balance
  - Confirm button disabled until account selected (`selectedAccountId != null`)
  - `RecurringUiState.availableAccounts` already loaded via `combine` flow with `accountRepository.getAllAccounts()`

- **RecurringTransactionWorker Failure Handling** (`RecurringTransactionWorker.kt`):
  - `processRecurringExpenses()` checks balance BEFORE inserting occurrence
  - If sufficient: insert expense + deduct from account + record balance history (`RECURRING_EXPENSE`)
  - If insufficient: insert expense with `[FAILED]` notes prefix, no deduction, send notification
  - `sendFailureNotification()` creates notification channel (`recurring_expense_failure`) on API 26+, sends high-priority notification via `NotificationCompat`
  - Permission check for `POST_NOTIFICATIONS` on API 33+ (TIRAMISU)
  - `processRecurringIncomes()` adds `template.accountId` to new income and updates account balance (`RECURRING_INCOME`)
  - Edge case: deleted account → treated as insufficient balance → `[FAILED]` recorded + notification

### Blocked
- (none)

## Key Decisions
- Fee stored as `feeText: String` in `TransferUiState` (consistent with `amount` pattern), parsed via computed `fee` property `get() = feeText.toDoubleOrNull() ?: 0.0`
- Worker uses DAOs directly (like `BackupWorker` pattern) rather than Repository layer, avoiding additional interface/impl changes
- Occurrences created with `isRecurring = false` so they aren't picked up as templates by future Worker runs
- Failure marked via `[FAILED]` notes prefix rather than new entity field to avoid database migration
- Notification channel created on every notification send (safe; `createNotificationChannel` is idempotent)
- Worker scheduled with `ExistingPeriodicWorkPolicy.UPDATE` — first run ~15 min after scheduling, then every 24h

### Widget System Upgrade
- **BalanceWidgetProvider**: Shows total balance across all accounts + today's income/expense in a premium card layout with green/red/blue theme colors. Tap to open app. Real data via Room database.
- **ExpenseWidget** (upgraded): Now shows real today's expense total fetched from database. "Add Expense" button navigates to add_expense screen. Premium card layout.
- **QuickActionWidgetProvider**: Two-button layout for "Add Income" (green) and "Add Expense" (red). Each button navigates directly to the respective screen.
- **Database helper**: `SavingBuddyDatabase.getInstance(context)` added for widget/worker access without Hilt injection.
- **Widget colors**: `widget_income_green`, `widget_expense_red`, `widget_savings_blue`, `widget_card_bg`, `widget_text_primary/secondary` defined in `colors.xml`.
- **Navigation from widgets**: `MainActivity` reads `navigate_to` intent extra; `MainNavigation()` composable accepts `navigateTo` param and navigates via `LaunchedEffect`.
- **Update period**: All widgets update every 30 minutes (1800000ms).

## Relevant Files
- `ui/screens/accounts/TransferScreen.kt` — fee input card, summary/confirmation/success dialogs with fee
- `ui/screens/accounts/TransferViewModel.kt` — `TransferUiState.feeText`, `updateFee()`, `totalAmount`, `executeTransfer()` fee validation
- `data/repository/AccountRepositoryImpl.kt` — `transferMoney(fromId, toId, amount, fee, note)` with fee deduction
- `domain/repository/AccountRepository.kt` — interface with `fee` param

- `ui/screens/recurring/RecurringViewModel.kt` — `saveRecurringIncome/Expense` with `accountId`, accounts loaded via `combine`
- `ui/screens/recurring/RecurringScreen.kt` — `AddRecurringDialog` with account picker dropdown
- `data/RecurringTransactionWorker.kt` — balance check before expense, `[FAILED]` marker, notification
- `data/local/dao/IncomeDao.kt` — `getRecurringIncomes()`, `getLatestOccurrenceDate()`, `countOccurrencesOnDay()`
- `data/local/dao/ExpenseDao.kt` — `getRecurringExpenses()`, `getLatestOccurrenceDate()`, `countOccurrencesOnDay()`
- `data/local/dao/AccountDao.kt` — `getAccountById()`, `updateBalance()` suspend functions
- `data/local/entity/IncomeEntity.kt` — `IncomeEntity.accountId`, `ExpenseEntity.accountId`
- `domain/model/Income.kt` — `accountId: Long? = null`
- `domain/model/Expense.kt` — `accountId: Long? = null`
- `data/local/converter/IncomeMapper.kt` — maps `accountId` both directions
- `data/local/converter/ExpenseMapper.kt` — maps `accountId` both directions
- `SavingBuddyApp.kt` — schedules `RecurringTransactionWorker` on startup

- **New (Widget System):**
  - `widget/BalanceWidgetProvider.kt` — balance + today's income/expense widget
  - `widget/ExpenseWidget.kt` — upgraded with real today's total
  - `widget/QuickActionWidgetProvider.kt` — add income/expense quick buttons
  - `res/layout/balance_widget.xml` — premium card layout with divider
  - `res/layout/expense_widget.xml` — premium card layout with today's total
  - `res/layout/quick_action_widget.xml` — two-button action layout
  - `res/drawable/widget_card_bg.xml` — rounded card background drawable
  - `res/xml/balance_widget_info.xml` — provider config
  - `res/xml/quick_action_widget_info.xml` — provider config
  - `res/values/colors.xml` — 9 widget theme colors added
  - `res/values/strings.xml` — 10 new widget string resources
  - `data/local/SavingBuddyDatabase.kt` — `getInstance(context)` companion method added
  - `MainActivity.kt` — `navigate_to` intent handling
  - `ui/navigation/MainNavigation.kt` — `navigateTo` param + `LaunchedEffect` navigation
  - `AndroidManifest.xml` — 3 widget receivers registered
