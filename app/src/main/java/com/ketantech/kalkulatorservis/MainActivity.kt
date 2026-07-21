package com.ketantech.kalkulatorservis

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ketantech.kalkulatorservis.data.AppRepository
import com.ketantech.kalkulatorservis.data.Customer
import com.ketantech.kalkulatorservis.data.Receipt
import com.ketantech.kalkulatorservis.data.ServiceTemplate
import com.ketantech.kalkulatorservis.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Layar utama: kalkulator input teknisi.
 * Nota ditampilkan sebagai popup bottom sheet saat klik HITUNG.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SettingsPrefs
    private lateinit var receiptGen: ReceiptNumberGenerator
    private val repository: AppRepository by lazy { AppRepository(this) }

    private var knownCustomers: List<Customer> = emptyList()
    private var customerAdapter: ArrayAdapter<String>? = null

    /** Terima template yang dipilih dari TemplatesActivity. */
    private val pickTemplate = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val template = ServiceTemplate(
                id = data.getLongExtra(TemplatesActivity.EXTRA_TEMPLATE_ID, 0),
                name = data.getStringExtra(TemplatesActivity.EXTRA_TEMPLATE_NAME) ?: "",
                deviceName = data.getStringExtra(TemplatesActivity.EXTRA_TEMPLATE_DEVICE) ?: "",
                sparepartCost = data.getLongExtra(TemplatesActivity.EXTRA_TEMPLATE_SPAREPART, 0),
                serviceLevel = data.getIntExtra(TemplatesActivity.EXTRA_TEMPLATE_LEVEL, 1)
            )
            applyTemplate(template)
        }
    }

    private val rupiah: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
    private val displayDateFormat =
        SimpleDateFormat("d MMMM yyyy", Locale("in", "ID"))

    private var hasCalculated = false
    private var currentResult: CalculationResult? = null
    private var currentLevel: ServiceLevel = ServiceLevel.LEVEL_1
    private var currentWarrantyDays: Int = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek onboarding: hanya tampil sekali saat pertama install
        val onboardingPrefs = OnboardingPrefs(this)
        if (!onboardingPrefs.hasSeenOnboarding) {
            startActivity(Intent(this, OnboardingActivity::class.java))
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = SettingsPrefs(this)
        receiptGen = ReceiptNumberGenerator(this)

        setupInputs()
        setupButtons()
        observeCustomers()

        // Pulihkan input setelah rotasi layar
        savedInstanceState?.let { restoreState(it) }

        // Skeleton loading
        showSkeletonThenContent()
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
            override fun afterTextChanged(s: Editable?) {
                // Update data terbaru untuk next calculate
                updateCurrentData()
            }
        }
        binding.etDeviceName.addTextChangedListener(watcher)
        binding.etCustomerName.addTextChangedListener(watcher)

        binding.etSparepartCost.addTextChangedListener(
            ThousandSeparatorTextWatcher(binding.etSparepartCost) { _ ->
                updateCurrentData()
            }
        )

        binding.rgServiceLevel.setOnCheckedChangeListener { group, _ ->
            group.performHapticFeedback(android.view.HapticFeedbackConstants.SEGMENT_TICK)
            updateCurrentData()
        }
    }

    private fun setupButtons() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnTemplates.setOnClickListener {
            pickTemplate.launch(Intent(this, TemplatesActivity::class.java))
        }

        binding.btnCustomers.setOnClickListener {
            startActivity(Intent(this, CustomersActivity::class.java))
        }

        binding.btnReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        binding.btnCalculate.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            hasCalculated = true
            updateCurrentData()
            showReceiptPopup()
        }

        binding.btnNewReceipt.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            saveCurrentReceipt()
        }

        binding.btnSaveTemplate.setOnClickListener {
            showSaveTemplateDialog()
        }
    }

    /** Simpan nota ke database, lalu reset form untuk nota berikutnya. */
    private fun saveCurrentReceipt() {
        val result = currentResult ?: run {
            // Belum pernah hitung: cukup konsumsi nomor & bersihkan
            receiptGen.consumeNext()
            clearInputs()
            showSuccessAnimation()
            return
        }

        val receiptNumber = receiptGen.consumeNext()
        val device = binding.etDeviceName.text.toString().trim()
        val customer = binding.etCustomerName.text.toString().trim()

        val receipt = Receipt(
            receiptNumber = receiptNumber,
            deviceName = device,
            customerName = customer,
            sparepartCost = result.sparepartCost,
            riskFund = result.riskFund,
            operationalCost = result.operationalCost,
            serviceFee = result.serviceFee,
            total = result.total,
            serviceLevel = currentLevel.tier,
            warrantyDays = currentWarrantyDays
        )

        lifecycleScope.launch {
            repository.saveReceipt(receipt)
            if (customer.isNotEmpty()) {
                repository.upsertCustomer(customer)
            }
        }

        clearInputs()
        showSuccessAnimation()
    }

    /** Auto-complete nama pelanggan dari database. */
    private fun observeCustomers() {
        val actv = binding.etCustomerName as? android.widget.AutoCompleteTextView ?: return
        lifecycleScope.launch {
            repository.getAllCustomers().collect { customers ->
                knownCustomers = customers
                customerAdapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    customers.map { it.name }
                )
                actv.setAdapter(customerAdapter)
                actv.threshold = 1
            }
        }
    }

    /** Dialog simpan input saat ini sebagai template. */
    private fun showSaveTemplateDialog() {
        val input = EditText(this).apply {
            hint = getString(R.string.template_name_hint)
            val device = binding.etDeviceName.text.toString().trim()
            if (device.isNotEmpty()) setText(device)
            setSelectAllOnFocus(true)
        }

        val container = android.widget.FrameLayout(this)
        val pad = (20 * resources.displayMetrics.density).toInt()
        container.setPadding(pad, pad / 2, pad, 0)
        container.addView(input)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.template_save_title)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) return@setPositiveButton
                val template = ServiceTemplate(
                    name = name,
                    deviceName = binding.etDeviceName.text.toString().trim(),
                    sparepartCost = ThousandSeparatorTextWatcher.parse(
                        binding.etSparepartCost.text.toString()
                    ),
                    serviceLevel = selectedLevel().tier
                )
                lifecycleScope.launch {
                    repository.saveTemplate(template)
                    Toast.makeText(
                        this@MainActivity,
                        R.string.template_saved,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /** Terapkan template ke form (dipanggil dari TemplatesActivity). */
    fun applyTemplate(template: ServiceTemplate) {
        binding.etDeviceName.setText(template.deviceName)
        binding.etSparepartCost.setText(
            if (template.sparepartCost > 0)
                ThousandSeparatorTextWatcher.format(template.sparepartCost)
            else ""
        )
        when (template.serviceLevel) {
            2 -> binding.rbLevel2.isChecked = true
            3 -> binding.rbLevel3.isChecked = true
            else -> binding.rbLevel1.isChecked = true
        }
        Toast.makeText(this, R.string.template_applied, Toast.LENGTH_SHORT).show()
    }

    /** Simpan data terkini untuk ditampilkan di popup. */
    private fun updateCurrentData() {
        val sparepartText = binding.etSparepartCost.text.toString()
        val sparepart = ThousandSeparatorTextWatcher.parse(sparepartText)
        currentLevel = selectedLevel()
        val serviceFee = PriceCalculator.feeForLevel(currentLevel, prefs)
        currentWarrantyDays = PriceCalculator.warrantyDaysForLevel(currentLevel, prefs)

        currentResult = PriceCalculator.calculate(
            sparepartCost = sparepart,
            riskMarginPercent = prefs.riskMarginPercent,
            operationalCost = prefs.operationalCost,
            serviceFee = serviceFee
        )
    }

    /** Tampilkan nota sebagai popup bottom sheet. */
    private fun showReceiptPopup() {
        val result = currentResult ?: return

        val bottomSheet = ReceiptBottomSheet.newInstance(
            receiptNumber = receiptGen.peekNext(),
            deviceName = binding.etDeviceName.text.toString().trim(),
            customerName = binding.etCustomerName.text.toString().trim(),
            result = result,
            serviceLevel = currentLevel,
            warrantyDays = currentWarrantyDays,
            onShareClick = { shareReceiptAsText() },
            onDoneClick = { /* tutup saja */ }
        )
        bottomSheet.show(supportFragmentManager, ReceiptBottomSheet.TAG)
    }

    /** Format nota sebagai teks siap share ke WhatsApp. */
    private fun shareReceiptAsText() {
        val result = currentResult ?: return

        val device = binding.etDeviceName.text.toString().trim()
        val customer = binding.etCustomerName.text.toString().trim()
        val receiptNumber = receiptGen.peekNext()
        val date = displayDateFormat.format(Date())

        val sb = StringBuilder()
        sb.appendLine("🧾 *KETANTECH STORE*")
        sb.appendLine("Estimasi Biaya Servis")
        sb.appendLine("─".repeat(28))
        sb.appendLine("No: $receiptNumber")
        sb.appendLine("Tgl: $date")
        sb.appendLine()
        if (device.isNotEmpty()) sb.appendLine("📱 *$device*")
        if (customer.isNotEmpty()) sb.appendLine("👤 $customer")
        sb.appendLine()
        sb.appendLine("*RINCIAN BIAYA*")
        if (result.showSparepartRows) {
            sb.appendLine("• Suku Cadang: ${rupiah.format(result.sparepartCost)}")
            sb.appendLine("• Garansi & QC: ${rupiah.format(result.riskFund)}")
        }
        sb.appendLine("• Bahan Penunjang: ${rupiah.format(result.operationalCost)}")
        sb.appendLine("• Jasa Teknisi L${currentLevel.tier}: ${rupiah.format(result.serviceFee)}")
        sb.appendLine()
        sb.appendLine("─".repeat(28))
        sb.appendLine("💰 *TOTAL: ${rupiah.format(result.total)}*")
        sb.appendLine("─".repeat(28))
        sb.appendLine()
        sb.appendLine("Garansi berlaku $currentWarrantyDays hari untuk jasa pemasangan dan komponen yang diganti.")
        sb.appendLine()
        sb.appendLine("Terima kasih 🙏")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            putExtra(Intent.EXTRA_SUBJECT, "Nota Servis $receiptNumber")
        }
        startActivity(Intent.createChooser(shareIntent, "Bagikan nota via..."))
    }

    private fun clearInputs() {
        binding.etDeviceName.text?.clear()
        binding.etCustomerName.text?.clear()
        binding.etSparepartCost.text?.clear()
        binding.rbLevel1.isChecked = true
        hasCalculated = false
        currentResult = null
    }

    private fun selectedLevel(): ServiceLevel = when (binding.rgServiceLevel.checkedRadioButtonId) {
        R.id.rbLevel2 -> ServiceLevel.LEVEL_2
        R.id.rbLevel3 -> ServiceLevel.LEVEL_3
        else -> ServiceLevel.LEVEL_1
    }

    /** Tampilkan skeleton loading 600ms, lalu fade ke konten asli. */
    private fun showSkeletonThenContent() {
        binding.layoutSkeleton.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE

        binding.root.postDelayed({
            binding.layoutSkeleton.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    binding.layoutSkeleton.visibility = View.GONE
                    binding.layoutSkeleton.alpha = 1f

                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.layoutEmptyState.alpha = 0f
                    binding.layoutEmptyState.animate().alpha(1f).setDuration(300).start()
                }
                .start()
        }, 600)
    }

    /** Animasi success: scale up + fade, lalu hilang. */
    private fun showSuccessAnimation() {
        binding.layoutSuccessOverlay.visibility = View.VISIBLE
        binding.layoutSuccessOverlay.alpha = 0f
        binding.layoutSuccessOverlay.scaleX = 0.5f
        binding.layoutSuccessOverlay.scaleY = 0.5f

        binding.layoutSuccessOverlay.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .withEndAction {
                binding.layoutSuccessOverlay.postDelayed({
                    binding.layoutSuccessOverlay.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction {
                            binding.layoutSuccessOverlay.visibility = View.GONE
                        }
                        .start()
                }, 1200)
            }
            .start()
    }

    companion object {
        private const val STATE_DEVICE = "state_device"
        private const val STATE_CUSTOMER = "state_customer"
        private const val STATE_SPAREPART = "state_sparepart"
        private const val STATE_LEVEL = "state_level"
    }
}
