package com.example.aguadeoromanagement.networking.api

import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.models.SupplierOrderMain
import com.example.aguadeoromanagement.networking.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getSupplierOrderMainHistoryForNumbers(
    orderList: List<String>,
): List<SupplierOrderMain> {
    return withContext(Dispatchers.IO) {
        var idsString = ""
        if (orderList.isNotEmpty()) {
            orderList.toSet().forEach { idsString += ("'$it', ") }
            idsString = idsString.removeRange(idsString.length - 2, idsString.length)
        }
        val q =
            Query("select * from SupplierOrderMain where SupplierOrderNumber in (${idsString})")
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
            Query("select * from StockHistory1 where OrderNumber like '%$orderComponentString%'")
        val succes = q.execute(Constants.url)
        val res = mutableListOf<StockHistory>()
        q.res.forEach{ map ->
            res.add(
                StockHistory(
                    supplierOrderNumber = map["SupplierOrderMainID"]!!,
                    orderNumber = map["OrderNumber"]!!,
                    historicDate = map["HistoricDate"]!!,
                    productId = map["ProductID"]!!.toIntOrNull() ?: 0,
                    supplier = map["Supplier"]!!,
                    type = map["Type"]!!.toInt(),
                    quantity = map["Quantity"]!!.toDouble(),
                    cost = map["Cost"]?.toDoubleOrNull() ?: 0.0,
                    flow = map["Flow"]!!,
                    process = map["Process"]!!,
                )
            )
        }
        return@withContext res
    }
}
