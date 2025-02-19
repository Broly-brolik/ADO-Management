package com.example.aguadeoromanagement.models

data class Product(
    var id: Int = 0,
    var productCode: String = "",
    var orderType: String = "",
    var shortCode: String = "",
    var size: String = "",
    var description: String = "",
    var unit: String = "",
    var category: Int = 0,
    var subCategory: Int = 0,
    var type: String = "",
    val entryDate: String = "",
    val line: String = "",
    val colorAdo: String = "",
    val listPrice: Int = 0,
    val remarks: String = "",
    val remarks2: String = "",
    val standardCost: Int = 0,
    val discontinued: Int = -1,
    val reorderLevel: Int = 0,
    val image: String = "",
    val quantity: Double = 0.0,
)

