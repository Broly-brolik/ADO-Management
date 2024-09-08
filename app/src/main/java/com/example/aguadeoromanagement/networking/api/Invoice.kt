package com.example.aguadeoromanagement.networking.api

import android.util.Log
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.networking.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

suspend fun updateInvoice(invoice: Invoice): Boolean {
    return withContext(Dispatchers.IO) {
        var createdDateToInsert = ""
        if (invoice.createdDate.isNotEmpty()) {
            createdDateToInsert = try {
                LocalDate.parse(invoice.createdDate, Constants.forUsFormatter)
                    .format(Constants.accessFormater)
            } catch (e: java.lang.Exception) {
                invoice.createdDate
            }
        }

        val dueOnToInsert = invoice.dueOn

        val q =
            Query(
                "update Invoice set " +
                        "Supplier = ${invoice.supplierId}, InvoiceDate = #${invoice.invoiceDate}#," +
                        " InvNumber = '${invoice.invoiceNumber}', Amount = ${invoice.amount}," +
                        " ReceivedDate = #${invoice.receivedDate}#, Remark = '${invoice.remark}'," +
                        " DueOn = #${dueOnToInsert}#, Paid = ${invoice.paid}, Remain = ${invoice.remain}," +
                        " Status = '${invoice.status}', Discount = ${invoice.discountAmount}," +
                        " Curr = '${invoice.currency}', CreatedDate = #$createdDateToInsert#," +
                        " RegisteredBy = '${invoice.registeredBy}' WHERE ID = ${invoice.id} "
            )
        return@withContext q.execute(Constants.url)
    }
}

suspend fun createInvoice(invoice: Invoice): Int {
    return withContext(Dispatchers.IO) {

        var createdDateToInsert = ""
        if (invoice.createdDate.isNotEmpty()) {
            createdDateToInsert = LocalDate.parse(invoice.createdDate, Constants.forUsFormatter)
                .format(Constants.accessFormater)
        }

//        val dueOnToInsert = LocalDate.parse(invoice.dueOn, DateTimeFormatter.ISO_DATE)
//            .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        val dueOnToInsert = invoice.dueOn

        val q =
            Query(
                "insert into Invoice " +
                        "(Supplier, InvoiceDate, InvNumber, Amount, ReceivedDate, Remark, DueOn, Paid, Remain, Status, Discount, Curr, CreatedDate, RegisteredBy)" +
                        " values (${invoice.supplierId}, #${invoice.invoiceDate}#, '${invoice.invoiceNumber}', ${invoice.amount}, #${invoice.receivedDate}#, '${invoice.remark}'," +
                        " #${dueOnToInsert}#, ${invoice.paid}, ${invoice.remain}, '${invoice.status}', ${invoice.discountAmount}, '${invoice.currency}', #$createdDateToInsert#," +
                        " '${invoice.registeredBy}')"
            )
        return@withContext q.execute(Constants.url, true)
    }
}


suspend fun linkStockHistoryToInvoice(invID: String, stockHistory: List<Int>):Boolean {
    return withContext(Dispatchers.IO) {

        Log.e("stock to link", stockHistory.toString())
        if (stockHistory.isEmpty()){
            return@withContext false
        }
        var ids_string = ""
        stockHistory.forEach { ids_string += ("$it, ") }
        ids_string = ids_string.removeRange(ids_string.length - 2, ids_string.length)

        val q =
            Query("update StockHistory1 set InvoiceID = $invID where ID in ($ids_string)")
        val s = q.execute(Constants.url)
        return@withContext s
    }

}

suspend fun closeInvoices(invoice_ids: List<Int>): Boolean {
    return withContext(Dispatchers.IO) {
        var ids_string = ""
        invoice_ids.forEach { ids_string += ("$it, ") }
        ids_string = ids_string.removeRange(ids_string.length - 2, ids_string.length)

        val q =
            Query("update Invoice set Status = 'Closed' where ID in ($ids_string)")
        return@withContext q.execute(Constants.url)
    }
}


suspend fun updateInvoiceStatus(invoice: Invoice): Boolean {
    return withContext(Dispatchers.Main) {
        val q = Query("update Invoice set Status = '${invoice.status}' where ID = ${invoice.id}")
        return@withContext q.execute(Constants.url)
    }
}

suspend fun updateInvoiceCheck(invoices: List<Invoice>): Boolean {
    return withContext(Dispatchers.Main) {
        var ids_string = ""
        invoices.forEach { ids_string += ("${it.id}, ") }
        ids_string = ids_string.removeRange(ids_string.length - 2, ids_string.length)
        val q =
            Query("update Invoice set Validated = True where ID in ($ids_string)")
        return@withContext q.execute(Constants.url)
    }
}
