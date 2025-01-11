package com.example.aguadeoromanagement.networking.api

import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.Payment
import com.example.aguadeoromanagement.networking.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

suspend fun getPayments(
    daysBeforeNow: Int, future: Boolean, where: String = "", noContactName: Boolean = false
): Pair<List<Payment>, Boolean> = withContext(Dispatchers.IO) {
    val now = LocalDate.now()
    val dateLimit = now.minusDays(daysBeforeNow.toLong()).format(Constants.accessFormater)
    var whereClause = where
    if (where.isEmpty()) {
        whereClause =
            if (future) "WHERE ScheduledDate >= #$dateLimit#" else "WHERE ScheduledDate >= #$dateLimit# AND ScheduledDate <= #${
                now.format(Constants.accessFormater)
            }#"
    }

    whereClause += " AND IsPayment = True"

//"select Supplier.*, Contacts.ID as contactId from Contacts inner join Supplier on Contacts.SupplierID = Supplier.ID WHERE Contacts.ID = $id"
    var q = Query(
        "select Contacts.Contacts, InvoiceHistory.*, Invoice.InvNumber, Invoice.Curr, Invoice.Remain, Invoice.Amount FROM "
                + "(InvoiceHistory INNER JOIN Invoice ON InvoiceHistory.InvoiceID = Invoice.ID) " + "INNER JOIN Contacts ON Contacts.ID = Invoice.Supplier "
                + "$whereClause " + "ORDER BY ScheduledDate DESC"
    )

    if (noContactName) {
        q = Query(
            "select InvoiceHistory.*, Invoice.InvNumber, Invoice.Curr, Invoice.Remain, Invoice.Amount FROM "
                    + "(InvoiceHistory INNER JOIN Invoice ON InvoiceHistory.InvoiceID = Invoice.ID) " + "$whereClause " + "ORDER BY ScheduledDate DESC"
        )
    }
//            WHERE PaymentDate >= #$dateLimit#
    val s = q.execute(Constants.url)
    val res = mutableListOf<Payment>()
    q.res.forEach { payment ->
        try {
            res.add(
                Payment(
                    payment["ID"]!!.toInt(),
                    payment["InvoiceID"]!!.toInt(),
                    payment["AmountPaid"]?.toDoubleOrNull() ?: 0.0,
                    scheduledDate = if (!payment["ScheduledDate"].isNullOrEmpty()) LocalDate.parse(
                        payment["ScheduledDate"]!!, Constants.fromAccessFormatter
                    ).format(Constants.forUsFormatter) else "",
                    payment["PaymentBy"] ?: "",
                    payment["Remarks"] ?: "",
                    executed = payment["Executed"] == "1",
                    executedDate = if (!payment["ExecutedDate"].isNullOrEmpty()) LocalDate.parse(
                        payment["ExecutedDate"]!!, Constants.fromAccessFormatter
                    ).format(Constants.forUsFormatter) else "",
                    invoiceNumber = payment["InvNumber"] ?: "",
                    contactName = payment["Contacts"] ?: "",
                    currency = payment["Curr"] ?: "",
                    paymentMode = payment["PaymentBy"] ?: "",
                    checkedBy = payment["CheckedBy"] ?: "",
                    remainingOfInvoice = payment["Remain"]?.toDoubleOrNull() ?: 0.0,
                    totalOfInvoice = payment["Amount"]?.toDoubleOrNull() ?: 0.0,
                )
            )
        } catch (e: Error) {
            e.printStackTrace()
        }

    }
    return@withContext Pair(res, s)
}

suspend fun updatePayment(payment: Payment): Boolean{
    return withContext(Dispatchers.IO){


        val query =
            "update InvoiceHistory SET AmountPaid = ${payment.amountPaid}, ScheduledDate = #${payment.scheduledDate}#, PaymentBy = '${payment.by}', Remarks = '${payment.remarks}', CheckedBy = '${payment.checkedBy}' where ID = ${payment.id}"
        val q = Query(query)
        val s = q.execute(Constants.url)
        return@withContext s
    }
}