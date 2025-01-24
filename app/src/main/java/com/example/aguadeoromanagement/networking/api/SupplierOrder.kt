package com.example.aguadeoromanagement.networking.api

import android.util.Log
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.Product
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.models.SupplierOrderMain
import com.example.aguadeoromanagement.networking.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getSupplierOrderMainHistoryForNumbers(
    order_list: List<String>,
): List<SupplierOrderMain> {
    return withContext(Dispatchers.IO) {
        var ids_string = ""
        if (order_list.isNotEmpty()) {
            order_list.toSet().forEach { ids_string += ("'$it', ") }
            ids_string = ids_string.removeRange(ids_string.length - 2, ids_string.length)
        }
        val q =
            Query("select * from SupplierOrderMain where SupplierOrderNumber in (${ids_string})")
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

suspend fun getOrderComponentBySupplier(orderComponent: String): List<StockHistory>{
    return withContext(Dispatchers.IO){
        val orderComponentString: String = orderComponent.substringBeforeLast("_")
        val q =
            Query("select * FROM StockHistory1 WHERE OrderNumber like '%$orderComponentString%'")
        val succes = q.execute(Constants.url)
        val res = mutableListOf<StockHistory>()
        q.res.forEach{ map ->
            res.add(
                StockHistory(
                    supplierOrderNumber = map["SupplierOrderMainID"]!!,
                    orderNumber = map["OrderNumber"]!!,
                    historicDate = map["HistoricDate"]!!,
                    productId = map["ProductID"]!!.toInt(),
                    supplier = map["Supplier"]!!,
                    type = map["Type"]!!.toInt(),
                    quantity = map["Quantity"]!!.toDouble(),
                    cost = map["Cost"]?.toDoubleOrNull() ?: 0.0,
                    flow = map["Flow"]!!,
                )
            )
        }
        return@withContext res
    }
}
