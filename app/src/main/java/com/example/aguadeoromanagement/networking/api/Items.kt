package com.example.aguadeoromanagement.networking.api

import android.util.Log
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.InvoiceItem
import com.example.aguadeoromanagement.networking.Query
import com.example.aguadeoromanagement.networking.getSupplierOrdersOfIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

suspend fun insertInvoiceItem(
    invoiceItem: InvoiceItem,
    invoice: Invoice,
): Int {
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
            Log.e("insert", "inserting item")
            val query = "insert into InvoiceItems " +
                    "(InvoiceID, SupplierOrderMain_ID, Product, Detail, Unit, UnitPrice,Quantity, Approved, ApprovedDate, AccountCode, Process, Flow, VAT, OrderNumber)" +
                    " values (${invoiceItem.invoiceId},'${invoiceItem.supplierOrderID}',${product},'${invoiceItem.detail}','${invoiceItem.unit}',${invoiceItem.unitPrice},${invoiceItem.quantity},'${invoiceItem.approved}',NOW(),${accountCode},'${process}','${flow}',${invoiceItem.vat}, ${invoiceItem.orderNumber})"
            val q = Query(query)
            val id = q.execute(Constants.url, true)
            return@withContext id
        } else {
            Log.e("insert", "inserting stock")
            val sup = getSupplier(invoice.supplierId.toString(), true)
            if (!sup.second) {
                return@withContext -1
            }
            val supplierName = sup.first.name
            val query2 = "insert into StockHistory1 " +
                    "(OrderNumber, HistoricDate, ProductID, Supplier, Detail, Type, Quantity, Cost, Process, Flow)" +
                    " values ('${invoiceItem.supplierOrderID}',NOW(),${invoiceItem.product},'${supplierName}','${invoiceItem.detail}',1,${invoiceItem.quantity},${invoiceItem.unitPrice},${process},${flow})"
            val q = Query(query2)
            delay(100)
            return@withContext q.execute(Constants.url, true)
        }
    }
}


suspend fun getInvoiceItems(invoice: Invoice): List<InvoiceItem> {
    return withContext(Dispatchers.IO) {
        val q = Query(
            "select * from InvoiceItems where InvoiceID = ${invoice.id}"
        )
        val success = q.execute(Constants.url, "Supplier invoices")
        val res = mutableListOf<InvoiceItem>()
        q.res.forEach { item ->
            res.add(
                InvoiceItem(
                    id = item["ID"]!!.toInt(),
                    invoiceId = item["InvoiceID"]!!.toInt(),
                    supplierOrderID = item["SupplierOrderMain_ID"]!!,
                    quantity = item["Quantity"]?.toDoubleOrNull() ?: 0.0,
                    unitPrice = item["UnitPrice"]?.toDoubleOrNull() ?: 0.0,
                    unit = item["Unit"]!!,
                    approved = item["Approved"]!!,
                    approvedDate = item["ApprovedDate"]!!,
                    product = item["Product"]?.toIntOrNull() ?: 0,
                    accountCode = item["AccountCode"]!!,
                    process = item["Process"]!!,
                    flow = item["Flow"]!!,
                    vat = item["VAT"]?.toDoubleOrNull() ?: 0.0,
                    detail = item["Detail"]!!,
                )
            )
        }

        val supOrdersNumber = res.map { item-> item.supplierOrderID }.distinct()
        if (supOrdersNumber.isEmpty()){
            return@withContext res
        }
        val supOrders = getSupplierOrdersOfIds(supOrdersNumber)
        val supOrdersMap = mutableMapOf<String, String>()
        supOrders.forEach { order -> supOrdersMap[order.supplierOrderNumber] = order.instruction }
        res.forEach { item-> item.instruction = supOrdersMap.getOrDefault(item.supplierOrderID, "-") }

        return@withContext res
    }
}