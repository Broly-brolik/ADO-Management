package com.example.aguadeoromanagement.states

import com.example.aguadeoromanagement.models.Inventory

data class InventoryListState(
    val inventoryList: List<Inventory> = emptyList(),
    val currentLocation: Int = 0,
    val totalLocation: Int = 0,
    val allLocations: List<String> = emptyList(),
    val currentItem: Inventory? = null,
    val loading: Boolean = true,
    val startTransfer: Boolean = false,
    val selectedCodes: List<String> = emptyList(),
    val showTransferPopup: Boolean = false
)
