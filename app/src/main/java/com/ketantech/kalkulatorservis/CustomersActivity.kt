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
import com.ketantech.kalkulatorservis.data.Customer
import com.ketantech.kalkulatorservis.databinding.ActivityCustomersBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Database pelanggan: daftar nama, jumlah servis, dan riwayat per pelanggan.
 */
class CustomersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomersBinding
    private lateinit var repository: AppRepository
    private lateinit var adapter: CustomerAdapter

    private val rupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }
    private val dateFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("in", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_customers)

        repository = AppRepository(this)
        setupRecyclerView()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = CustomerAdapter(
            onItemClick = { customer -> showCustomerHistory(customer) },
            onDeleteClick = { customer -> confirmDelete(customer) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadCustomers("")
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                loadCustomers(s.toString())
            }
        })
    }

    private var loadJob: kotlinx.coroutines.Job? = null

    private fun loadCustomers(query: String) {
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            val flow = if (query.isEmpty()) repository.getAllCustomers()
            else repository.searchCustomers(query)
            flow.collectLatest { customers ->
                // Hitung jumlah servis tiap pelanggan
                val items = customers.map { customer ->
                    CustomerAdapter.CustomerItem(
                        customer = customer,
                        serviceCount = repository.countServicesForCustomer(customer.name)
                    )
                }
                adapter.submitList(items)
                binding.layoutEmpty.visibility =
                    if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    /** Tampilkan riwayat servis pelanggan dalam dialog. */
    private fun showCustomerHistory(customer: Customer) {
        lifecycleScope.launch {
            val receipts = repository.getReceiptsByCustomer(customer.name).first()

            val message = if (receipts.isEmpty()) {
                getString(R.string.customer_no_history)
            } else {
                receipts.take(10).joinToString("\n\n") { r ->
                    "${r.receiptNumber}\n" +
                            "${r.deviceName.ifEmpty { "-" }} • ${rupiah.format(r.total)}\n" +
                            dateFormat.format(r.createdAt)
                }
            }
            AlertDialog.Builder(this@CustomersActivity)
                .setTitle(getString(R.string.customer_history_title, customer.name))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun confirmDelete(customer: Customer) {
        AlertDialog.Builder(this)
            .setTitle(R.string.customer_delete_title)
            .setMessage(getString(R.string.customer_delete_message, customer.name))
            .setPositiveButton(R.string.delete_confirm) { _, _ ->
                lifecycleScope.launch { repository.deleteCustomer(customer) }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
