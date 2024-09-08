package com.example.aguadeoromanagement.adapters.invoiceDetails

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.databinding.LineInOutsBinding
import com.example.aguadeoromanagement.databinding.LineInvoiceItemsBinding
import com.example.aguadeoromanagement.dialogs.AddItemDialog
import com.example.aguadeoromanagement.fragments.InvoiceDetails
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.InvoiceItem
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.models.SupplierOrderMain
import com.example.aguadeoromanagement.networking.APIFetcher
import com.example.aguadeoromanagement.utils.roundWithNDecimal
import com.example.aguadeoromanagement.utils.toPrice

class InOutsAdapter(
    private val stockHistories: List<StockHistory>,
    private val activity: FragmentActivity? = null,
    private val invoice: Invoice? = null,
    private val invoiceDetails: InvoiceDetails? = null,
    private val onClick: (StockHistory) -> Unit = {  }
) :
    RecyclerView.Adapter<InOutsAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LineInOutsBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LineInOutsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).root
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return stockHistories.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stock = stockHistories[position]
        holder.binding.apply {
            textViewId.text = stock.productId.toString()
            textViewCode.text = stock.shortCode
            textViewQuantity.text = "${stock.quantity} - ${(stock.quantity*stock.cost).toPrice()}CHF"

            val type = if (stock.type == 1) "IN" else if (stock.type == 2) "OUT" else "N/A"

            textViewInOutType.text = type

            if (stock.id == 0){
                textViewId.setTextColor(Color.RED)
                textViewCode.setTextColor(Color.RED)
                textViewQuantity.setTextColor(Color.RED)

            }else{
                buttonDeleteStock.visibility = View.GONE
            }

            if (stock.shortCode.isEmpty()){
                if (activity != null) {
                    APIFetcher(activity).getProduct(stock.productId){product, s->
                        if (s){
                            stock.shortCode = product!!.productCode
                            textViewCode.text = stock.shortCode
                        }
                    }
                }
            }

            buttonDeleteStock.setOnClickListener {
                val toDelete = invoiceDetails?.invoiceItems?.find { invoiceItem -> invoiceItem.uuid == stock.uuid }
                invoiceDetails?.invoiceItems?.remove(toDelete)
                invoiceDetails?.update()
            }


        }

        holder.binding.root.setOnClickListener {
//            val dialog = AddItemDialog(stock = stock)
//            if (invoiceDetails != null){
//                dialog.showDialog(activity!!, invoice!!, mutableListOf(), invoiceDetails = invoiceDetails )
//
//            }
            Log.e("out", stock.toString())
            onClick(stock)
        }


    }

}