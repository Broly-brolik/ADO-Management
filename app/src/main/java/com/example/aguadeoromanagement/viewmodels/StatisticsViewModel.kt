package com.example.aguadeoromanagement.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.models.InventoryCheck
import com.example.aguadeoromanagement.networking.Query
import com.example.aguadeoromanagement.states.ScannedLocationData
import com.example.aguadeoromanagement.utils.getDate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//class ProductDetailsViewModelFactory(private val inventoryCode: String) :
//    ViewModelProvider.NewInstanceFactory() {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T =
//        ProductDetailsViewModel(inventoryCode) as T
//}


class StatisticsViewModel() : ViewModel() {
    var loading = mutableStateOf(true)
    val statisticsData = mutableStateOf(mapOf<String, List<String>>())
    val inventoryData = mutableStateOf(mapOf<String, ScannedLocationData>())
    val images = mutableStateOf(mapOf<String, String>())
    val inventoryChecks = mutableStateOf(listOf<InventoryCheck>())

    init {
        viewModelScope.launch {
//            val status_list = ManagementApplication.getOptionValueList("InventoryStatus")
//            Log.e("status list", status_list.toString())
//            for (status in status_list) {
//                val inventory = getInventoryByStatus(status)
//                val newStat = statisticsData.value.toMutableMap()
//                newStat.putIfAbsent(status, inventory)
//                statisticsData.value = newStat
//            }
//
//            val index = retrieveCurrentCheckIndex()
//
//            val jsonStatistics = Gson().toJson(statisticsData.value)
//            Log.e("current check index", index.toString())
//            Log.e("json", jsonStatistics)
//            updateCurrentCheckStock(jsonStatistics, checkID = index.toString())
//
//            val data = retrieveCurrentCheckData()
//
//            val all_codes = data.values.flatMap { it.detectedCodes }.map { it.code }
//            Log.e("data", all_codes.size.toString())
//            statisticsData.value = retrieveCurrentCheckStockSnapshot(25)
//            inventoryData.value = retrieveCurrentCheckData(25)
            getAllInventoryChecks()
            loading.value = false
        }
    }


    suspend fun getInventoryByStatus(status: String): List<String> {
        return withContext(Dispatchers.IO) {
            val query = Query("select InventoryCode FROM Inventory WHERE Status = '$status'")
            val s = query.execute(Constants.url)
            return@withContext query.res.map { it.getOrDefault("InventoryCode", "") }
                .filter { it.isNotEmpty() }.toList()
        }
    }

    suspend fun getAllInventoryChecks() {
        return withContext(Dispatchers.IO) {
            val query = Query("select * FROM InventoryChecks")
            val s = query.execute(Constants.url)

            val invList: MutableList<InventoryCheck> = mutableListOf()

             query.res.forEach { it ->
                if (it.getOrDefault("StockSnapshot", "").isEmpty()){
                    return@forEach
                }
                val stockSnapshot =
                    Gson().fromJson<MutableMap<String, List<String>>>(
                        it.getOrDefault(
                            "StockSnapshot",
                            ""
                        ),
                        object : TypeToken<Map<String, List<String>>>() {}.type
                    )

                val data =
                    Gson().fromJson<MutableMap<String, ScannedLocationData>>(
                        it.getOrDefault(
                            "Data",
                            ""
                        ),
                        object : TypeToken<Map<String, ScannedLocationData>>() {}.type
                    )

                val invCheck = InventoryCheck(
                    ID = it.getOrDefault("ID", "0").toInt(),
                    checkDate = it.getOrDefault("CheckDate", ""),
                    checkBy = "",
                    inventoryData = data.toMap(),
                    stockSnapshot = stockSnapshot
                )
                 invList.add(invCheck)
            }
            inventoryChecks.value = invList

        }
    }

    suspend fun retrieveCurrentCheckIndex(): Int {
        return withContext(Dispatchers.IO) {

            val query =
                "select ID FROM InventoryChecks where CheckDate = #${getDate()}# Order By CheckDate Desc"
            val q = Query(query)
            val s = q.execute(Constants.url)

            if (q.res.size == 0) {
                return@withContext -1
            } else {
                val checkID = q.res[0].getOrDefault("ID", "-1").toInt()

                return@withContext checkID
            }

        }
    }

    suspend fun updateCurrentCheckStock(stockData: String, checkID: String) {
        return withContext(Dispatchers.IO) {
            val query =
                Query("update InventoryChecks set StockSnapshot = '$stockData' WHERE ID = $checkID")
            val s = query.execute(Constants.url)
            Log.e("success", s.toString())
        }
    }

    suspend fun retrieveCurrentCheckStockSnapshot(ID: Int): Map<String, List<String>> {
        return withContext(Dispatchers.IO) {

//            val query =
//                "select StockSnapshot FROM InventoryChecks where CheckDate = #${getDate()}# Order By CheckDate Desc"
            val query =
                "select StockSnapshot FROM InventoryChecks where ID = $ID"
            val q = Query(query)
            val s = q.execute(Constants.url)

            if (q.res.size == 0) {
                return@withContext emptyMap()
            } else {
                val data =
                    Gson().fromJson<MutableMap<String, List<String>>>(
                        q.res[0].getOrDefault(
                            "StockSnapshot",
                            ""
                        ),
                        object : TypeToken<Map<String, List<String>>>() {}.type
                    )

                return@withContext data.toMap()
            }

        }
    }

    suspend fun retrieveCurrentCheckData(ID: Int): Map<String, ScannedLocationData> {
        return withContext(Dispatchers.IO) {

//            val query =
//                "select * FROM InventoryChecks where CheckDate = #${getDate()}# Order By CheckDate Desc"
            val query =
                "select * FROM InventoryChecks where ID = $ID"
            val q = Query(query)
            val s = q.execute(Constants.url)

            if (q.res.size == 0) {
                return@withContext emptyMap()
            } else {
                val checkID = q.res[0].getOrDefault("ID", "-1").toInt()
                val data =
                    Gson().fromJson<MutableMap<String, ScannedLocationData>>(
                        q.res[0].getOrDefault(
                            "Data",
                            ""
                        ),
                        object : TypeToken<Map<String, ScannedLocationData>>() {}.type
                    )


                return@withContext data.toMap()
            }

        }
    }

    suspend fun retrieveCodeInfo(codes: List<String>) {
        return withContext(Dispatchers.IO) {
            var ids_string = ""
            codes.forEach { ids_string += ("'$it', ") }
            ids_string = ids_string.removeRange(ids_string.length - 2, ids_string.length)


            val query =
                Query("select Image, InventoryCode FROM Inventory WHERE InventoryCode in ($ids_string)")
            val s = query.execute(Constants.url)

            Log.e("codes infos", query.res.toString())
            val map = mutableMapOf<String, String>()
            query.res.forEach { map.putIfAbsent(it["InventoryCode"]!!, it["Image"]!!) }
            images.value = map

        }
    }


}