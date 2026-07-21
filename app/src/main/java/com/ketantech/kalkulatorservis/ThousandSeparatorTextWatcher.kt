package com.ketantech.kalkulatorservis

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.NumberFormat
import java.util.Locale

/**
 * TextWatcher untuk format ribuan otomatis (450000 -> 450.000)
 * dan parsing balik ke Long (450.000 -> 450000).
 *
 * Digunakan di input modal sparepart agar teknisi mudah membaca angka besar.
 */
class ThousandSeparatorTextWatcher(
    private val editText: EditText,
    private val onValueChanged: (Long) -> Unit
) : TextWatcher {

    private var isFormatting = false
    private val formatter = NumberFormat.getNumberInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isFormatting) return
        isFormatting = true

        try {
            val original = s.toString()
            // Hapus semua non-digit untuk parsing
            val clean = original.replace(Regex("[^0-9]"), "")

            if (clean.isEmpty()) {
                editText.setText("")
                editText.setSelection(0)
                onValueChanged(0)
                return
            }

            val value = clean.toLong()
            val formatted = formatter.format(value)

            // Set text hanya jika berbeda (hindari infinite loop)
            if (original != formatted) {
                editText.setText(formatted)
                // Posisikan cursor di akhir
                editText.setSelection(formatted.length)
            }

            onValueChanged(value)
        } catch (e: Exception) {
            // Fallback: kirim 0 jika parsing gagal
            onValueChanged(0)
        } finally {
            isFormatting = false
        }
    }

    companion object {
        /**
         * Parse string dengan atau tanpa separator ke Long.
         * "450.000" -> 450000, "450000" -> 450000, "" -> 0
         */
        fun parse(text: String): Long {
            val clean = text.replace(Regex("[^0-9]"), "")
            return clean.toLongOrNull() ?: 0L
        }

        /**
         * Format Long ke string ribuan Indonesia.
         * 450000 -> "450.000"
         */
        fun format(value: Long): String {
            return NumberFormat.getNumberInstance(Locale("in", "ID"))
                .apply { maximumFractionDigits = 0 }
                .format(value)
        }
    }
}
