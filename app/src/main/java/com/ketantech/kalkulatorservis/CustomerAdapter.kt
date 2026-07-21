package com.ketantech.kalkulatorservis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ketantech.kalkulatorservis.data.Customer
import java.text.SimpleDateFormat
import java.util.Locale

class CustomerAdapter(
    private val onItemClick: (Customer) -> Unit,
    private val onDeleteClick: (Customer) -> Unit
) : ListAdapter<CustomerAdapter.CustomerItem, CustomerAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale("in", "ID"))

    /** Customer + jumlah servis (dihitung dari tabel nota). */
    data class CustomerItem(val customer: Customer, val serviceCount: Int)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDetail: TextView = view.findViewById(R.id.tvDetail)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val ctx = holder.itemView.context
        holder.tvName.text = item.customer.name

        val count = ctx.getString(R.string.customer_service_count, item.serviceCount)
        val last = ctx.getString(R.string.customer_last_service, dateFormat.format(item.customer.lastServiceAt))
        holder.tvDetail.text = "$count • $last"

        holder.itemView.setOnClickListener { onItemClick(item.customer) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item.customer) }
    }

    class DiffCallback : DiffUtil.ItemCallback<CustomerItem>() {
        override fun areItemsTheSame(oldItem: CustomerItem, newItem: CustomerItem) =
            oldItem.customer.id == newItem.customer.id
        override fun areContentsTheSame(oldItem: CustomerItem, newItem: CustomerItem) =
            oldItem == newItem
    }
}
