package com.example.aguadeoromanagement.models

data class InvoiceHistory(
    val id: Int,
    val invoiceId: Int,
    val amount: Double,
    val paymentMode: String,
    val scheduledDate: String,
    val remark: String,
    val checkedBy: String,
    val executedDate: String,
    val executed: Boolean,
    val entryDate: String = "",
    val isPayment: Boolean
)
