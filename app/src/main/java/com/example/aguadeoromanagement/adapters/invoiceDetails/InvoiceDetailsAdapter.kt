package com.example.aguadeoromanagement.adapters.invoiceDetails

import android.R
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.databinding.LineInvoiceItemsBinding
import com.example.aguadeoromanagement.dialogs.AddItemDialog
import com.example.aguadeoromanagement.fragments.InvoiceDetails
import com.example.aguadeoromanagement.models.*
import com.example.aguadeoromanagement.networking.APIFetcher
import com.example.aguadeoromanagement.utils.roundWithNDecimal
import com.example.aguadeoromanagement.utils.toPrice

fun computeDistinctOrders(
    invoiceItems: List<InvoiceItem>,
    stockHistory: List<StockHistory>
): List<String> {
    var distinctOrders: MutableSet<String> =
        invoiceItems.distinctBy { invoiceItem -> invoiceItem.supplierOrderID }
            .map { it.supplierOrderID }.toMutableSet()
    distinctOrders.addAll(stockHistory.distinctBy { stock -> stock.orderNumber }
        .map { it.orderNumber }.toSet())
    return distinctOrders.toList()
}

class InvoiceDetailsAdapter(
    private var invoiceItems: List<InvoiceItem>,
    private val stockHistory: List<StockHistory>,
    private val supplierName: String,
    private val activity: FragmentActivity,
    private val invoice: Invoice,
    private val toShow: MutableMap<String, Boolean> = mutableMapOf(),
    private val optionValues: List<OptionValue>,
    private val invoiceDetails: InvoiceDetails? = null
) :
    RecyclerView.Adapter<InvoiceDetailsAdapter.ViewHolder>() {

    var distinctOrders = computeDistinctOrders(invoiceItems, stockHistory)


    val supplierOrderStatus =
        optionValues.filter { optionValue -> optionValue.type == "SupplierOrderStatus" }
            .map { optionValue -> optionValue.optionValue }.toList().sorted()


    fun updateItems(items: List<InvoiceItem>) {
        invoiceItems = items
        distinctOrders = computeDistinctOrders(invoiceItems, stockHistory)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LineInvoiceItemsBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LineInvoiceItemsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).root
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return distinctOrders.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val supplierOrder = distinctOrders[position]


        val filteredItems =
            invoiceItems.filter { item -> item.supplierOrderID == supplierOrder && item.product == 0 }

        val filteredStock = stockHistory.filter { it.orderNumber == supplierOrder }


        var a =
            filteredItems.fold(0.0) { acc, invoiceItem -> acc + (invoiceItem.unitPrice * invoiceItem.quantity * (1 + invoiceItem.vat / 100)) }

        a += filteredStock.fold(0.0) { acc, stock -> acc + (stock.cost * stock.quantity)}


        val apiFetcher = APIFetcher(activity)
//        apiFetcher.getStockHistoryForOrder(1, supplierId) { stock, s ->
//        val stock = invoiceDetails?.stockHistoryForOrders?.get(supplierOrder) ?: listOf()
//            Log.e("Stock", stock.toString())
        holder.binding.apply {
            supplierOrderId.text = supplierOrder
            amount.text = a.toPrice().toString()
//            instruction.text = invoiceItem.instruction


            val recyclerViewStockHistory = recyclerViewStockHistory
            recyclerViewStockHistory.layoutManager = LinearLayoutManager(
                activity.applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )
            recyclerViewStockHistory.addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    recyclerViewStockHistory.context,
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
            )

            val stockAndItems: MutableList<StockHistory> = mutableListOf()

//            if (supplierOrder.isNotEmpty()) { //car si c est empty ca va renvoyer n importe quoi
//                stockAndItems.addAll(stock)
//            }


            stockAndItems.addAll(filteredStock)


            recyclerViewStockHistory.adapter =
                InOutsAdapter(stockAndItems, activity, invoice, invoiceDetails = invoiceDetails)
            Log.e("stock and items", stockAndItems.toString())


            val recyclerViewSingleItems = recyclerViewSingleItem
            recyclerViewSingleItems.layoutManager = LinearLayoutManager(
                activity.applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )
            recyclerViewSingleItems.addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    recyclerViewSingleItems.context,
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
            )
            recyclerViewSingleItems.adapter = SingleItemsAdapter(
                filteredItems,
                activity,
                invoice,
                invoiceDetails = invoiceDetails
            )
//            instruction.text = supplierOrder.instruction

            layoutExtraInfo.visibility = View.GONE
            toShow.putIfAbsent(supplierOrder, false)

            val collapseBtn = buttonCollapseSingleItems

            if (toShow[supplierOrder]!!) {
                layoutExtraInfo.visibility = View.VISIBLE
                collapseBtn.text = "-"
            } else {
                layoutExtraInfo.visibility = View.GONE
                collapseBtn.text = "+"
            }


            collapseBtn.setOnClickListener {
                if (layoutExtraInfo.visibility == View.VISIBLE) {
                    layoutExtraInfo.visibility = View.GONE
                    collapseBtn.text = "+"
                    toShow[supplierOrder] = false
                } else if (layoutExtraInfo.visibility == View.GONE) {
                    layoutExtraInfo.visibility = View.VISIBLE
                    collapseBtn.text = "-"
                    toShow[supplierOrder] = true
                }
            }
        }


    }


}