package com.ketantech.kalkulatorservis

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ketantech.kalkulatorservis.data.AppRepository
import com.ketantech.kalkulatorservis.data.ServiceTemplate
import com.ketantech.kalkulatorservis.databinding.ActivityTemplatesBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Daftar template servis: pakai untuk mengisi form, atau hapus template buatan sendiri.
 */
class TemplatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTemplatesBinding
    private lateinit var repository: AppRepository
    private lateinit var adapter: TemplateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemplatesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_templates)

        repository = AppRepository(this)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = TemplateAdapter(
            onApplyClick = { template -> applyTemplate(template) },
            onDeleteClick = { template -> confirmDelete(template) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            repository.getAllTemplates().collectLatest { templates ->
                adapter.submitList(templates)
                binding.layoutEmpty.visibility =
                    if (templates.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    /** Kirim template ke MainActivity dan tutup layar ini. */
    private fun applyTemplate(template: ServiceTemplate) {
        val intent = android.content.Intent().apply {
            putExtra(EXTRA_TEMPLATE_ID, template.id)
            putExtra(EXTRA_TEMPLATE_NAME, template.name)
            putExtra(EXTRA_TEMPLATE_DEVICE, template.deviceName)
            putExtra(EXTRA_TEMPLATE_SPAREPART, template.sparepartCost)
            putExtra(EXTRA_TEMPLATE_LEVEL, template.serviceLevel)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun confirmDelete(template: ServiceTemplate) {
        AlertDialog.Builder(this)
            .setTitle(R.string.template_delete_title)
            .setMessage(getString(R.string.template_delete_message, template.name))
            .setPositiveButton(R.string.delete_confirm) { _, _ ->
                lifecycleScope.launch { repository.deleteTemplate(template.id) }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_TEMPLATE_ID = "extra_template_id"
        const val EXTRA_TEMPLATE_NAME = "extra_template_name"
        const val EXTRA_TEMPLATE_DEVICE = "extra_template_device"
        const val EXTRA_TEMPLATE_SPAREPART = "extra_template_sparepart"
        const val EXTRA_TEMPLATE_LEVEL = "extra_template_level"
    }
}
