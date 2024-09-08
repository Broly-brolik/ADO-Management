package com.example.aguadeoromanagement.networking.api

import android.util.Log
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.Inventory
import com.example.aguadeoromanagement.networking.Query
import com.example.aguadeoromanagement.utils.filterLocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Timestamp
import java.util.Calendar
import java.util.Date

suspend fun getInventoryWithoutLocation(): List<Inventory> {
    return withContext(Dispatchers.IO) {

        val query =
            Query("select * FROM StockHistory left JOIN Inventory ON StockHistory.InventoryCode = Inventory.InventoryCode where Action='New' AND (IDLocation IS NULL OR IDLocation=0) order by Inventory.CatalogCode asc;")

        val s = query.execute(Constants.url)
        val res = mutableListOf<Inventory>()
        query.res.forEach { map ->

            val inv = Inventory(
                name = map["Name"]!!,
                inventoryCode = map["InventoryCode"]!!,
                imageUrl = Constants.imageUrl + map["Image"]!!,
                rfidTag = map["rfidTag"]!!,
                idLocation = map["IDLocation"]!!
            )
            res.add(inv)
        }
        return@withContext res.distinctBy { it.inventoryCode }
    }

}

suspend fun updateInventoryLocation(
    inventoryList: List<String>,
    newLocationID: Int,
    oldLocationID: Int
): Boolean {
    return withContext(Dispatchers.IO) {
        Log.e("updating ${inventoryList}", "to $newLocationID")
        if (newLocationID == oldLocationID){
            return@withContext true
        }

        if (inventoryList.isEmpty()) {
            return@withContext true
        }
        var ids_string = ""
        inventoryList.forEach { ids_string += ("'${it}', ") }
        ids_string = ids_string.removeRange(ids_string.length - 2, ids_string.length)
        val q =
            Query("update Inventory set CurrentLocationID=$newLocationID where InventoryCode in ($ids_string)")
        val s = q.execute(Constants.url)
//        return@withContext true

        //updating stock history
        val utilDate = Date()
        val cal = Calendar.getInstance()
        cal.time = utilDate
        cal[Calendar.MILLISECOND] = 0
        var time = Timestamp(cal.timeInMillis).toString()
        time = time.substring(0, time.length - 2)

        inventoryList.forEach {
            val code = it

            val queryTransfer =
                "insert into StockHistory (HistoryDate, InventoryCode, IDLocation, Notes, DoneBy, Action) values (#$time#, '$code', $oldLocationID, '', 'AA', 'Transferred')"
            val qT = Query(queryTransfer)
            val sT = qT.execute(Constants.url)

            val queryNew =
                "insert into StockHistory (HistoryDate, InventoryCode, IDLocation, Notes, DoneBy, Action) values (#$time#, '$code', $newLocationID, '', 'AA', 'New')"
            val qN = Query(queryNew)
            val sN: Boolean = qN.execute(Constants.url)
        }



        return@withContext s
    }
}
