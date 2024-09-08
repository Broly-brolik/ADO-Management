package com.example.aguadeoromanagement.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.enums.InventoryCheckState
import com.example.aguadeoromanagement.models.Inventory
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.SupplierOrderMain
import com.example.aguadeoromanagement.networking.Query
import com.example.aguadeoromanagement.networking.api.getInvoiceItems
import com.example.aguadeoromanagement.networking.getStockHistoryForOrders
import com.example.aguadeoromanagement.networking.getSupplierOrderMainHistory
import com.example.aguadeoromanagement.networking.getSupplierOrdersOfIds
import com.example.aguadeoromanagement.states.DetectedCodes
import com.example.aguadeoromanagement.states.InventoryListState
import com.example.aguadeoromanagement.states.ScannedLocationData
import com.example.aguadeoromanagement.utils.filterLocationData
import com.example.aguadeoromanagement.utils.getDate
import com.example.aguadeoromanagement.utils.getTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors


class InvoiceDetailsViewModel : ViewModel() {
    val invoice = mutableStateOf(null)


    init {
        viewModelScope.launch {
//            getSupplierOrderMainHistory(supplierName).filter { it.status != "Status 3" && it.status != "Status 0" }
//                .toMutableList()
//            if (invoice.id > 0) {
//                invoiceItems = getInvoiceItems(invoice).toMutableList()
//                val distinctOrders =
//                    invoiceItems.map { invoiceItem -> invoiceItem.supplierOrderID }
//                        .distinct()
//                supplierOrders = getSupplierOrdersOfIds(distinctOrders).toMutableList()
//
//                invoiceItems.forEach { item ->
//                    var exists = false
//                    supplierOrders.forEach { order ->
//                        if (item.supplierOrderID == order.supplierOrderNumber) {
//                            exists = true
//                            item.instruction = order.instruction
//                            item.supplierOrderStatus = order.status
//                        }
//                    }
//                    if (!exists) {
//                        supplierOrders.add(
//                            SupplierOrderMain(
//                                approval = "",
//                                createdDate = "",
//                                deadline = "",
//                                editable = false,
//                                instruction = item.detail,
//                                instructionCode = "",
//                                orderComponentId = 0,
//                                recipient = supplierName,
//                                supplierOrderNumber = item.supplierOrderID,
//                                id = 0,
//                                remark = "",
//                                status = "",
//                                step = ""
//                            )
//                        )
//                    }
//                }
//                stockHistoryForOrders = getStockHistoryForOrders(
//                    1,
//                    supplierOrders.map { it.supplierOrderNumber } + supplierOrderMainHistory.map { it.supplierOrderNumber })
//            }
        }
    }
}



