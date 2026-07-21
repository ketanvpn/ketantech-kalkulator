package com.ketantech.kalkulatorservis

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ketantech.kalkulatorservis.data.AppRepository
import com.ketantech.kalkulatorservis.data.Receipt
import com.ketantech.kalkulatorservis.databinding.ActivityHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var repository: AppRepository
    private lateinit var adapter: ReceiptAdapter

    private val rupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_history)

        repository = AppRepository(this)
        setupRecyclerView()
        setupSearch()
        loadStats()
    }

    private fun setupRecyclerView() {
        adapter = ReceiptAdapter(
            onItemClick = { receipt -> showReceiptDetail(receipt) },
            onDeleteClick = { receipt -> confirmDelete(receipt) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                observeReceipts(s.toString())
            }
        })
        observeReceipts("")
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    private fun observeReceipts(query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            val flow = if (query.isEmpty()) repository.getRecentReceipts()
            else repository.searchReceipts(query)
            flow.collectLatest { receipts ->
                adapter.submitList(receipts)
                binding.layoutEmpty.visibility =
                    if (receipts.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadStats() {
        lifecycleScope.launch {
            val count = repository.getTodayCount()
            val total = repository.getTodayTotal()
            binding.tvTodayCount.text = count.toString()
            binding.tvTodayTotal.text = rupiah.format(total)
        }
    }

    private fun showReceiptDetail(receipt: Receipt) {
        // Buka bottom sheet dengan data dari database
        val bottomSheet = ReceiptBottomSheet.newInstance(
            receiptNumber = receipt.receiptNumber,
            deviceName = receipt.deviceName,
            customerName = receipt.customerName,
            result = CalculationResult(
                sparepartCost = receipt.sparepartCost,
                riskFund = receipt.riskFund,
                operationalCost = receipt.operationalCost,
                serviceFee = receipt.serviceFee,
                total = receipt.total,
                showSparepartRows = receipt.sparepartCost > 0
            ),
            serviceLevel = when (receipt.serviceLevel) {
                2 -> ServiceLevel.LEVEL_2
                3 -> ServiceLevel.LEVEL_3
                else -> ServiceLevel.LEVEL_1
            },
            warrantyDays = receipt.warrantyDays,
            onShareClick = { shareReceipt(receipt) },
            onDoneClick = { }
        )
        bottomSheet.show(supportFragmentManager, ReceiptBottomSheet.TAG)
    }

    /** Share ulang nota dari riwayat sebagai teks. */
    private fun shareReceipt(receipt: Receipt) {
        val date = java.text.SimpleDateFormat("d MMMM yyyy", Locale("in", "ID"))
            .format(receipt.createdAt)
        val sb = StringBuilder()
        sb.appendLine("🧾 *KETANTECH STORE*")
        sb.appendLine("Estimasi Biaya Servis")
        sb.appendLine("─".repeat(28))
        sb.appendLine("No: ${receipt.receiptNumber}")
        sb.appendLine("Tgl: $date")
        sb.appendLine()
        if (receipt.deviceName.isNotEmpty()) sb.appendLine("📱 *${receipt.deviceName}*")
        if (receipt.customerName.isNotEmpty()) sb.appendLine("👤 ${receipt.customerName}")
        sb.appendLine()
        sb.appendLine("*RINCIAN BIAYA*")
        if (receipt.sparepartCost > 0) {
            sb.appendLine("• Suku Cadang: ${rupiah.format(receipt.sparepartCost)}")
            sb.appendLine("• Garansi & QC: ${rupiah.format(receipt.riskFund)}")
        }
        sb.appendLine("• Bahan Penunjang: ${rupiah.format(receipt.operationalCost)}")
        sb.appendLine("• Jasa Teknisi L${receipt.serviceLevel}: ${rupiah.format(receipt.serviceFee)}")
        sb.appendLine()
        sb.appendLine("─".repeat(28))
        sb.appendLine("💰 *TOTAL: ${rupiah.format(receipt.total)}*")
        sb.appendLine("─".repeat(28))
        sb.appendLine()
        sb.appendLine("Garansi berlaku ${receipt.warrantyDays} hari untuk jasa pemasangan dan komponen yang diganti.")
        sb.appendLine()
        sb.appendLine("Terima kasih 🙏")

        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, sb.toString())
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Bagikan nota via..."))
    }

    private fun confirmDelete(receipt: Receipt) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_message, receipt.receiptNumber))
            .setPositiveButton(R.string.delete_confirm) { _, _ ->
                lifecycleScope.launch {
                    repository.deleteReceipt(receipt.id)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
