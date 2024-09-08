package com.example.aguadeoromanagement.adapters.suppliersFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.databinding.LineInvoicesHistoryBinding
import com.example.aguadeoromanagement.fragments.SuppliersManagementDirections
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.utils.toPrice
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class SuppliersInvoicesHistory(private val invoices: List<Invoice>, private val contactName: String) :
    RecyclerView.Adapter<SuppliersInvoicesHistory.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LineInvoicesHistoryBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.line_invoices_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return invoices.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var receivedDateString = ""
        var invoiceDateString = ""
        var receivedDate: LocalDate? = null;
        var invoiceDate: LocalDate? = null;
        var dueDate: LocalDate? = null;
        var dueDays = "N/A";
        val invoice = invoices[position]
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        if (invoice.receivedDate.isNotEmpty()) {
            receivedDate = LocalDate.parse(invoice.receivedDate, formatter)
            receivedDateString =
                receivedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }
        if (invoice.invoiceDate.isNotEmpty()) {
            invoiceDate = LocalDate.parse(invoice.invoiceDate, formatter)
            invoiceDateString =
                invoiceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }

        if (invoice.dueOn.isNotEmpty()){
//            Log.e("due", invoice.dueOn)
            dueDate = LocalDate.parse(invoice.dueOn, formatter)
//            Log.e("due", dueDate.toString())

        }


        if (dueDate !=null){
            val now = LocalDate.now()
//            Log.e("now", now.toString())
            val period = Period.between(now, dueDate);
            dueDays = "${period.days} (${dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))})"
        }


        holder.binding.invoiceNumber.text = invoice.invoiceNumber
        holder.binding.invoiceDate.text = invoiceDateString
        holder.binding.receivedDate.text = receivedDateString
        holder.binding.remain.text = invoice.remain.toPrice().toString()
        holder.binding.status.text = invoice.status
        holder.binding.daysToPay.text = dueDays
        holder.binding.root.setOnClickListener {
            val action =
                SuppliersManagementDirections.actionSuppliersManagementToInvoiceDetails(
                    invoices[position],
                    contactName,
                    emptyArray()
                )
//                val action =
//                SuppliersManagementDirections.actionSuppliersManagementToInvoiceDetailsV2(
//                    invoices[position],
//                    contactName
//                )
            findNavController(it).navigate(action)
        }
    }
}