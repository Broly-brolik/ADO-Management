package com.example.aguadeoromanagement.dialogs

import android.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.adapters.invoiceDetails.DialogItemsAdapter
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.models.SupplierOrderMain

//class InvoiceItemsDialog {
//    fun showDialog(
//        context: FragmentActivity,
//        data: List<StockHistory>,
//        invoice: Invoice,
//        supplierOrderMain: SupplierOrderMain
//    ) {
//        val builder = AlertDialog.Builder(context)
//        val outs: MutableList<StockHistory> = mutableListOf()
//        val ins: MutableList<StockHistory> = mutableListOf()
//        data.forEach {
//            if (it.type == 2) {
//                outs.add(it)
//            } else {
//                ins.add(it)
//            }
//        }
//
//        builder.setTitle("Invoice items")
//        val view = context.layoutInflater.inflate(
//            R.layout.dialog_invoice_items,
//            null
//        )
//        val recyclerView = view.findViewById<RecyclerView>(R.id.outList)
//        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
//        recyclerView.addItemDecoration(
//            androidx.recyclerview.widget.DividerItemDecoration(
//                recyclerView.context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
//            )
//        )
//        recyclerView.adapter = DialogItemsAdapter(outs)
//
//        val recyclerView2 = view.findViewById<RecyclerView>(R.id.inList)
//        recyclerView2.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
//        recyclerView2.addItemDecoration(
//            androidx.recyclerview.widget.DividerItemDecoration(
//                recyclerView.context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
//            )
//        )
//        recyclerView2.adapter = DialogItemsAdapter(ins)
//
//        builder.setView(view)
//        builder.setPositiveButton("Add item") { dialog, _ ->
//            context.runOnUiThread {
//                AddItemDialog().showDialog(context, invoice, supplierOrderMain.supplierOrderNumber)
//            }
//        }
//        builder.setNegativeButton("Cancel") { dialog, _ ->
//            dialog.dismiss()
//        }
//        builder.create().show()
//    }
//
//}