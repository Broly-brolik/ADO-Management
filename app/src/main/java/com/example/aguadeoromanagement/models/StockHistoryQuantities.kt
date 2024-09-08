package com.example.aguadeoromanagement.models


data class StockHistoryQuantities(
    val orderNumber: String,
    var inQuantity: Double,
    var outQuantity: Double,
)
