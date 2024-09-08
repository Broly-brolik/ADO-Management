package com.example.aguadeoromanagement.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InvoiceItem(
    var id: Int = 0,
    var invoiceId: Int,
    val supplierOrderID: String,
    var supplierOrderStatus: String = "",
    val product: Int,
    val quantity: Double,
    val unitPrice: Double,
    val unit: String,
    val approved: String,
    val approvedDate: String,
    val accountCode: String,
    val process: String,
    val flow: String,
    val vat: Double,
    val detail: String,
    var instruction: String = "",
    var uuid: String = "",
    var orderNumber: String = ""
) : Parcelable
