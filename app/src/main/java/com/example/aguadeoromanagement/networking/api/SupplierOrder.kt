package com.example.aguadeoromanagement.networking.api

import android.util.Log
import com.example.aguadeoromanagement.Constants
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
            Query("select * FROM SupplierOrderMain WHERE SupplierOrderNumber in (${ids_string})")
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