package com.ketantech.kalkulatorservis

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ketantech.kalkulatorservis.databinding.ActivitySettingsBinding

/**
 * Layar pengaturan (PRD 5.3). Tanpa PIN — penggunaan pribadi.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SettingsPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        prefs = SettingsPrefs(this)
        loadValues()

        binding.btnSaveSettings.setOnClickListener { saveValues() }
        binding.btnResetDefaults.setOnClickListener { confirmReset() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadValues() {
        binding.etRiskMargin.setText(prefs.riskMarginPercent.toString())
        binding.etOperationalCost.setText(prefs.operationalCost.toString())
        binding.etLevel1Fee.setText(prefs.level1Fee.toString())
        binding.etLevel2Fee.setText(prefs.level2Fee.toString())
        binding.etLevel3Fee.setText(prefs.level3Fee.toString())
        binding.etWarrantyL1.setText(prefs.warrantyDaysL1.toString())
        binding.etWarrantyL2.setText(prefs.warrantyDaysL2.toString())
        binding.etWarrantyL3.setText(prefs.warrantyDaysL3.toString())
    }

    private fun saveValues() {
        // Parsing aman: input kosong/invalid mempertahankan nilai lama (PRD 8)
        binding.etRiskMargin.text.toString().toIntOrNull()
            ?.let { prefs.riskMarginPercent = it }
        binding.etOperationalCost.text.toString().toLongOrNull()
            ?.let { prefs.operationalCost = it }
        binding.etLevel1Fee.text.toString().toLongOrNull()
            ?.let { prefs.level1Fee = it }
        binding.etLevel2Fee.text.toString().toLongOrNull()
            ?.let { prefs.level2Fee = it }
        binding.etLevel3Fee.text.toString().toLongOrNull()
            ?.let { prefs.level3Fee = it }
        binding.etWarrantyL1.text.toString().toIntOrNull()
            ?.let { prefs.warrantyDaysL1 = it }
        binding.etWarrantyL2.text.toString().toIntOrNull()
            ?.let { prefs.warrantyDaysL2 = it }
        binding.etWarrantyL3.text.toString().toIntOrNull()
            ?.let { prefs.warrantyDaysL3 = it }

        // Tampilkan kembali nilai tersimpan (setelah clamping/coercion)
        loadValues()
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle(R.string.settings_reset_title)
            .setMessage(R.string.settings_reset_message)
            .setPositiveButton(R.string.settings_reset_confirm) { _, _ ->
                prefs.resetToDefaults()
                loadValues()
                Toast.makeText(this, R.string.settings_reset_done, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
