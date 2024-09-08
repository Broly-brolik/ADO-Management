package com.example.aguadeoromanagement.adapters.suppliersFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.databinding.LineContactHistoryBinding
import com.example.aguadeoromanagement.models.ContactHistory

class SupplierHistoryAdapter(
    private val historyList: List<ContactHistory>
) : RecyclerView.Adapter<SupplierHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LineContactHistoryBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.line_contact_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.contactHistoryDate.text = historyList[position].date
        holder.binding.contactHistoryRemark.text = historyList[position].remark
    }
}