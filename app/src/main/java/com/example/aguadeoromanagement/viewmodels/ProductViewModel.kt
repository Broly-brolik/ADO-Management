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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProductViewModel() : ViewModel() {
    val optionValues = mutableStateOf(emptyList<OptionValue>())
    val loading = mutableStateOf(true)


    init {
        viewModelScope.launch {
            optionValues.value = getOptionValues()
            loading.value = false
            Log.e(
                "option v",
                optionValues.value.filter { optionValue -> optionValue.type == "ArticleType" }
                    .map { it.optionValue }.toString()
            )
        }
    }

    suspend fun insertInventory(inventory: Inventory): Boolean {
        return withContext(Dispatchers.IO) {
            val q =
                Query(
                    "insert into Inventory (InventoryCode, CatalogCode, Code, Mark, Category, Description, Width, Height, Material, Color, Weight, Size, Stone, StoneQuantity, StoneType, StoneSize, Carat, StoneColor, StoneClarity, StoneCut, StonePolish, StoneSymetry, LAB, Certificate, Price, Remark, Status, Line, Collection, Style, Forme, Setting, Name, RingPrize, MeleePrize, StonePrize, OrderInstructions, PrRing, PrMelee, PrStone) values ('${inventory.inventoryCode}', '${inventory.catalogCode}', '${inventory.code}', '${inventory.mark}', '${inventory.category}', '${inventory.description}', '${inventory.width}', '${inventory.height}', '${inventory.material}', '${inventory.color}', '${inventory.weight}', '${inventory.size}', '${inventory.stone}', '${inventory.stoneQuantity}', '${inventory.stoneType}', '${inventory.stoneSize}', '${inventory.carat}', '${inventory.stoneColor}', '${inventory.stoneClarity}', '${inventory.stoneCut}', '${inventory.stonePolish}', '${inventory.stoneSymmetry}', '${inventory.LAB}', '${inventory.certificate}', ${inventory.price}, '${inventory.remark}', '${inventory.status}', '${inventory.line}', '${inventory.collection}', '${inventory.style}', '${inventory.shape}', '${inventory.setting}', '${inventory.name}', ${inventory.ringPrize}, ${inventory.meleePrize}, ${inventory.stonePrize}, '${inventory.orderInstructions}', ${inventory.prRing}, ${inventory.prMelee}, ${inventory.prStone})"
                )
            val s = q.execute(Constants.url)


            return@withContext s
        }
    }

    suspend fun copyProduct(inventoryCode: String){
        withContext(Dispatchers.IO){
            val product = getProduct(inventoryCode)
            if (product != null){

            }
        }
    }

    suspend fun getLastItemOfCategory(category: String): Inventory {
        return withContext(Dispatchers.IO) {
            val q = Query("select InventoryCode FROM Inventory WHERE Category = '${category.replace("'", "''")}' ORDER BY ID DESC")
            val s = q.execute(Constants.url)
//            Log.e("res last item", q.res[0].getOrDefault("InventoryCode", "no code"))
            if (q.res.size == 0){
                Toast.makeText(ManagementApplication.getAppContext(), "no code found", Toast.LENGTH_LONG)
                    .show()
                return@withContext Inventory(inventoryCode = "")
            }
            val code = q.res[0].getOrDefault("InventoryCode", "")
            val nb = code.split("-")[1].toInt()+1
            val letter = code.split("-")[0]
            val new_code = "$letter-$nb"

            val product = getProduct(code) ?: return@withContext Inventory(inventoryCode = "")
            product.inventoryCode = new_code
            product.code = new_code

            withContext(Dispatchers.Main) {
                Toast.makeText(ManagementApplication.getAppContext(), new_code, Toast.LENGTH_LONG)
                    .show()
            }
            return@withContext product
        }
    }
}