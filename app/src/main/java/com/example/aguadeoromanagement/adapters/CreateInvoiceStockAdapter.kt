package com.example.aguadeoromanagement.adapters

import android.app.AlertDialog
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.databinding.LineCreateInvoiceBinding
import com.example.aguadeoromanagement.databinding.LineCreateInvoiceStockHistoryBinding
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.models.SupplierOrderMain
import com.example.aguadeoromanagement.utils.toPrice


class CreateInvoiceStockAdapter(
    private var stockHistory: List<StockHistory>,
    val stockHistoryByOrders: MutableMap<String, MutableList<StockHistory>>,
    val toSelectHistory: MutableSet<Int>,
    private val activity: FragmentActivity,
    private val context: Context
) : RecyclerView.Adapter<CreateInvoiceStockAdapter.ViewHolder>() {

    class ViewHolder(
        val binding: LineCreateInvoiceStockHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LineCreateInvoiceStockHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = stockHistoryByOrders.keys.toList()[position]
        val stockByOrders = stockHistoryByOrders[order]!!
        val cost =
            stockByOrders.fold(0.0) { acc, stock -> acc + (stock.cost * stock.quantity) }.toPrice()
        holder.binding.apply {
            orderNumber.text = order
//            textViewQuantityStock.text = stock.quantity.toPrice().toString()
//            textViewProductID.text = stock.productId.toString()

            textViewCostStock.text = cost.toString()

            checkBoxAddToInvoice.isChecked = toSelectHistory.contains(position)


            checkBoxAddToInvoice.setOnClickListener { buttonView ->

                if (checkBoxAddToInvoice.isChecked) {
                    stockByOrders.forEach {
                        toSelectHistory.add(stockHistory.indexOf(it))
                    }
                } else {
                    stockByOrders.forEach {
                        toSelectHistory.remove(stockHistory.indexOf(it))
                    }
                }
//                Log.e("all stock", toSelectHistory.toString())
            }
        }
    }

    override fun getItemCount() = stockHistoryByOrders.keys.size


}