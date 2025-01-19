package com.example.aguadeoromanagement.networking

import android.util.Log
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime


suspend fun getSuppliers(): List<Contact> {
    return withContext(Dispatchers.IO) {

        val q = Query("select * from Supplier order by SupplierName ASC")
        val s = q.execute(Constants.url)
        if (!s) {
            return@withContext listOf<Contact>()
        }
        val res = mutableListOf<Contact>()
        q.res.forEach { contact ->
            val c = Contact()
            c.id = contact["ID"]!!.toInt()
            c.name = contact["SupplierName"] ?: ""
            c.email = contact["SupplierEmail"] ?: ""
            c.resp1 = contact["Responsable"] ?: ""
            c.remark = contact["Remark"] ?: ""
            c.address = contact["SupplierAddress"] ?: ""
            c.phone = contact["SupplierTel"] ?: ""
            c.email2 = contact["SupplierEmail2"] ?: ""
            c.type = contact["SupplierType"] ?: ""
            c.active = contact["Active"]?.toBooleanStrictOrNull() ?: false
            res.add(c)
        }
        return@withContext res
    }
}

suspend fun getContacts(): Map<String, String> {
    return withContext(Dispatchers.IO) {

        val q = Query("select * from Contacts")
        val s = q.execute(Constants.url)
        if (!s) {
            return@withContext emptyMap<String, String>()
        }
        val res = mutableMapOf<String, String>()
        q.res.forEach { contact ->
            res[contact["ID"]!!] = contact["Contacts"]!!
        }
        Log.e("contacts", res.toString())
        return@withContext res
    }
}


suspend fun getSupplierInvoices(
    contact: Contact, closed: Boolean = false
): Pair<List<Invoice>, Boolean> {
    return withContext(Dispatchers.IO) {
        var filter = "(Status = 'Open' or Status = 'Investigate')"
        if (closed) {
            filter = "(Status = 'Closed')"
        }

        val q =
            Query("select * from Invoice WHERE Supplier = ${contact.idToInsert} and $filter order by InvoiceDate ASC")
        val s = q.execute(Constants.url)
        val res = mutableListOf<Invoice>()
        q.res.forEach { invoice ->
            res.add(
                Invoice(
                    id = invoice["ID"]!!.toInt(),
                    supplierId = contact.idToInsert,
                    invoiceDate = invoice["InvoiceDate"]!!,
                    invoiceNumber = invoice["InvNumber"]!!,
                    amount = invoice["Amount"]?.toDoubleOrNull() ?: 0.0,
                    receivedDate = invoice["ReceivedDate"]!!,
                    remark = invoice["Remark"]!!,
                    items = mutableListOf(),
                    dueOn = invoice["DueOn"] ?: "",
                    paid = invoice["Paid"]?.toDoubleOrNull() ?: 0.0,
                    remain = invoice["Remain"]?.toDoubleOrNull() ?: 0.0,
                    status = invoice["Status"] ?: "",
                    discountPercentage = invoice["Discount"]?.toDoubleOrNull() ?: 0.0,
                    registeredBy = invoice["RegisteredBy"] ?: "",
                    createdDate = invoice["CreatedDate"] ?: "",
                    currency = invoice["Curr"] ?: "",
                )
            )
        }
        return@withContext Pair(res.toList(), s)
    }
}

suspend fun getPaymentsListWithFilter(
    f: String = "",
    daysBeforeNow: Int = 6,
    selectedDate: LocalDate = LocalDate.now()
): List<Payment> {
    return withContext(Dispatchers.IO) {
        var filter = f
        if (filter.isEmpty()) {
            val dayStop = selectedDate.format(Constants.accessFormater)
            val dayStart = selectedDate.minusDays(daysBeforeNow.toLong()).format(Constants.accessFormater)
            filter = "WHERE ScheduledDate <= #$dayStop# and ScheduledDate >= #$dayStart#"
        }
        filter += " AND IsPayment = True"
        val q = Query("select Contacts.Contacts, InvoiceHistory.*, Invoice.InvNumber, Invoice.Curr, Invoice.Remain, Invoice.Amount FROM "
                + "(InvoiceHistory INNER JOIN Invoice ON InvoiceHistory.InvoiceID = Invoice.ID) " + "INNER JOIN Contacts ON Contacts.ID = Invoice.Supplier "
                + "$filter " + "ORDER BY ScheduledDate DESC")

        val s = q.execute(Constants.url)
        val res = mutableListOf<Payment>()
        q.res.forEach { payment ->
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
        }
        return@withContext res.toList()
    }
}

suspend fun getInvoicesWithFilter(
    f: String = "",
    daysBeforeNow: Int = 6,
    selectedDate: LocalDate = LocalDate.now()
): List<Invoice> {
    return withContext(Dispatchers.IO) {

        var filter = f
        if (filter.isEmpty()) {
            val dayStop = selectedDate.format(Constants.accessFormater)
            val dayStart =
                selectedDate.minusDays(daysBeforeNow.toLong()).format(Constants.accessFormater)

            filter = "WHERE ReceivedDate <= #$dayStop# and ReceivedDate >= #$dayStart#"
        }

        val q = Query("select * from Invoice $filter order by ReceivedDate DESC")
        val s = q.execute(Constants.url)
        val res = mutableListOf<Invoice>()
        q.res.forEach { invoice ->
            res.add(
                Invoice(
                    id = invoice["ID"]!!.toInt(),
                    supplierId = invoice["Supplier"]!!.toInt(),
                    invoiceDate = invoice["InvoiceDate"]!!,
                    invoiceNumber = invoice["InvNumber"]!!,
                    amount = invoice["Amount"]?.toDoubleOrNull() ?: 0.0,
                    receivedDate = invoice["ReceivedDate"]!!,
                    remark = invoice["Remark"]!!,
                    items = mutableListOf(),
                    dueOn = invoice["DueOn"] ?: "",
                    paid = invoice["Paid"]?.toDoubleOrNull() ?: 0.0,
                    remain = invoice["Remain"]?.toDoubleOrNull() ?: 0.0,
                    status = invoice["Status"] ?: "",
                    discountPercentage = invoice["Discount"]?.toDoubleOrNull() ?: 0.0,
                    registeredBy = invoice["RegisteredBy"] ?: "",
                    createdDate = invoice["CreatedDate"] ?: "",
                    currency = invoice["Curr"] ?: "",
                    accountCode = invoice["AccountCode"] ?: "0",
                    validated = invoice["Validated"] == "1"
                )
            )
        }
        return@withContext res.toList()
    }

}


suspend fun getOrderIdFromSupplierOrderNumber(supplierOrderNumber: String): Int {
    return withContext(Dispatchers.IO) {
        var query =
            "select OrderComponentID FROM SupplierOrderMain WHERE SupplierOrderNumber = '$supplierOrderNumber'"
        var q = Query(query)
        var res = q.execute(Constants.url)
        if (!res) {
            return@withContext -1
        }
        val orderComponentID = q.res[0]["OrderComponentID"]?.toIntOrNull() ?: -1
        if (orderComponentID == -1) {
            return@withContext -1
        }
        query = "select OrderNumber FROM OrderComponent WHERE ID = $orderComponentID"
        q = Query(query)
        res = q.execute(Constants.url)
        return@withContext q.res[0]["OrderNumber"]?.toIntOrNull() ?: -1

    }
}

suspend fun getStockHistoryForOrders(
    supplierOrderId: Int,
    supplierOrderNumber: List<String>,
//        supplierName: String,
): Map<String, MutableList<StockHistory>> {
    return withContext(Dispatchers.IO) {
        var ids_string = ""
        if (supplierOrderNumber.isNotEmpty()) {
            supplierOrderNumber.forEach { ids_string += ("'$it', ") }
            ids_string = ids_string.removeRange(ids_string.length - 2, ids_string.length)
            Log.e("ids", ids_string)
        }
        if (ids_string.isEmpty()) {
            return@withContext mapOf()
        }

        val query = if (supplierOrderId == 0) {
            "select * from StockHistory1  where OrderNumber = '${supplierOrderNumber}'"
        } else {
//            "select StockHistory1.*, Products.ProductCode from StockHistory1  inner join Products on StockHistory1.ProductID = Products.ID where OrderNumber in (${ids_string})"
            "select * FROM StockHistory1 where OrderNumber in (${ids_string})"
        }
        val q = Query(query)
        val success = q.execute(Constants.url)
        val res = mutableMapOf<String, MutableList<StockHistory>>()
        q.res.forEach { map ->
            Log.e("current map", map.toString())
            res.putIfAbsent(map["OrderNumber"]!!, mutableListOf())
            res[map["OrderNumber"]]?.add(
                StockHistory(
                    id = map["ID"]!!.toInt(),
                    orderNumber = map["OrderNumber"]!!,
                    supplierOrderNumber = map["SupplierOrderMainID"]!!,
                    historicDate = map["HistoricDate"]!!,
                    productId = map["ProductID"]!!.toIntOrNull()?:0,
                    supplier = map["Supplier"]!!,
                    detail = map["Detail"]!!,
                    type = map["Type"]!!.toIntOrNull() ?: -1,
                    quantity = map["Quantity"]?.toDoubleOrNull() ?: 0.0,
                    cost = map["Cost"]?.toDoubleOrNull() ?: 0.0,
                    remark = map["Remark"]!!,
                    weight = map["Weight"]?.toDoubleOrNull() ?: 0.0,
                    loss = map["Loss"]!!,
                    process = map["Process"]!!,
                    flow = map["Flow"]!!
//                    shortCode = map["ProductCode"]!!,

                )
            )
        }
        return@withContext res
    }

}


suspend fun getSupplierOrderMainHistory(
    contactName: String,
): List<SupplierOrderMain> {
    return withContext(Dispatchers.IO) {
        val q =
            Query("select SupplierOrderMain.*, OrderComponent.OrderNumber from SupplierOrderMain INNER JOIN OrderComponent on SupplierOrderMain.OrderComponentID = OrderComponent.ID WHERE Recipient = '${contactName}' order by Status ASC, Deadline")
        val s = q.execute(Constants.url)
        val res = mutableListOf<SupplierOrderMain>()
        q.res.forEach { supplierOrderMain ->
            res.add(
                SupplierOrderMain(
                    supplierOrderMain["ID"]!!.toInt(),
                    supplierOrderMain["OrderComponentID"]!!.toIntOrNull(),
                    supplierOrderMain["Instruction"] ?: "",
                    supplierOrderMain["Deadline"] ?: "",
                    supplierOrderMain["Recipient"] ?: "",
                    supplierOrderMain["Status"] ?: "",
                    supplierOrderMain["CreatedDate"] ?: "",
                    supplierOrderMain["Step"] ?: "",
                    supplierOrderMain["InstructionCode"] ?: "",
                    supplierOrderMain["Remark"] ?: "",
                    supplierOrderMain["SupplierOrderNumber"] ?: "",
                    supplierOrderMain["Approval"] ?: "",
                    false,
                    supplierOrderMain["OrderNumber"] ?: ""
                )
            )
        }

        return@withContext res
    }
}

suspend fun getCategories(): Map<Int, String> {
    return withContext(Dispatchers.IO) {
        val q = Query("select * from Category")
        val s = q.execute(Constants.url, "Stock")
        val res = mutableMapOf<Int, String>()
        q.res.forEach { map ->
            res[map["ID"]!!.toInt()] = map["Category"]!!
        }
        Log.e("cat", res.toString())
        return@withContext res
    }
}

suspend fun getOptionValues(): List<OptionValue> {
    return withContext(Dispatchers.IO) {
        val q = Query("select * from OptionValues")
        val s = q.execute(Constants.url)
        val res = mutableListOf<OptionValue>()
        q.res.forEach { map ->
            res.add(
                OptionValue(
                    map["ID"]!!.toInt(), map["Type"]!!, map["OptionKey"]!!, map["OptionValue"]!!
                )
            )
        }
        return@withContext res
    }
}

suspend fun getSupplierOrdersOfIds(
    ids: List<String>,
): List<SupplierOrderMain> {
    return withContext(Dispatchers.IO) {

        if (ids.isEmpty()) {
            return@withContext listOf()
        }
        var ids_string = ""
        if (ids.isNotEmpty()) {
            ids.forEach { ids_string += ("'$it', ") }
            ids_string = ids_string.removeRange(ids_string.length - 2, ids_string.length)
            Log.e("ids", ids_string)
        }

        val query = Query(
            "select * FROM SupplierOrderMain WHERE SupplierOrderNumber in ($ids_string)"
        )

        val success = query.execute(Constants.url)
        val res = mutableListOf<SupplierOrderMain>()
        query.res.forEach { supplierOrderMain ->
            res.add(
                SupplierOrderMain(
                    supplierOrderMain["ID"]!!.toInt(),
                    supplierOrderMain["OrderComponentID"]!!.toIntOrNull(),
                    supplierOrderMain["Instruction"] ?: "",
                    supplierOrderMain["Deadline"] ?: "",
                    supplierOrderMain["Recipient"] ?: "",
                    supplierOrderMain["Status"] ?: "",
                    supplierOrderMain["CreatedDate"] ?: "",
                    supplierOrderMain["Step"] ?: "",
                    supplierOrderMain["InstructionCode"] ?: "",
                    supplierOrderMain["Remark"] ?: "",
                    supplierOrderMain["SupplierOrderNumber"] ?: "",
                    supplierOrderMain["Approval"] ?: "",
                    false,
                )
            )
        }
        return@withContext res
    }
}


suspend fun updateInvoiceItem(invoiceItem: InvoiceItem): Boolean {
    return withContext(Dispatchers.IO) {
        var process = 0
        var flow = 0
        //TODO do it properly
        when (invoiceItem.process) {
            "setting" -> process = 1
            "casting" -> process = 2
            "orf" -> process = 3
            "rep" -> process = 4
            "pol" -> process = 5
            "others" -> process = 6
        }
        when (invoiceItem.flow) {
            "stock" -> flow = 1
            "returned" -> flow = 2
            "process" -> flow = 3
            "finished" -> flow = 4
            "variance" -> flow = 5
        }
        var product = invoiceItem.product.toString()
        if (invoiceItem.product == 0) {
            product = "null"
        }
        var accountCode = invoiceItem.accountCode
        if (invoiceItem.accountCode.isEmpty()) {
            accountCode = "null"
        }

        if (invoiceItem.product == 0) {
            val query =
                "update InvoiceItems " + "set InvoiceID = '${invoiceItem.invoiceId}', SupplierOrderMain_ID = '${invoiceItem.supplierOrderID}', Product = ${product}, Detail = '${invoiceItem.detail}', Unit = '${invoiceItem.unit}', UnitPrice = ${invoiceItem.unitPrice},Quantity = ${invoiceItem.quantity}, Approved = '${invoiceItem.approved}', ApprovedDate = NOW(), AccountCode = ${accountCode}, Process = '${process}', Flow = '${flow}', VAT = ${invoiceItem.vat} WHERE ID = ${invoiceItem.id}"
            val q = Query(query)
            return@withContext q.execute(Constants.url)
        }
        return@withContext false
    }
}

suspend fun getProductStockQuantity(productID: String): Map<Int, Double> {
    return withContext(Dispatchers.IO) {
        if (productID.isEmpty()) {
            return@withContext emptyMap()
        }

        val query = Query(
            "SELECT SUM(Quantity) as total, Type FROM StockHistory1 WHERE ProductID = $productID GROUP BY Type"
        )

        val success = query.execute(Constants.url)
        if (!success) {
            return@withContext emptyMap()
        }

        val typeQuantities = mutableMapOf<Int, Double>()
        query.res.forEach { map ->
            val type = map["Type"]?.toIntOrNull()
            val total = map["total"]?.toDoubleOrNull()
            if (type != null && total != null) {
                typeQuantities[type] = total
            }
        }

        return@withContext typeQuantities
    }
}


