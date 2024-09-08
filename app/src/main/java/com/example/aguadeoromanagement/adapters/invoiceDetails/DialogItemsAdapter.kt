package com.example.aguadeoromanagement.adapters.invoiceDetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.databinding.LineInvoiceProductsBinding
import com.example.aguadeoromanagement.models.StockHistory

class DialogItemsAdapter(private val data: List<StockHistory>) :
    RecyclerView.Adapter<DialogItemsAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LineInvoiceProductsBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LineInvoiceProductsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).root
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val out = data[position]
        holder.binding.apply {
            productId.text = out.productId.toString()
            productCode.text = out.shortCode
            flow.text = out.flow
            quantity.text = out.quantity.toString()
            price.text = out.cost.toString()
            process.text = out.process
            vat.text = "n/a"
            amount.text = "n/a"

        }
    }
}