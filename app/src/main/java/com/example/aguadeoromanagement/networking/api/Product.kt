package com.example.aguadeoromanagement.networking.api

import android.util.Log
import android.widget.Toast
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.models.Inventory
import com.example.aguadeoromanagement.models.Product
import com.example.aguadeoromanagement.models.SupplierOrderMain
import com.example.aguadeoromanagement.networking.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getProductList(ids: List<String>): List<Product> {
    return withContext(Dispatchers.IO) {
        if (ids.isEmpty()) {
            return@withContext listOf()
        }
        var ids_string = ""
        if (ids.isNotEmpty()) {
            ids.toSet().forEach { ids_string += ("$it, ") }
            ids_string = ids_string.removeRange(ids_string.length - 2, ids_string.length)
            Log.e("ids", ids_string)
        }
        val query = Query(
            "select * FROM Products WHERE ID in (${ids_string})"
        )
        val success = query.execute(Constants.url)
        val res = mutableListOf<Product>()
        query.res.forEach { map ->
            res.add(
                Product(
                    id = map["ID"]?.toIntOrNull() ?: 0,
                    description = map["Description"]!!,
                    productCode = map["ProductCode"]!!,
                    unit = map["Unit"]!!,
                )
            )
        }
        return@withContext res
    }
}

suspend fun getProduct(inventoryCode: String):Inventory? {
    return withContext(Dispatchers.IO) {

        var query =
            "select * from Inventory where Inventory.InventoryCode='${inventoryCode}'"
//            var query =
//                "select * from Inventory where Inventory.InventoryCode='C-513'"
        var q = Query(query)
        var s = q.execute(Constants.url)
        Log.e("size", q.res.size.toString())
        if (!s || q.res.size != 1) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    ManagementApplication.getAppContext(),
                    "Error fetching inventory",
                    Toast.LENGTH_LONG
                ).show()
            }
            return@withContext null
        }
        val map = q.res[0]
        val inv = Inventory(
            name = map["Name"]!!,
            inventoryCode = map["InventoryCode"]!!,
            imageUrl = Constants.imageUrl + map["Image"]!!,
            rfidTag = map["rfidTag"]!!,
            catalogCode = map.getOrDefault("CatalogCode", ""),
            code = map.getOrDefault("Code", ""),
            mark = map.getOrDefault("Mark", ""),
            category = map.getOrDefault("Category", ""),
            description = map.getOrDefault("Description", ""),
            width = map.getOrDefault("Width", ""),
            height = map.getOrDefault("Height", ""),
            material = map.getOrDefault("Material", ""),
            color = map.getOrDefault("Color", ""),
            weight = map.getOrDefault("Weight", ""),
            size = map.getOrDefault("Size", ""),
            stone = map.getOrDefault("Stone", ""),
            stoneQuantity = map.getOrDefault("StoneQuantity", ""),
            stoneType = map.getOrDefault("StoneType", ""),
            stoneSize = map.getOrDefault("StoneSize", ""),
            carat = map.getOrDefault("Carat", ""),
            stoneColor = map.getOrDefault("StoneColor", ""),
            stoneClarity = map.getOrDefault("StoneClarity", ""),
            stoneCut = map.getOrDefault("StoneCut", ""),
            stonePolish = map.getOrDefault("StonePolish", ""),
            stoneSymmetry = map.getOrDefault("StoneSymetry", ""),
            LAB = map.getOrDefault("LAB", ""),
            certificate = map.getOrDefault("Certificate", ""),
            cost = map.getOrDefault("Cost", ""),
            price = map.getOrDefault("Price", ""),
            remark = map.getOrDefault("Remark", ""),
            status = map.getOrDefault("Status", ""),
            line = map.getOrDefault("Line", ""),
            collection = map.getOrDefault("Collection", ""),
            style = map.getOrDefault("Style", ""),
            shape = map.getOrDefault("Forme", ""),
            setting = map.getOrDefault("Setting", ""),
            ringPrize = map.getOrDefault("RingPrize", ""),
            meleePrize = map.getOrDefault("MeleePrize", ""),
            stonePrize = map.getOrDefault("StonePrize", ""),
            orderInstructions = map.getOrDefault("OrderInstructions", ""),
            prRing = map.getOrDefault("PrRing", ""),
            prMelee = map.getOrDefault("PrMelee", ""),
            prStone = map.getOrDefault("PrStone", ""),
            calculatedPrice = map.getOrDefault("CalculatedPrice", ""),
            idLocation = map.getOrDefault("CurrentLocationID", "")

//                idLocation = map["IDLocation"]!!
        )
        return@withContext inv
    }
}

suspend fun searchProductById(productID: String): List<Product> {
    return withContext(Dispatchers.IO){
        val query = Query(
            "select * from Products where ID = $productID"
        )
        val succes = query.execute(Constants.url)
        val res = mutableListOf<Product>()
        query.res.forEach{ map ->
            res.add(
                Product(
                    productCode = map["ProductCode"]!!,
                    category = map["Category"]!!,
                    subCategory = map["Subcategory"]!!,
                    type = map["Type"]!!,
                )
            )
        }
        return@withContext res
    }
}
