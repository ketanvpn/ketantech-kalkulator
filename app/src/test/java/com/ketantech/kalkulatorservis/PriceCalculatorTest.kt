package com.ketantech.kalkulatorservis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test untuk logika perhitungan — memverifikasi formula PRD 7.1
 * dan contoh perhitungan PRD 7.3.
 */
class PriceCalculatorTest {

    @Test
    fun `contoh PRD 7_3 - ganti LCD Level 2 menghasilkan total Rp665_000`() {
        // Skenario PRD: Modal 450.000, margin 10%, operasional 20.000, jasa L2 150.000
        val result = PriceCalculator.calculate(
            sparepartCost = 450_000,
            riskMarginPercent = 10,
            operationalCost = 20_000,
            serviceFee = 150_000
        )

        assertEquals(450_000, result.sparepartCost)
        assertEquals(45_000, result.riskFund)
        assertEquals(20_000, result.operationalCost)
        assertEquals(150_000, result.serviceFee)
        assertEquals(665_000, result.total)
        assertTrue(result.showSparepartRows)
    }

    @Test
    fun `modal sparepart nol menyembunyikan baris sparepart dan garansi`() {
        // PRD 7.2: jika Modal_Sparepart = 0, baris sparepart & garansi disembunyikan
        val result = PriceCalculator.calculate(
            sparepartCost = 0,
            riskMarginPercent = 10,
            operationalCost = 20_000,
            serviceFee = 75_000
        )

        assertEquals(0, result.sparepartCost)
        assertEquals(0, result.riskFund)
        assertFalse(result.showSparepartRows)
        // Total tetap = operasional + jasa (servis software tanpa sparepart)
        assertEquals(95_000, result.total)
    }

    @Test
    fun `nilai negatif dikembalikan ke nol (clamping)`() {
        val result = PriceCalculator.calculate(
            sparepartCost = -50_000,
            riskMarginPercent = 10,
            operationalCost = -1,
            serviceFee = -100
        )

        assertEquals(0, result.sparepartCost)
        assertEquals(0, result.operationalCost)
        assertEquals(0, result.serviceFee)
        assertEquals(0, result.total)
        assertFalse(result.showSparepartRows)
    }

    @Test
    fun `persentase margin di atas 100 dipotong ke 100`() {
        val result = PriceCalculator.calculate(
            sparepartCost = 100_000,
            riskMarginPercent = 250,
            operationalCost = 0,
            serviceFee = 0
        )
        assertEquals(100_000, result.riskFund)
        assertEquals(200_000, result.total)
    }

    @Test
    fun `persentase margin nol menghasilkan dana risiko nol`() {
        val result = PriceCalculator.calculate(
            sparepartCost = 300_000,
            riskMarginPercent = 0,
            operationalCost = 20_000,
            serviceFee = 300_000
        )
        assertEquals(0, result.riskFund)
        assertEquals(620_000, result.total)
    }

    @Test
    fun `pembulatan dana risiko memakai integer division (floor)`() {
        // 33.333 x 10% = 3.333,3 -> floor 3.333 (Rupiah tanpa sen, PRD 8)
        val result = PriceCalculator.calculate(
            sparepartCost = 33_333,
            riskMarginPercent = 10,
            operationalCost = 0,
            serviceFee = 0
        )
        assertEquals(3_333, result.riskFund)
    }

    @Test
    fun `angka besar tetap dihitung dengan benar`() {
        val result = PriceCalculator.calculate(
            sparepartCost = 5_000_000_000,
            riskMarginPercent = 10,
            operationalCost = 20_000,
            serviceFee = 300_000
        )
        assertEquals(500_000_000, result.riskFund)
        assertEquals(5_500_320_000, result.total)
    }
}
