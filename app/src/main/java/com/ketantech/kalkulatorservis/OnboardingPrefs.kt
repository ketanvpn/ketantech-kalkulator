package com.ketantech.kalkulatorservis

import android.content.Context

/**
 * Penyimpanan status onboarding — hanya tampil sekali saat pertama install.
 */
class OnboardingPrefs(context: Context) {

    private val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var hasSeenOnboarding: Boolean
        get() = prefs.getBoolean(KEY_SEEN, false)
        set(value) = prefs.edit().putBoolean(KEY_SEEN, value).apply()

    companion object {
        private const val FILE_NAME = "ketantech_onboarding"
        private const val KEY_SEEN = "has_seen_onboarding"
    }
}
