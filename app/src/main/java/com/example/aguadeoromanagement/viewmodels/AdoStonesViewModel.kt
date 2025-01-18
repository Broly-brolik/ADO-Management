package com.example.aguadeoromanagement.viewmodels

import com.example.aguadeoromanagement.models.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.networking.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdoStonesViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> get() = _products

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    suspend fun searchProductById(productID: String) {
        // Set _error to null before starting the query
        _error.value = null

        try {
            val query = Query(
                "select * from Products where ID = $productID"
            )

            val success = query.execute(Constants.url)

            if (success) {
                val res = mutableListOf<Product>()
                query.res.forEach { map ->
                    res.add(
                        Product(
                            id = map["ID"]?.toIntOrNull() ?: 0,
                            productCode = map["ProductCode"] ?: "",
                            description = map["Description"] ?: "",
                            unit = map["Unit"] ?: "",
                            category = map["Category"] ?: "",
                            subCategory = map["Subcategory"] ?: "",
                            type = map["Type"] ?: "",
                            entryDate = map["EntryDate"] ?: "",
                            line = map["Line"] ?: "",
                            colorAdo = map["Color_ADO"] ?: "",
                            picture = Constants.imageUrl + (map["Picture"] ?: "") // Construct the image URL
                        )
                    )
                }

                // Update _products with the fetched data
                _products.value = res
            } else {
                // If query fails, set an error message
                _error.value = "Failed to fetch products."
            }
        } catch (e: Exception) {
            // Handle any exception
            _error.value = "Error: ${e.message}"
        }
    }
}
/*
suspend fun updateStockHistory(productId: String) {
    // Set _error to null before starting the query
    _error.value = null

    try {
        // First, fetch the total values for Type=1 and Type=2 for the given productId
        val queryType1 = Query(
            "SELECT SUM(Quantity) FROM StockHistory WHERE ProductID = $productId AND Type = 1"
        )
        val queryType2 = Query(
            "SELECT SUM(Quantity) FROM StockHistory WHERE ProductID = $productId AND Type = 2"
        )

        val successType1 = queryType1.execute(Constants.url)
        val successType2 = queryType2.execute(Constants.url)

        if (successType1 && successType2) {
            val type1Total = queryType1.res.firstOrNull()?.get("SUM(Quantity)")?.toIntOrNull() ?: 0
            val type2Total = queryType2.res.firstOrNull()?.get("SUM(Quantity)")?.toIntOrNull() ?: 0

            // Subtract the total of Type=2 from Type=1 and update the StockHistory table
            val newTotal = type1Total - type2Total

            // Update the table with the new total for Type=1 (if applicable)
            val updateQuery = Query(
                "UPDATE StockHistory SET Quantity = $newTotal WHERE ProductID = $productId AND Type = 1"
            )
            val successUpdate = updateQuery.execute(Constants.url)

            if (successUpdate) {
                _error.value = "Stock successfully updated."
            } else {
                _error.value = "Failed to update stock."
            }
        } else {
            _error.value = "Failed to fetch stock history."
        }
    } catch (e: Exception) {
        // Handle any exception
        _error.value = "Error: ${e.message}"
    }
}*/

/*
1) ajouter quantity pour un Product Id (INS - OUTS)
2)suggerer les product Id au fur et à mesure qu'on écrit
 */