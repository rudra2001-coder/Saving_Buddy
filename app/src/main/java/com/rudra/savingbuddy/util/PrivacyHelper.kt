package com.rudra.savingbuddy.util

import android.view.View
import android.view.HapticFeedbackConstants

object PrivacyHelper {
    fun togglePrivacyMode(
        view: View,
        isPrivacyMode: Boolean,
        onPrivacyEnabled: () -> Unit,
        onPrivacyDisabled: () -> Unit
    ) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        if (isPrivacyMode) {
            onPrivacyEnabled()
        } else {
            onPrivacyDisabled()
        }
    }

    fun hideAmount(amount: Double): String {
        return "••••••"
    }

    fun hideAmountsInString(text: String, pattern: Regex): String {
        return pattern.replace(text) { "••••" }
    }

    fun formatWithPrivacy(amount: Double, privacyMode: Boolean): String {
        return if (privacyMode) {
            hideAmount(amount)
        } else {
            CurrencyFormatter.format(amount)
        }
    }

    fun vibrate(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun vibrateError(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }
}

object HapticFeedback {
    fun light(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun medium(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun heavy(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}