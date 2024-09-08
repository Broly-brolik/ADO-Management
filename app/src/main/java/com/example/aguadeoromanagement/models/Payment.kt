package com.example.aguadeoromanagement.models

/**
 * @param scheduledDate - the scheduled date of the payment.
 * @param executedDate - the date where we validated the payment.
 *
 *
 */
data class Payment(
    var id: Int,
    val invoiceID: Int,
    val amountPaid: Double,
    val scheduledDate: String,
    val by: String,
    val remarks: String,
    var executed: Boolean,
    val executedDate: String = "",
    var invoiceNumber: String = "",
    var contactName: String = "",
    var currency: String = "",
    var paymentMode: String = "",
    var checkedBy: String = "",
    var remainingOfInvoice: Double = 0.0,
    var totalOfInvoice: Double = 0.0,
)
