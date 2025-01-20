package com.example.aguadeoromanagement.viewmodels

import android.util.Log
import com.example.aguadeoromanagement.models.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.networking.Query
import com.example.aguadeoromanagement.networking.api.getProductStockQuantity
import kotlinx.coroutines.launch

class AdoStonesViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> get() = _products

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun searchProductById(productID: String) {
        val query = Query("select * from Products where ID = $productID")
        val success = query.execute(Constants.url)
        if (success) {
            val res = mutableListOf<Product>()

            query.res.forEach { map ->
                val product = Product(
                    id = map["ID"]?.toDoubleOrNull()?.toInt() ?: 0,
                    productCode = map["ProductCode"] ?: "",
                    description = map["Description"] ?: "",
                    unit = map["Unit"] ?: "",
                    category = map["Category"] ?: "",
                    subCategory = map["Subcategory"] ?: "",
                    type = map["Type"] ?: "",
                    entryDate = map["EntryDate"] ?: "",
                    line = map["Line"] ?: "",
                    colorAdo = map["Color_ADO"] ?: "",
                    imageUrl = if (map["Picture"].isNullOrEmpty()) {
                        ""
                    } else {
                        "${Constants.imageUrl}/stones/${map["Picture"]}"
                    }
                )

                viewModelScope.launch {
                    val stockQuantities = getProductStockQuantity(product.id.toString())
                    val totalQuantity = (stockQuantities[1] ?: 0.0) - (stockQuantities[2] ?: 0.0)

                    res.add(product.copy(quantity = totalQuantity))

                    _products.value = res
                }
            }
        } else {
            _error.value = "Failed to fetch products."
        }
    }
}
