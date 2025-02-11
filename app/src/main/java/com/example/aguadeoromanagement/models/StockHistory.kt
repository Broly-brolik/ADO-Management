package com.example.aguadeoromanagement.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StockHistory(
    val id: Int = 0,
    var orderNumber: String = "",
    var supplierOrderNumber: String = "",
    var historicDate: String = "",
    var productId: Int = 0,
    var supplier: String = "",
    var detail: String = "",
    var type: Int = 0,
    var quantity: Double = 0.0,
    var cost: Double = 0.0,
    var remark: String = "",
    val weight: Double = 0.0,
    val loss: String = "",
    val process: String = "",
    var flow: String = "",
    var shortCode: String = "",
    var uuid: String = "",
    var category: String = ""
) : Parcelable
