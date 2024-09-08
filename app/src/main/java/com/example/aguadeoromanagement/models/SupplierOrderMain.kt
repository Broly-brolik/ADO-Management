package com.example.aguadeoromanagement.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SupplierOrderMain(
    val id: Int,
    val orderComponentId: Int?,
    var instruction: String,
    val deadline: String,
    val recipient: String,
    var status: String,
    val createdDate: String,
    val step: String,
    val instructionCode: String,
    val remark: String,
    var supplierOrderNumber: String,
    val approval: String,
    val editable: Boolean,
    var orderNumber: String = "",

    ) : Parcelable
