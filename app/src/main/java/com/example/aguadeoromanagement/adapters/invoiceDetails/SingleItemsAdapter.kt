package com.example.aguadeoromanagement.adapters.invoiceDetails

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.databinding.LineInvoiceItemsBinding
import com.example.aguadeoromanagement.databinding.LineInvoiceSingleItemsBinding
import com.example.aguadeoromanagement.dialogs.AddItemDialog
import com.example.aguadeoromanagement.fragments.InvoiceDetails
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.InvoiceItem
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.networking.APIFetcher
import com.example.aguadeoromanagement.utils.roundWithNDecimal
import com.example.aguadeoromanagement.utils.toPrice

class SingleItemsAdapter(
    private val invoiceItems: List<InvoiceItem>,
    private val activity: FragmentActivity,
    private val invoice: Invoice,
    private val invoiceDetails: InvoiceDetails? = null
) : RecyclerView.Adapter<SingleItemsAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LineInvoiceSingleItemsBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LineInvoiceSingleItemsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).root
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = invoiceItems[position]
//        Log.e("ITEM", item.toString())
        holder.binding.apply {
            singleItemDetail.text = item.detail
            singleItemPrice.text = (item.quantity * item.unitPrice).toPrice().toString()

            if (item.id == 0){
//                singleItemPrice.setTextColor(Color.RED)
//                singleItemDetail.setTextColor(Color.RED)
            }else{
                buttonDeleteItem.visibility = View.GONE
            }

            buttonDeleteItem.setOnClickListener {
                invoiceDetails?.invoiceItems?.remove(item)
                invoiceDetails?.update()
            }

        }
        holder.binding.root.setOnClickListener {
//            Log.e("item", item.toString())
            val dialog = AddItemDialog(item)
            dialog.showDialog(activity, invoice, mutableListOf(), invoiceDetails = invoiceDetails)

        }
    }

    override fun getItemCount(): Int {
        return invoiceItems.size
    }
}