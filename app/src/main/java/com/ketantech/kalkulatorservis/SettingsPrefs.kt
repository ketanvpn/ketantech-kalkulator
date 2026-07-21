package com.ketantech.kalkulatorservis

import android.content.Context
import android.content.SharedPreferences

/**
 * Penyimpanan pengaturan via SharedPreferences (PRD 5.3 & 9.1).
 * Semua nilai punya default sesuai PRD dan fallback aman jika data corrupt.
 */
class SettingsPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var riskMarginPercent: Int
        get() = prefs.getInt(KEY_RISK_MARGIN, DEFAULT_RISK_MARGIN)
        set(value) = prefs.edit().putInt(KEY_RISK_MARGIN, value.coerceIn(0, 100)).apply()

    var operationalCost: Long
        get() = prefs.getLong(KEY_OPERATIONAL, DEFAULT_OPERATIONAL)
        set(value) = prefs.edit().putLong(KEY_OPERATIONAL, value.coerceAtLeast(0)).apply()

    var level1Fee: Long
        get() = prefs.getLong(KEY_LEVEL_1, DEFAULT_LEVEL_1)
        set(value) = prefs.edit().putLong(KEY_LEVEL_1, value.coerceAtLeast(0)).apply()

    var level2Fee: Long
        get() = prefs.getLong(KEY_LEVEL_2, DEFAULT_LEVEL_2)
        set(value) = prefs.edit().putLong(KEY_LEVEL_2, value.coerceAtLeast(0)).apply()

    var level3Fee: Long
        get() = prefs.getLong(KEY_LEVEL_3, DEFAULT_LEVEL_3)
        set(value) = prefs.edit().putLong(KEY_LEVEL_3, value.coerceAtLeast(0)).apply()

    var warrantyDaysL1: Int
        get() = prefs.getInt(KEY_WARRANTY_L1, DEFAULT_WARRANTY_L1)
        set(value) = prefs.edit().putInt(KEY_WARRANTY_L1, value.coerceIn(0, 365)).apply()

    var warrantyDaysL2: Int
        get() = prefs.getInt(KEY_WARRANTY_L2, DEFAULT_WARRANTY_L2)
        set(value) = prefs.edit().putInt(KEY_WARRANTY_L2, value.coerceIn(0, 365)).apply()

    var warrantyDaysL3: Int
        get() = prefs.getInt(KEY_WARRANTY_L3, DEFAULT_WARRANTY_L3)
        set(value) = prefs.edit().putInt(KEY_WARRANTY_L3, value.coerceIn(0, 365)).apply()

    /** Kembalikan semua pengaturan ke nilai default (PRD 5.3). */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val FILE_NAME = "ketantech_settings"

        private const val KEY_RISK_MARGIN = "riskMarginPercent"
        private const val KEY_OPERATIONAL = "operationalCost"
        private const val KEY_LEVEL_1 = "level1Fee"
        private const val KEY_LEVEL_2 = "level2Fee"
        private const val KEY_LEVEL_3 = "level3Fee"
        private const val KEY_WARRANTY_L1 = "warrantyDaysL1"
        private const val KEY_WARRANTY_L2 = "warrantyDaysL2"
        private const val KEY_WARRANTY_L3 = "warrantyDaysL3"

        // Default sesuai PRD 5.3
        const val DEFAULT_RISK_MARGIN = 10
        const val DEFAULT_OPERATIONAL = 20_000L
        const val DEFAULT_LEVEL_1 = 75_000L
        const val DEFAULT_LEVEL_2 = 150_000L
        const val DEFAULT_LEVEL_3 = 300_000L
        const val DEFAULT_WARRANTY_L1 = 7
        const val DEFAULT_WARRANTY_L2 = 30
        const val DEFAULT_WARRANTY_L3 = 14
    }
}
