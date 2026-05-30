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
- **CalendarScreen.kt**: Top bar with subtitle, premium MonthlySummaryCard with gradient accent + border + icon header
- **TransactionHistoryScreen.kt**: Subtle borders on TransactionCard, refined header
- **SettingsScreen.kt**: Borders added to SettingsSectionCard, stats card, and footer card
- **FusionScreen.kt**: Top bar subtitle, pill-style tab row replacing TabRow, borders on SimpleAssetItem/SimpleEmptyCard/SimpleEmptyInsights
- **BackupScreen.kt & ExportScreen.kt**: Top bar subtitles, premium card shapes/borders, theme color replacements
- **BillRemindersScreen.kt, CalculatorScreen.kt, CurrencyConverterScreen.kt, GamificationScreen.kt**: Top bar subtitles, premium card styling, section header icons, theme color replacements
- **InvestmentScreen.kt, LanguageScreen.kt, MusicScreen.kt, NotificationsScreen.kt, ReceiptScannerScreen.kt, SubscriptionManagerScreen.kt**: Top bar subtitles, premium card borders/shapes, theme color replacements
- **AccountDetailScreen.kt, ExpenseDetailScreen.kt, ChangelogScreen.kt, AdvancedFeaturesScreen.kt**: Top bar subtitles, border/shape/icon upgrades, theme color replacements

### Blocked
- Gradle build cannot be verified due to Java 25 ↔ Maven Central TLS handshake error (`Tag mismatch` on `symbol-processing-aa-embeddable-2.3.6.jar`)

## Key Decisions
- Kept `DashboardComponents.kt` (newer design) and deleted `DashboardCards.kt` (older duplicate)
- All upgraded screens use: RoundedCornerShape(20-24dp) for cards, 0.5dp border with outlineVariant(0.4f), green-tinged gradient backgrounds, 14-16dp card padding, section headers with icon + titleSmall/SemiBold
- Category chips use emoji + displayName with selected state (colored background at 0.15f alpha, border 1.5dp colored)
- Quick amount chips used in both AddExpense (preset amounts) and Transfer (100/500/1000/5000)
- Subscription/Recurring items use accent color based on status (Gray=paused, ExpenseRed=overdue, WarningOrange=near due, IncomeGreen/ExpenseRed=type)

## Relevant Files
- `app/src/main/java/com/rudra/savingbuddy/ui/theme/Color.kt` — all theme colors
- `app/src/main/java/com/rudra/savingbuddy/ui/theme/PremiumComponents.kt` — shared GlassCard, PremiumTextField, PremiumButton
- `app/src/main/java/com/rudra/savingbuddy/util/CurrencyFormatter.kt` — formatBDT, formatCompact, format
- `app/src/main/java/com/rudra/savingbuddy/ui/navigation/MainNavigation.kt` — routes cleaned up
- All 35+ screen files under `ui/screens/` — upgraded
