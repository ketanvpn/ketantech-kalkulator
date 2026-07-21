package com.ketantech.kalkulatorservis

/**
 * Level jasa servis sesuai PRD 5.1.
 */
enum class ServiceLevel(val tier: Int) {
    LEVEL_1(1), // Ringan / Software
    LEVEL_2(2), // Menengah / Hardware Eksterior
    LEVEL_3(3)  // Berat / Hardware Mesin
}

/**
 * Nilai hasil perhitungan (PRD 7.1).
 */
data class CalculationResult(
    val sparepartCost: Long,
    val riskFund: Long,
    val operationalCost: Long,
    val serviceFee: Long,
    val total: Long,
    val showSparepartRows: Boolean
)

/**
 * Kalkulator harga servis — murni logika bisnis, mudah di-unit-test.
 *
 * Formula (PRD 7.1):
 *   Dana_Risiko = Modal_Sparepart × (Risk_Margin% / 100)
 *   Total       = Modal_Sparepart + Dana_Risiko + Biaya_Operasional + Biaya_Jasa
 */
object PriceCalculator {

    fun calculate(
        sparepartCost: Long,
        riskMarginPercent: Int,
        operationalCost: Long,
        serviceFee: Long
    ): CalculationResult {
        val safeSparepart = sparepartCost.coerceAtLeast(0)
        val riskFund = safeSparepart * riskMarginPercent.coerceIn(0, 100) / 100

        return CalculationResult(
            sparepartCost = safeSparepart,
            riskFund = riskFund,
            operationalCost = operationalCost.coerceAtLeast(0),
            serviceFee = serviceFee.coerceAtLeast(0),
            total = safeSparepart + riskFund + operationalCost.coerceAtLeast(0) + serviceFee.coerceAtLeast(0),
            // PRD 7.2: baris sparepart & garansi disembunyikan jika modal = 0
            showSparepartRows = safeSparepart > 0
        )
    }

    /** Tarif jasa berdasarkan level yang dipilih. */
    fun feeForLevel(level: ServiceLevel, prefs: SettingsPrefs): Long = when (level) {
        ServiceLevel.LEVEL_1 -> prefs.level1Fee
        ServiceLevel.LEVEL_2 -> prefs.level2Fee
        ServiceLevel.LEVEL_3 -> prefs.level3Fee
    }

    /** Durasi garansi (hari) berdasarkan level yang dipilih. */
    fun warrantyDaysForLevel(level: ServiceLevel, prefs: SettingsPrefs): Int = when (level) {
        ServiceLevel.LEVEL_1 -> prefs.warrantyDaysL1
        ServiceLevel.LEVEL_2 -> prefs.warrantyDaysL2
        ServiceLevel.LEVEL_3 -> prefs.warrantyDaysL3
    }
}
