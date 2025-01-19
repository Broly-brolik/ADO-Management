package com.example.aguadeoromanagement.models

data class Product(
    var id: Int = 0,
    var productCode: String = "",
    var description: String = "",
    var unit: String = "",
    var category: String = "",
    var subCategory: String = "",
    var type: String = "",
    val entryDate: String = "",
    val line: String = "",
    val colorAdo: String = "",
    val imageUrl: String = "",
    val quantity: Double = 0.0,
)

