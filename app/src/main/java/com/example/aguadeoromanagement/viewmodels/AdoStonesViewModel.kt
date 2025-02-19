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
        val query2 = Query("select Picture.FileData from Products where ID = $productID")
        val success2 = query2.execute(Constants.url)

        if (success2) { // et ici
            Log.d("zzz", "ici")
            val imageData = query2.res.firstOrNull()?.get("Picture.FileData") ?: ""
            if (imageData.isNotEmpty()) {
                Log.d("AdoStonesViewModel", "Image data found for product $productID")
            }
        }

        if (success) {
            val res = mutableListOf<Product>()

            query.res.forEach { map ->
                val product = Product(
                    id = map["ID"]?.toIntOrNull() ?: 0,
                    productCode = map["ProductCode"] ?: "",
                    description = map["Description"] ?: "",
                    unit = map["Unit"] ?: "",
                    category = map["Category"]?.toIntOrNull() ?: 0,
                    subCategory = map["Subcategory"]?.toIntOrNull() ?: 0,
                    type = map["Type"] ?: "",
                    entryDate = map["EntryDate"] ?: "",
                    line = map["Line"] ?: "",
                    colorAdo = map["Color_ADO"] ?: "",
                    image = map["Picture"] ?: "" // ici
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

    fun getNextId(): Int {
        val lastIdQuery = Query("select max(ID) from Products")
        val success = lastIdQuery.execute(Constants.url)
        var newId = 0
        val lastId = lastIdQuery.res.firstOrNull()?.get("Expr1000")?.toDouble()?.toInt()
        newId = if (lastId != null) lastId + 1 else 0
        return newId
    }

    fun createNewProduct(product: Product) {

        val query = Query(
            "insert into Products (ID, ProductCode, OrderType, ShortCode, Size, Description, Unit, Category, Subcategory, Type, EntryDate, Line, Color_ADO, List_Price, Remarks, Remarks2, Standard_Cost, Discontinued, ReorderLevel) values (" +
                    "${product.id}, " +
                    "'${product.productCode}', " +
                    "'${product.orderType}', " +
                    "'${product.shortCode}', " +
                    "'${product.size}', " +
                    "'${product.description}', " +
                    "'${product.unit}', " +
                    "${product.category}, " +
                    "${product.subCategory}, " +
                    "'${product.type}', " +
                    "#${product.entryDate}#, " +
                    "'${product.line}', " +
                    "'${product.colorAdo}', " +
                    "${product.listPrice}, " +
                    "'${product.remarks}', " +
                    "'${product.remarks2}', " +
                    "'${product.standardCost}', " +
                    "${product.discontinued}, " +
                    "${product.reorderLevel} " +
                    ")"
        )
        val success = query.execute(Constants.url)
        if (!success) {
            _error.value = "Failed to create product."
        }
    }
}
