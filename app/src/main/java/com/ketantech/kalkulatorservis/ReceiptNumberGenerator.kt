package com.ketantech.kalkulatorservis

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generator nomor nota harian (PRD 5.2 & 9.2).
 * Format: KTS-YYYYMMDD-XXX — counter reset setiap ganti hari.
 *
 * Nomor baru hanya di-generate saat nota di-finalkan (tombol "Nota Baru"),
 * bukan saat angka kalkulasi berubah (PRD 8 & 10.1).
 */
class ReceiptNumberGenerator(context: Context) {

    private val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

    /** Pratinjau nomor nota berikutnya tanpa menaikkan counter. */
    fun peekNext(): String {
        val today = dateFormat.format(Date())
        val counter = currentCounterFor(today) + 1
        return format(today, counter)
    }

    /** Ambil nomor nota baru DAN naikkan counter (saat finalisasi). */
    fun consumeNext(): String {
        val today = dateFormat.format(Date())
        val counter = currentCounterFor(today) + 1
        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_COUNTER, counter)
            .apply()
        return format(today, counter)
    }

    private fun currentCounterFor(today: String): Int {
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        return if (lastDate == today) prefs.getInt(KEY_COUNTER, 0) else 0
    }

    private fun format(date: String, counter: Int): String =
        "KTS-$date-${counter.toString().padStart(3, '0')}"

    companion object {
        private const val FILE_NAME = "ketantech_receipt_counter"
        private const val KEY_LAST_DATE = "lastDate"
        private const val KEY_COUNTER = "counter"
    }
}
