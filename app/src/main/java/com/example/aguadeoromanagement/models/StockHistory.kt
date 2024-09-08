package com.example.aguadeoromanagement.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StockHistory(
    val id: Int,
    val orderNumber: String,
    var supplierOrderNumber: String = "",
    val historicDate: String,
    val productId: Int,
    val supplier: String,
    val detail: String,
    val type: Int,
    val quantity: Double,
    val cost: Double,
    val remark: String,
    val weight: Double,
    val loss: String,
    val process: String,
    val flow: String,
    var shortCode: String = "",
    var uuid: String = "",
    var category: String = ""
) : Parcelable
