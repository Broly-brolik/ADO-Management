package com.example.aguadeoromanagement.networking.api

import android.util.Log
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.networking.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getStockHistoryForInvoice(invoiceID: Int): List<StockHistory> {
    return withContext(Dispatchers.IO) {

        val query = Query("select * FROM StockHistory1 WHERE InvoiceID = $invoiceID")
        val s = query.execute(Constants.url)
        val res = mutableListOf<StockHistory>()
        query.res.forEach { map ->
            res.add(
                StockHistory(
                    id = map["ID"]!!.toInt(),
                    orderNumber = map["OrderNumber"]!!,
                    supplierOrderNumber = map["SupplierOrderMainID"]!!,
                    historicDate = map["HistoricDate"]!!,
                    productId = map["ProductID"]!!.toIntOrNull() ?: 0,
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

                )
            )
        }
        return@withContext res
    }
}

suspend fun getStockForSimilarOrders(order:String): List<StockHistory> {
//suspend fun getStockForSimilarOrders(order:String): Map<String,List<StockHistory>> {
    return withContext(Dispatchers.IO){
        val split = order.split("_")
        val prefix = split[0]+"_"+split[1]
        val query = Query("select * FROM StockHistory1 WHERE OrderNumber LIKE '%$prefix%'")
        val s = query.execute(Constants.url)
        val res = mutableListOf<StockHistory>()
        query.res.forEach { map ->
            res.add(
                StockHistory(
                    id = map["ID"]!!.toInt(),
                    orderNumber = map["OrderNumber"]!!,
                    supplierOrderNumber = map["SupplierOrderMainID"]!!,
                    historicDate = map["HistoricDate"]!!,
                    productId = map["ProductID"]!!.toIntOrNull() ?: 0,
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

                )
            )
        }
//        Log.e("similar orders", query.res.map { it["OrderNumber"] }.distinct().toString()  )
//        return@withContext res.groupBy { it.orderNumber }
        return@withContext res
    }
}