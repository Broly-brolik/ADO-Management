package com.example.aguadeoromanagement.models

data class SupplierOrder(
    val id: Int,
    val supplierOrderId: String,
    var unitPrice: Double = 0.0,
    var quantity: Double = 0.0,
    var grams: Double = 0.0,
    val unit: String,
    val receivedDate: String,
    val invoiceNumber: String,
    var approved: String,
    val invoiceId: String,
    var approvedDate: String,
    var approveUpdated: Boolean,
    var isExpanded: Boolean,
    var type: Int,
    var children: MutableList<SupplierOrder> = mutableListOf(),
    var productId: Int = 0,
    var amount: Double = 0.0,
)
