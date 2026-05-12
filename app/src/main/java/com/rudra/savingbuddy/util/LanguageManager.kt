package com.rudra.savingbuddy.util

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    data class Language(
        val code: String,
        val displayName: String,
        val nativeName: String,
        val flag: String
    )

    val supportedLanguages = listOf(
        Language("en", "English", "English", "🇺🇸"),
        Language("bn", "Bengali", "বাংলা", "🇧🇩"),
        Language("hi", "Hindi", "हिन्दी", "🇮🇳"),
        Language("ar", "Arabic", "العربية", "🇸🇦"),
        Language("es", "Spanish", "Español", "🇪🇸"),
        Language("fr", "French", "Français", "🇫🇷"),
        Language("de", "German", "Deutsch", "🇩🇪"),
        Language("ja", "Japanese", "日本語", "🇯🇵"),
        Language("ko", "Korean", "한국어", "🇰🇷"),
        Language("zh", "Chinese", "中文", "🇨🇳"),
        Language("pt", "Portuguese", "Português", "🇵🇹"),
        Language("ru", "Russian", "Русский", "🇷🇺"),
        Language("tr", "Turkish", "Türkçe", "🇹🇷"),
        Language("ur", "Urdu", "اردو", "🇵🇰")
    )

    fun getSelectedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
        setLocale(context, languageCode)
    }

    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getLanguageName(code: String): String {
        return supportedLanguages.find { it.code == code }?.displayName ?: "English"
    }

    fun getLanguageFlag(code: String): String {
        return supportedLanguages.find { it.code == code }?.flag ?: "🇺🇸"
    }
}

object AppStrings {
    val strings: Map<String, Map<String, String>> = mapOf(
        "en" to mapOf(
            "app_name" to "Saving Buddy",
            "dashboard" to "Dashboard",
            "reports" to "Reports",
            "settings" to "Settings",
            "add_income" to "Add Income",
            "add_expense" to "Add Expense",
            "total_savings" to "Total Savings",
            "total_income" to "Total Income",
            "total_expenses" to "Total Expenses",
            "budget" to "Budget",
            "goals" to "Goals",
            "calendar" to "Calendar",
            "accounts" to "Accounts",
            "music" to "Music Player",
            "notifications" to "Notifications",
            "language" to "Language",
            "theme" to "Theme",
            "dark_mode" to "Dark Mode",
            "light_mode" to "Light Mode",
            "export" to "Export",
            "import" to "Import",
            "backup" to "Backup",
            "about" to "About",
            "help" to "Help",
            "search" to "Search",
            "filter" to "Filter",
            "cancel" to "Cancel",
            "save" to "Save",
            "delete" to "Delete",
            "edit" to "Edit",
            "share" to "Share",
            "rate" to "Rate App",
            "version" to "Version",
            "privacy" to "Privacy Policy",
            "terms" to "Terms of Service",
            "contact" to "Contact Support",
            "no_data" to "No data available",
            "loading" to "Loading...",
            "error" to "An error occurred",
            "retry" to "Retry",
            "success" to "Success",
            "welcome" to "Welcome to Saving Buddy"
        ),
        "bn" to mapOf(
            "app_name" to "সেভিং বাডি",
            "dashboard" to "ড্যাশবোর্ড",
            "reports" to "রিপোর্ট",
            "settings" to "সেটিংস",
            "add_income" to "আয় যোগ করুন",
            "add_expense" to "ব্যয় যোগ করুন",
            "total_savings" to "মোট সঞ্চয়",
            "total_income" to "মোট আয়",
            "total_expenses" to "মোট ব্যয়",
            "budget" to "বাজেট",
            "goals" to "লক্ষ্য",
            "calendar" to "ক্যালেন্ডার",
            "accounts" to "অ্যাকাউন্ট",
            "music" to "মিউজিক প্লেয়ার",
            "notifications" to "বিজ্ঞপ্তি",
            "language" to "ভাষা",
            "theme" to "থিম",
            "dark_mode" to "ডার্ক মোড",
            "light_mode" to "লাইট মোড",
            "export" to "এক্সপোর্ট",
            "backup" to "ব্যাকআপ",
            "about" to "সম্পর্কে",
            "search" to "অনুসন্ধান",
            "filter" to "ফিল্টার",
            "cancel" to "বাতিল",
            "save" to "সংরক্ষণ",
            "delete" to "মুছুন",
            "edit" to "সম্পাদনা",
            "share" to "শেয়ার",
            "rate" to "অ্যাপ রেট দিন",
            "version" to "ভার্সন",
            "privacy" to "গোপনীয়তা নীতি",
            "terms" to "সেবার শর্তাবলী",
            "contact" to "যোগাযোগ",
            "no_data" to "কোনো তথ্য নেই",
            "loading" to "লোড হচ্ছে...",
            "error" to "একটি ত্রুটি ঘটেছে",
            "retry" to "পুনরায় চেষ্টা করুন",
            "success" to "সফল",
            "welcome" to "সেভিং বাডিতে স্বাগতম"
        )
    )

    fun getString(key: String, languageCode: String = "en"): String {
        return strings[languageCode]?.get(key) ?: strings["en"]?.get(key) ?: key
    }
}

object StringResources {
    const val DASHBOARD = "dashboard"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
    const val ADD_INCOME = "add_income"
    const val ADD_EXPENSE = "add_expense"
    const val TOTAL_SAVINGS = "total_savings"
}
