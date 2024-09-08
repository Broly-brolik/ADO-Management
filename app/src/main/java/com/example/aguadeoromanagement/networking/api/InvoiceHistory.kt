package com.example.aguadeoromanagement.networking.api

import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.InvoiceHistory
import com.example.aguadeoromanagement.models.Payment
import com.example.aguadeoromanagement.networking.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

suspend fun insertInHistory(invoiceID: Int, remark: String): Int {
    return withContext(Dispatchers.IO) {

        val now = LocalDateTime.now().format(Constants.accessFormaterDateTime)
        val query = "insert into InvoiceHistory " +
                "(InvoiceID, Remarks, EntryDate, IsPayment)" +
                " values (${invoiceID},'${remark}', #${now}#, False)"
        val q = Query(query)
        return@withContext q.execute(Constants.url, true)
    }
}

suspend fun createPayment(payment: Payment): Int {
    return withContext(Dispatchers.IO) {
        val now = LocalDateTime.now().format(Constants.accessFormaterDateTime)
        val query = "insert into InvoiceHistory " +
                "(InvoiceID, AmountPaid, ScheduledDate, PaymentBy, Remarks, Executed, CheckedBy, EntryDate)" +
                " values (${payment.invoiceID},'${payment.amountPaid}',#${payment.scheduledDate}#,'${payment.by}','${payment.remarks}',${payment.executed}, '${payment.checkedBy}', #${now}#)"
        val q = Query(query)
        return@withContext q.execute(Constants.url, true)

    }
}

suspend fun getInvoiceHistory(invoiceId: Int): List<InvoiceHistory> {
    return withContext(Dispatchers.IO) {
        val q = Query(
            "select * from InvoiceHistory where InvoiceID = $invoiceId ORDER BY EntryDate DESC"
        )
        val success = q.execute(Constants.url, "Supplier invoices")
        val res = mutableListOf<InvoiceHistory>()
        q.res.forEach { item ->
            res.add(
                InvoiceHistory(
                    id = item["ID"]!!.toInt(),
                    invoiceId = item["InvoiceID"]!!.toInt(),
                    amount = item["AmountPaid"]?.toDoubleOrNull() ?: 0.0,
                    paymentMode = item["PaymentBy"] ?: "",
                    scheduledDate = item["ScheduledDate"] ?: "",
                    remark = item["Remarks"] ?: "",
                    checkedBy = item["CheckedBy"] ?: "",
                    executedDate = item["ExecutedDate"] ?: "",
                    executed = item["Executed"] == "1",
                    entryDate = item["EntryDate"] ?: "",
                    isPayment = item["IsPayment"] == "1"
                )
            )
        }
        return@withContext res
    }
}

suspend fun updateSinglePayment(
    payment: Payment,
    date: String,
    checkedBy: String,
    paymentMode: String
): Boolean {
    return withContext(Dispatchers.IO) {
        val now = LocalDateTime.now().format(Constants.accessFormaterDateTime)

        var q =
            Query("update InvoiceHistory set ExecutedDate = #${date}#, CheckedBy = '${checkedBy}', PaymentBy = '${paymentMode}', Executed=TRUE, EntryDate = #${now}# where ID = ${payment.id}")
        val success = q.execute(Constants.url)

        if (success) {
            val newRemaining = payment.remainingOfInvoice - payment.amountPaid
            q =
                Query("update Invoice set Remain = $newRemaining WHERE InvNumber = '${payment.invoiceNumber}'")
            return@withContext q.execute(Constants.url)
        }
        return@withContext false
    }
}