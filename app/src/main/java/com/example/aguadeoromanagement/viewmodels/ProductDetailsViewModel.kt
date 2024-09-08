package com.example.aguadeoromanagement.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.models.*
import com.example.aguadeoromanagement.networking.Query
import com.example.aguadeoromanagement.networking.api.getInvoiceItems
import com.example.aguadeoromanagement.networking.api.getProduct
import com.example.aguadeoromanagement.networking.getOptionValues
import com.example.aguadeoromanagement.networking.getSupplierInvoices
import com.example.aguadeoromanagement.networking.getSuppliers
import com.example.aguadeoromanagement.utils.filterLocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailsViewModelFactory(private val inventoryCode: String) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProductDetailsViewModel(inventoryCode) as T
}

class ProductDetailsViewModel(inventoryCode: String) : ViewModel() {
    val currentCode = mutableStateOf(inventoryCode)
    val allCodes = mutableStateOf(emptyList<String>())
    var currentInventory = mutableStateOf<Inventory?>(null)
    var productHistory = mutableStateOf(emptyList<Map<String, String>>())


    init {
        viewModelScope.launch {
//            updateProduct()
            if (currentCode.value.isEmpty()) {
                allCodes.value = getAllInventoryCodes()

            } else {
                updateProduct(inventoryCode)
            }
        }
    }

    private suspend fun getAllInventoryCodes(): List<String> {
        return withContext(Dispatchers.IO) {
            val query = Query("select distinct InventoryCode from Inventory")
            val s = query.execute(Constants.url)

            val codes =
                query.res.map { it.getOrDefault("InventoryCode", "") }.filter { it.isNotEmpty() }
//            Log.e("all codes", codes.toString())
            return@withContext codes
        }
    }

    suspend fun updateProduct(invCode: String = "") {
        val inventoryCode =
            if (invCode.isEmpty()) {
                currentCode.value
            } else {
                invCode
            }

        return withContext(Dispatchers.IO) {

            val inv = getProduct(currentCode.value) ?: return@withContext

            val query =
                "select * FROM StockHistory where StockHistory.InventoryCode = '${inventoryCode}' order by HistoryDate DESC"


//            query =
//                "select * FROM StockHistory where StockHistory.InventoryCode = 'C-513' order by HistoryDate DESC"
            val q = Query(query)
            val s = q.execute(Constants.url)
//            val filtered = filterLocationData(q.res)
//            if (filtered.size > 0) {
//
//                inv.idLocation = filtered[0].getOrDefault("IDLocation", ""); Log.e("filetered", q.res.toString())
//            }

            withContext(Dispatchers.Main) {
                productHistory.value = q.res

                currentInventory.value = inv
            }

            Log.e("current inventory", currentInventory.value.toString())

        }
    }

}