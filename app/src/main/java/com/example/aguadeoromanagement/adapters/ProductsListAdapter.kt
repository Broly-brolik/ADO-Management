package com.example.aguadeoromanagement.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.databinding.LineInvoiceProductsBinding
import com.example.aguadeoromanagement.models.StockHistory

class ProductsListAdapter(private val products: List<StockHistory>) :
    RecyclerView.Adapter<ProductsListAdapter.ViewHolder>() {
    class ViewHolder(
        val binding: LineInvoiceProductsBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LineInvoiceProductsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        var quant = product.quantity
        if (product.type == 2) {
            quant = product.quantity * -1
        }
        holder.binding.apply {
            productId.text = product.productId.toString()
            productCode.text = product.shortCode
            flow.text = product.flow
            process.text = product.process
            quantity.text = quant.toString()
            price.text = product.cost.toString()
            amount.text = (product.cost * product.quantity).toString()
            vat.text = "n/a"
        }
    }

}