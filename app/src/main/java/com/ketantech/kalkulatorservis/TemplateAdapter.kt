package com.ketantech.kalkulatorservis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ketantech.kalkulatorservis.data.ServiceTemplate
import java.text.NumberFormat
import java.util.Locale

class TemplateAdapter(
    private val onApplyClick: (ServiceTemplate) -> Unit,
    private val onDeleteClick: (ServiceTemplate) -> Unit
) : ListAdapter<ServiceTemplate, TemplateAdapter.ViewHolder>(DiffCallback()) {

    private val rupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDefaultBadge: TextView = view.findViewById(R.id.tvDefaultBadge)
        val tvDetail: TextView = view.findViewById(R.id.tvDetail)
        val btnApply: MaterialButton = view.findViewById(R.id.btnApply)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_template, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val template = getItem(position)
        holder.tvName.text = template.name
        holder.tvDefaultBadge.visibility = if (template.isDefault) View.VISIBLE else View.GONE

        val parts = mutableListOf<String>()
        if (template.deviceName.isNotEmpty()) parts.add(template.deviceName)
        if (template.sparepartCost > 0) parts.add(rupiah.format(template.sparepartCost))
        parts.add(holder.itemView.context.getString(R.string.template_level_format, template.serviceLevel))
        holder.tvDetail.text = parts.joinToString(" • ")

        holder.btnApply.setOnClickListener { onApplyClick(template) }
        holder.btnDelete.visibility = if (template.isDefault) View.GONE else View.VISIBLE
        holder.btnDelete.setOnClickListener { onDeleteClick(template) }
    }

    class DiffCallback : DiffUtil.ItemCallback<ServiceTemplate>() {
        override fun areItemsTheSame(oldItem: ServiceTemplate, newItem: ServiceTemplate) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ServiceTemplate, newItem: ServiceTemplate) =
            oldItem == newItem
    }
}
