package com.example.aguadeoromanagement.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Invoice(
    var id: Int,
    val supplierId: Int,
    val invoiceDate: String,
    val invoiceNumber: String,
    var amount: Double,
    val receivedDate: String,
    val remark: String,
    val items: List<InvoiceItem>,
    val dueOn: String,
    val paid: Double,
    var remain: Double,
    val discountPercentage: Double,
    var discountAmount: Double = 0.0,
    var status: String,
    val currency: String,
    val createdDate: String,
    val registeredBy: String,
    var accountCode: String = "",
    var validated: Boolean = false
) : Parcelable