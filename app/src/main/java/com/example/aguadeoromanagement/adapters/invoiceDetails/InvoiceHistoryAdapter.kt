package com.example.aguadeoromanagement.adapters.invoiceDetails

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.databinding.LineInvoiceHistoryBinding
import com.example.aguadeoromanagement.models.*
import com.example.aguadeoromanagement.networking.api.createPayment
import com.example.aguadeoromanagement.networking.api.getInvoiceHistory
import com.example.aguadeoromanagement.networking.api.updatePayment
import com.example.aguadeoromanagement.utils.toPrice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class InvoiceHistoryAdapter(
    private val historyList: List<InvoiceHistory>,
    private val invoice: Invoice,
    private val activity: FragmentActivity,
    private val recyclerViewHistory: RecyclerView
) :
    RecyclerView.Adapter<InvoiceHistoryAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LineInvoiceHistoryBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LineInvoiceHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).root
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = historyList[position]
        holder.binding.apply {

            var message = ""
            if (history.entryDate.isNotEmpty()) {
                message += LocalDateTime.parse(history.entryDate, Constants.fromAccessFormatter)
                    .format(
                        Constants.forUsFormatterDateTime
                    )
                message += ": "
            }

            if (history.isPayment) {
                message +=
                    "Payment of ${history.amount} (mode: ${history.paymentMode}) scheduled for ${
                        LocalDate.parse(
                            history.scheduledDate,
                            Constants.fromAccessFormatter
                        ).format(Constants.forUsFormatter)
                    }"
                if (history.executedDate.isNotEmpty()) {
                    message += " and executed on ${
                        LocalDate.parse(
                            history.executedDate,
                            Constants.fromAccessFormatter
                        ).format(Constants.forUsFormatter)
                    }"
                }
            } else {
                message += history.remark
            }
            textViewAmount.text = message
            textViewAmount.setOnClickListener {
                Log.e("hdfhasfhsadhfa", history.isPayment.toString())
                if (history.isPayment){
                    showUpdatePaymentDialog(activity, invoice, history, recyclerViewHistory)
                }
            }
        }
    }
}

fun showUpdatePaymentDialog(activity: FragmentActivity, invoice: Invoice, payment: InvoiceHistory, recyclerViewHistory: RecyclerView) {
    val view = activity.layoutInflater.inflate(
        R.layout.dialog_add_payment,
        null
    )
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("Add Payment")

    val editTextAmount = view.findViewById<EditText>(R.id.amount)
    val editTextRemark = view.findViewById<EditText>(R.id.remark)
    val spinnerPaymentMethod = view.findViewById<Spinner>(R.id.spinnerPaymentMethod)
    val spinnerCheckedBy = view.findViewById<Spinner>(R.id.spinnerApproval)
    val dateP = view.findViewById<DatePicker>(R.id.date)

//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatter = DateTimeFormatter.ISO_DATE
    val formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    var paymentDate: LocalDate? = null

    editTextRemark.setText(payment.remark)


    try {
        paymentDate = LocalDate.parse(payment.scheduledDate, formatter)
    } catch (e: Exception) {
        paymentDate = LocalDate.parse(payment.scheduledDate, formatter2)
    }
    Log.e("created date", paymentDate.toString())
//        Log.e("year", paymentDate.monthValue.toString())
    if (paymentDate != null) {
        dateP.updateDate(
            paymentDate.year,
            paymentDate.monthValue - 1,
            paymentDate.dayOfMonth
        )
    }


    val paymentMethods =
        ManagementApplication.getAppOptionValues().filter { optionValue -> optionValue.type == "SupplierPaymentMethod" }
            .map { optionValue -> optionValue.optionValue }

    val startIndex = paymentMethods.indexOf(payment.paymentMode)


    spinnerPaymentMethod.adapter = ArrayAdapter(
        activity.applicationContext, android.R.layout.simple_spinner_dropdown_item, paymentMethods
    )
    spinnerPaymentMethod.setSelection(startIndex)

    editTextAmount.setText(payment.amount.toPrice().toString())

    val approvals = ManagementApplication.getAppOptionValues().filter { optionValue -> optionValue.type == "Approval" }
        .map { optionValue -> optionValue.optionValue }


    val approvalIndex = approvals.indexOf(payment.checkedBy)
    val approvalAdapter =
        ArrayAdapter(activity.applicationContext, android.R.layout.simple_spinner_item, approvals)
    approvalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


    spinnerCheckedBy.adapter = approvalAdapter
    spinnerCheckedBy.setSelection(approvalIndex)

    builder.setView(view)
    builder.create()
    builder.setCancelable(false)


    builder.setPositiveButton("Update") { dialog, _ ->
        val mode = spinnerPaymentMethod.selectedItem.toString()
        Log.e("mode ", mode)
        val amount = view.findViewById<EditText>(R.id.amount).text.toString()
        val remark = view.findViewById<EditText>(R.id.remark).text.toString()

        val c = Calendar.getInstance()
        c.set(dateP.year, dateP.month, dateP.dayOfMonth)
        val date = c.time
        val dateFormat: DateFormat =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val strDate = "${dateP.month + 1}/${dateP.dayOfMonth}/${dateP.year}"


        val updatedPayment = Payment(
            id = payment.id,
            amountPaid = amount.toDouble(),
            remarks = remark,
            by = mode,
            scheduledDate = strDate,
            invoiceID = invoice.id,
            executed = false,
            checkedBy = spinnerCheckedBy.selectedItem.toString()
        )
        Log.e("updated payment", updatedPayment.toString())

        CoroutineScope(Dispatchers.Main).launch {
            val s = updatePayment(updatedPayment)

            if (s){
                val invoiceHistory = getInvoiceHistory(invoice.id)

                recyclerViewHistory.adapter = InvoiceHistoryAdapter(invoiceHistory, invoice, activity, recyclerViewHistory)
                Toast.makeText(activity, "payment updated", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    activity,
                    "Error updating payment",
                    Toast.LENGTH_SHORT
                ).show()
            }

//            dialog.dismiss()
        }
//        dialog.dismiss()




        return@setPositiveButton
    }
    builder.setNegativeButton("Delete") { dialog, _ ->
//        if (invoiceItems.any { invoiceItem -> invoiceItem.supplierOrderID.isNotEmpty() } || stockHistory.any { it.orderNumber.isNotEmpty() }) {
//            showUpdateStatusDialog()
//        }
        dialog.dismiss()

    }
    builder.setNegativeButton("Cancel") { dialog, _ ->
//        if (invoiceItems.any { invoiceItem -> invoiceItem.supplierOrderID.isNotEmpty() } || stockHistory.any { it.orderNumber.isNotEmpty() }) {
//            showUpdateStatusDialog()
//        }
        dialog.dismiss()

    }


    builder.show()

}