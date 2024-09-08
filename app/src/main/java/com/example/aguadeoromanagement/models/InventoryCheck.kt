package com.example.aguadeoromanagement.models

import com.example.aguadeoromanagement.states.ScannedLocationData

data class InventoryCheck(
    val ID: Int,
    val checkDate: String,
    val checkBy: String = "",
    val inventoryData: Map<String, ScannedLocationData>,
    val stockSnapshot: Map<String, List<String>>
)
