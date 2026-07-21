package com.ketantech.kalkulatorservis

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ketantech.kalkulatorservis.data.AppRepository
import com.ketantech.kalkulatorservis.databinding.ActivityReportBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Laporan harian: omzet, jumlah servis, rata-rata, dan breakdown per level.
 */
class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private lateinit var repository: AppRepository

    private val rupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_report)

        repository = AppRepository(this)
        loadReport()
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized) loadReport()
    }

    private fun loadReport() {
        lifecycleScope.launch {
            val todayCount = repository.getTodayCount()
            val todayTotal = repository.getTodayTotal()
            val weekTotal = repository.getWeekTotal()
            val monthTotal = repository.getMonthTotal()
            val byLevel = repository.getTodayByLevel()

            binding.tvTodayTotal.text = rupiah.format(todayTotal)
            binding.tvTodayCount.text = getString(R.string.report_service_count) + ": " + todayCount
            binding.tvWeekTotal.text = rupiah.format(weekTotal)
            binding.tvMonthTotal.text = rupiah.format(monthTotal)

            val avg = if (todayCount > 0) todayTotal / todayCount else 0
            binding.tvAverage.text = rupiah.format(avg)

            renderLevelBreakdown(byLevel)
        }
    }

    /** Gambar baris per level secara dinamis. */
    private fun renderLevelBreakdown(byLevel: List<com.ketantech.kalkulatorservis.data.LevelSummary>) {
        val container = binding.layoutLevels
        container.removeAllViews()

        if (byLevel.isEmpty()) {
            val empty = TextView(this).apply {
                text = getString(R.string.report_empty_level)
                setTextColor(ContextCompat.getColor(this@ReportActivity, R.color.slate_text_secondary))
                textSize = 13f
                gravity = Gravity.CENTER
            }
            container.addView(empty)
            return
        }

        val levelMap = byLevel.associateBy { it.serviceLevel }
        for (tier in 1..3) {
            val summary = levelMap[tier]
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                if (tier > 1) setPadding(0, dpToPx(10), 0, 0)
            }

            val label = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = getString(R.string.report_level_format, tier)
                setTextColor(ContextCompat.getColor(this@ReportActivity, R.color.slate_text))
                textSize = 14f
            }

            val value = TextView(this).apply {
                text = if (summary != null) {
                    "${summary.count}x • ${rupiah.format(summary.total)}"
                } else {
                    "0x • ${rupiah.format(0)}"
                }
                setTextColor(ContextCompat.getColor(
                    this@ReportActivity,
                    if (summary != null) R.color.blue_primary else R.color.slate_text_secondary
                ))
                textSize = 14f
                typeface = Typeface.MONOSPACE
            }

            row.addView(label)
            row.addView(value)
            container.addView(row)
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
