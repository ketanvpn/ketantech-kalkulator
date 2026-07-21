package com.ketantech.kalkulatorservis

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ketantech.kalkulatorservis.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Layar utama: kalkulator input teknisi (atas) + nota pelanggan (bawah).
 * Nota ter-update real-time saat input berubah (PRD 11.2).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SettingsPrefs
    private lateinit var receiptGen: ReceiptNumberGenerator

    private val rupiah: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
    private val displayDateFormat =
        SimpleDateFormat("d MMMM yyyy", Locale("in", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = SettingsPrefs(this)
        receiptGen = ReceiptNumberGenerator(this)

        setupInputs()
        setupButtons()

        // Pulihkan input setelah rotasi layar (PRD 8: rotasi tidak menghilangkan input)
        savedInstanceState?.let { restoreState(it) }

        recalculate()
    }

    override fun onResume() {
        super.onResume()
        // Settings mungkin berubah di SettingsActivity — hitung ulang
        recalculate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_DEVICE, binding.etDeviceName.text.toString())
        outState.putString(STATE_CUSTOMER, binding.etCustomerName.text.toString())
        outState.putString(STATE_SPAREPART, binding.etSparepartCost.text.toString())
        outState.putInt(STATE_LEVEL, selectedLevel().tier)
    }

    private fun restoreState(state: Bundle) {
        binding.etDeviceName.setText(state.getString(STATE_DEVICE, ""))
        binding.etCustomerName.setText(state.getString(STATE_CUSTOMER, ""))
        binding.etSparepartCost.setText(state.getString(STATE_SPAREPART, ""))
        when (state.getInt(STATE_LEVEL, 1)) {
            2 -> binding.rbLevel2.isChecked = true
            3 -> binding.rbLevel3.isChecked = true
            else -> binding.rbLevel1.isChecked = true
        }
    }

    private fun setupInputs() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = recalculate()
        }
        binding.etDeviceName.addTextChangedListener(watcher)
        binding.etCustomerName.addTextChangedListener(watcher)
        binding.etSparepartCost.addTextChangedListener(watcher)
        binding.rgServiceLevel.setOnCheckedChangeListener { _, _ -> recalculate() }
    }

    private fun setupButtons() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnNewReceipt.setOnClickListener {
            // Finalisasi: nomor nota naik (PRD 10.1), lalu form dibersihkan
            receiptGen.consumeNext()
            clearInputs()
        }
    }

    private fun clearInputs() {
        binding.etDeviceName.text?.clear()
        binding.etCustomerName.text?.clear()
        binding.etSparepartCost.text?.clear()
        binding.rbLevel1.isChecked = true
        recalculate()
    }

    private fun selectedLevel(): ServiceLevel = when (binding.rgServiceLevel.checkedRadioButtonId) {
        R.id.rbLevel2 -> ServiceLevel.LEVEL_2
        R.id.rbLevel3 -> ServiceLevel.LEVEL_3
        else -> ServiceLevel.LEVEL_1
    }

    /** Hitung ulang dan perbarui seluruh tampilan nota. */
    private fun recalculate() {
        // Parsing aman: huruf/minus tidak mungkin masuk (inputType=number),
        // string kosong/null dianggap 0 (PRD 8)
        val sparepart = binding.etSparepartCost.text.toString().toLongOrNull() ?: 0L
        val level = selectedLevel()
        val serviceFee = PriceCalculator.feeForLevel(level, prefs)
        val warrantyDays = PriceCalculator.warrantyDaysForLevel(level, prefs)

        val result = PriceCalculator.calculate(
            sparepartCost = sparepart,
            riskMarginPercent = prefs.riskMarginPercent,
            operationalCost = prefs.operationalCost,
            serviceFee = serviceFee
        )
        renderReceipt(result, level, warrantyDays)
    }

    private fun renderReceipt(
        result: CalculationResult,
        level: ServiceLevel,
        warrantyDays: Int
    ) {
        // Info nota
        binding.tvReceiptNumber.text = receiptGen.peekNext()
        binding.tvReceiptDate.text = displayDateFormat.format(Date())

        val device = binding.etDeviceName.text.toString().trim()
        binding.tvReceiptDevice.text =
            if (device.isEmpty()) getString(R.string.receipt_no_device) else device

        val customer = binding.etCustomerName.text.toString().trim()
        binding.tvReceiptCustomer.text = getString(
            R.string.receipt_customer_format,
            customer.ifEmpty { getString(R.string.receipt_default_customer) }
        )

        // PRD 7.2: baris sparepart & garansi hanya muncul jika modal > 0
        val sparepartVisibility = if (result.showSparepartRows) View.VISIBLE else View.GONE
        binding.rowSparepart.visibility = sparepartVisibility
        binding.rowRiskFund.visibility = sparepartVisibility

        binding.tvSparepartValue.text = rupiah.format(result.sparepartCost)
        binding.tvRiskFundValue.text = rupiah.format(result.riskFund)
        binding.tvOperationalValue.text = rupiah.format(result.operationalCost)

        binding.tvServiceFeeLabel.text =
            getString(R.string.receipt_service_fee_level, level.tier)
        binding.tvServiceFeeValue.text = rupiah.format(result.serviceFee)

        binding.tvTotalValue.text = rupiah.format(result.total)

        // Pesan edukasi dengan durasi garansi dinamis (PRD 5.2)
        binding.tvWarrantyMessage.text =
            getString(R.string.receipt_warranty_message, warrantyDays)
    }

    companion object {
        private const val STATE_DEVICE = "state_device"
        private const val STATE_CUSTOMER = "state_customer"
        private const val STATE_SPAREPART = "state_sparepart"
        private const val STATE_LEVEL = "state_level"
    }
}
