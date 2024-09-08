package com.example.aguadeoromanagement.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.Payment
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CashReportAdapter(
    private val context: Context,
    private var dataSet: MutableList<Invoice>,
    private var paymentsData: MutableList<Payment>
) :
    RecyclerView.Adapter<CashReportAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val invoiceDate: TextView
        val invoiceNumber: TextView
        val invoiceAmount: TextView
        val receivedDate: TextView
        val paid: TextView
        val remaining: TextView

        init {
            invoiceDate = view.findViewById(R.id.invoiceDate)
            invoiceNumber = view.findViewById(R.id.invoiceNumber)
            invoiceAmount = view.findViewById(R.id.amount)
            receivedDate = view.findViewById(R.id.receivedDate)
            paid = view.findViewById(R.id.paid)
            remaining = view.findViewById(R.id.remaining)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.line_report_cash, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val invoice = dataSet[position]
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        var receivedDateDate = ""
        if (invoice.receivedDate.isNotEmpty()) {
            receivedDateDate = LocalDate.parse(invoice.receivedDate, formatter)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
        var invoiceDateDate = ""
        if (invoice.invoiceDate.isNotEmpty()) {
            invoiceDateDate = LocalDate.parse(invoice.invoiceDate, formatter)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
        var paidTotal = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_UP)
        paymentsData.forEach { payment ->
            if (payment.invoiceID == invoice.id) {
                if (payment.executed) {
                    paidTotal =
                        paidTotal.add(
                            BigDecimal(payment.amountPaid).setScale(
                                2,
                                BigDecimal.ROUND_HALF_UP
                            )
                        )
                }
            }
        }
        val remainingTotal = BigDecimal(invoice.amount).setScale(2, BigDecimal.ROUND_HALF_UP)
            .subtract(paidTotal)
        with(holder) {
            invoiceDate.text = invoiceDateDate
            invoiceNumber.text = invoice.invoiceNumber
            invoiceAmount.text =
                "${BigDecimal(invoice.amount).setScale(2, BigDecimal.ROUND_HALF_UP)}CHF"
            receivedDate.text = receivedDateDate
            paid.text = "${paidTotal}CHF"
            remaining.text = "${remainingTotal}CHF"
        }

    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun setData(data: MutableList<Invoice>, payments: MutableList<Payment>) {
        dataSet.clear()
        dataSet.addAll(data)
        dataSet.sortByDescending { it.invoiceDate }
        paymentsData.clear()
        paymentsData.addAll(payments)
        notifyDataSetChanged()
    }
}