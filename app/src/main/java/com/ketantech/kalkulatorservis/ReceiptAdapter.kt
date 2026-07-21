package com.ketantech.kalkulatorservis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ketantech.kalkulatorservis.data.Receipt
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ReceiptAdapter(
    private val onItemClick: (Receipt) -> Unit,
    private val onDeleteClick: (Receipt) -> Unit
) : ListAdapter<Receipt, ReceiptAdapter.ViewHolder>(DiffCallback()) {

    private val rupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }
    private val dateFormat = SimpleDateFormat("d MMM, HH:mm", Locale("in", "ID"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tvReceiptNumber)
        val tvDevice: TextView = view.findViewById(R.id.tvDevice)
        val tvCustomer: TextView = view.findViewById(R.id.tvCustomer)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receipt, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receipt = getItem(position)
        holder.tvNumber.text = receipt.receiptNumber
        holder.tvDevice.text = receipt.deviceName.ifEmpty { "-" }
        holder.tvCustomer.text = receipt.customerName.ifEmpty { "Pelanggan" }
        holder.tvTotal.text = rupiah.format(receipt.total)
        holder.tvDate.text = dateFormat.format(receipt.createdAt)

        holder.itemView.setOnClickListener { onItemClick(receipt) }
        holder.btnDelete.setOnClickListener { onDeleteClick(receipt) }
    }

    class DiffCallback : DiffUtil.ItemCallback<Receipt>() {
        override fun areItemsTheSame(oldItem: Receipt, newItem: Receipt) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Receipt, newItem: Receipt) = oldItem == newItem
    }
}
