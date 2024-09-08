package com.example.aguadeoromanagement.states

data class ScannedLocationData(
    var inventoryList: MutableList<String>,
    var detectedCodes: MutableList<DetectedCodes>,
    var scanned: Boolean
    )

data class DetectedCodes(
    var code: String,
    var new: Boolean
)