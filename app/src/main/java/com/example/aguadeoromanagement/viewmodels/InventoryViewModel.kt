package com.example.aguadeoromanagement.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.enums.InventoryCheckState
import com.example.aguadeoromanagement.models.Inventory
import com.example.aguadeoromanagement.networking.Query
import com.example.aguadeoromanagement.networking.api.getInventoryWithoutLocation
import com.example.aguadeoromanagement.networking.api.updateInventoryLocation
import com.example.aguadeoromanagement.states.DetectedCodes
import com.example.aguadeoromanagement.states.InventoryListState
import com.example.aguadeoromanagement.states.ScannedLocationData
import com.example.aguadeoromanagement.utils.filterLocationData
import com.example.aguadeoromanagement.utils.getDate
import com.example.aguadeoromanagement.utils.getTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors

class InventoryViewModelFactory(private val locationId: Int) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        InventoryViewModel(locationId) as T


class InventoryViewModel(locationId: Int) : ViewModel() {
    //val inventoryListState = mutableStateOf(InventoryListState(currentLocation = locationId-1))
    val inventoryListState = mutableStateOf(InventoryListState())
    val deviceConnected = mutableStateOf(false)
    val scanningLocation = mutableStateOf(false)
    val showPopup = mutableStateOf(false)
    val showPopupSummaryLocation = mutableStateOf(false)
    var detectedInventoryCodes = mutableStateOf(setOf<DetectedCodes>())
    var newInventoryList = mutableStateOf(setOf<Inventory>())
    var detectedRFID: MutableSet<String> = mutableSetOf()
    var previousRFID: MutableSet<String> = mutableSetOf()
    var suppressedCode: MutableSet<String> = mutableSetOf()
    var timer = Timer()
    var busy = false
    val scannedLocationData = mutableStateOf(mutableMapOf<String, ScannedLocationData>())
    val showCheckScreen = mutableStateOf(false)
    var imageLinks = mutableStateOf(mapOf<String, String>())
    var savingInventoryReport = mutableStateOf(false)
    var insertingItems = mutableStateOf(false)
    var inventoryCheckState = mutableStateOf(InventoryCheckState.check_finished)
    var currentCheckID = mutableStateOf(-1)
    var locationItemsNumber = mutableStateOf<List<Int>>(emptyList())
    var checkScreenLoading = mutableStateOf(false)
    var showMissingLocation = mutableStateOf(false)

        init {

//        val inv1 = listOf("S-1422", "S-1194", "S-1337", "S-1420", "M-1128", "M-1143", "C-433", "C-264", "O-382", "O-1101", "B-282", "B-118", "A-120", "O-249", "O-379", "O-380", "O-374", "O-375", "O-381", "C-469", "C-509")
//        val det1 = listOf("S-1422", "S-1194", "S-1337", "S-1420", "M-1128", "M-1143", "C-433", "C-264", "O-382", "O-1101", "B-282", "B-118", "A-120", "O-249", "O-379", "O-380", "O-374", "O-375", "O-381", "C-469")
//        val scannedData1 = ScannedLocationData(inv1, det1)
//        scannedLocationData.value["1"]=scannedData1
//
//        val inv2 = listOf("S-1016", "S-1369", "S-1415", "S-1448", "S-1366", "S-1230", "S-1421", "S-1214", "S-1445", "S-1425", "S-1428", "S-1378", "S-1427", "S-1384", "S-1376", "S-1276", "S-1277", "S-1185", "S-1392", "S-1272", "S-1389", "S-1242", "S-1296", "S-1258", "S-1313", "S-1305", "S-1087", "S-1095", "S-1430", "S-1406", "S-1438", "S-1316", "S-1309", "S-1310", "S-1419", "S-1345", "S-1370", "S-1349", "S-1359", "S-1411", "S-1361", "S-1126", "S-1252", "S-1336", "S-1134", "M-1141", "M-1146", "C-304", "C-394", "C-425"," C-395", "O-268", "O-309", "O-1014", "O-1019", "O-311", "O-308", "O-317", "O-1034", "O-1035", "O-1036", "C-428")
//        val det2 = listOf("S-1016", "S-1369", "S-1415", "S-1448", "S-1366", "S-1230", "S-1421", "S-1214", "S-1445", "S-1425", "S-1428", "S-1378", "S-1427", "S-1384", "S-1376", "S-1276", "S-1277", "S-1185", "S-1392", "S-1272", "S-1389", "S-1242", "S-1296", "S-1258", "S-1313", "S-1305", "S-1087", "S-1095", "S-1430", "S-1406", "S-1438", "S-1316", "S-1309", "S-1310", "S-1419", "S-1345", "S-1370", "S-1349", "S-1359", "S-1411", "S-1361", "S-1126", "S-1252", "S-1336", "S-1134", "M-1141", "M-1146", "C-304", "C-394", "C-425"," C-395", "O-268", "O-309", "O-1014", "O-1019", "O-311", "O-308", "O-317", "O-1034", "O-1035", "O-1036", "C-509")
//        val scannedData2 = ScannedLocationData(inv2, det2)
//        scannedLocationData.value["5"]=scannedData2

        viewModelScope.launch {
            val currentCheckID = retrieveCurrentCheck()

            getLocationInfo()
//
            fetchLocationInventory(inventoryListState.value.currentLocation)
////           
//            locationItemsNumber.value = getLocationItemNumber()
//            val inv1 = getInventoryForLocation(8)
//            Log.e("inv for 8", inv1.map{it.inventoryCode}.toString())
//            updateInventoryLocation(inv1, 8)
//            }
            val equal = mutableListOf<Int>()
            val not_equal = mutableListOf<Int>()
//            inventoryListState.value.allLocations.forEachIndexed { index, s ->
////                if (index < 2) {
//                val i = index + 1
//
////                val inv1 = getInventoryForLocation(i).map{it.inventoryCode}.sorted()
//                val inv2 = getInventoryForLocationWithStockHistory(i, withVerified = true).map { it.inventoryCode }.sorted()
////                Log.e("inv1 for $i", inv1.toString())
////                Log.e("inv2 for $i", inv2.toString())
////
////                if (inv1==inv2){
////                    Log.e("equal", "for $i")
////                    equal.add(i)
////                }else{
////                    Log.e("Not equal", "for $i")
////                    not_equal.add(i)
////                }
//                updateInventoryLocation(inv2, i)
//
////                val inv = getInventoryForLocation(i)
////                val s = updateInventoryLocation(inv, i)
////                if (s) {
////                    Log.e("update sucessful for, ", i.toString())
////                }
//
////                }
//            }
//            Log.e("equal", equal.toString())
//            Log.e("not_equal", not_equal.toString())
//
//
        }
    }

    suspend fun validateCheck() {
        savingInventoryReport.value = true
        withContext(Dispatchers.Main) {
            val time = LocalDateTime.now().format(Constants.accessFormaterDateTime)
            val query =
                "insert into InventoryChecks (CheckDate) values (#$time#)"
            val q = Query(query)
            val id: Int = q.execute(Constants.url, true)
            Log.e("new id", id.toString())


            for (locationID in scannedLocationData.value.keys) {
                Log.e("validate", locationID)
                updateLocation(
                    location = locationID.toInt(),
                    inventoryList = scannedLocationData.value[locationID]!!.inventoryList,
                    detectedCodes = scannedLocationData.value[locationID]!!.detectedCodes.map { it.code }
                        .toSet(),
                    validatingCheck = true,
                    inventoryCheckID = id.toString()
                )
            }
            savingInventoryReport.value = false
        }
    }


    suspend fun updateLocation(
        location: Int,
        inventoryList: List<String> = inventoryListState.value.inventoryList.map { it.inventoryCode },
        detectedCodes: Set<String> = detectedInventoryCodes.value.map { it.code }.toSet(),
        validatingCheck: Boolean = false,
        inventoryCheckID: String = "0"
    ) {

        //TODO update after new into currentLocationID
        insertingItems.value = true
        withContext(Dispatchers.IO) {
            val notFound =
                inventoryList.toMutableList()
            notFound.removeAll(detectedCodes)

            val foundCodes = inventoryList.intersect(detectedCodes)
            val newCodes = detectedCodes.minus(inventoryList.toSet())

//        Log.e("detected size", detectedInventoryCodes.value.toString())
//        Log.e("not found size", notFound.toString())
//        Log.e("current loc", inventoryListState.value.currentLocation.toString())
            val utilDate = Date()
            val cal = Calendar.getInstance()
            cal.time = utilDate
            cal[Calendar.MILLISECOND] = 0
            var time = Timestamp(cal.timeInMillis).toString()
            time = time.substring(0, time.length - 2)

            if (validatingCheck) {
                for (code in foundCodes) {
                    val query =
                        "insert into StockHistory (HistoryDate, InventoryCode, IDLocation, Notes, DoneBy, Action, InventoryCheckID) values (#$time#, '$code', $location, '', 'AA', 'Verified', $inventoryCheckID)"
                    val q = Query(query)
                    val s: Boolean = q.execute(Constants.url)


                }
                for (code in newCodes) {
                    val query =
                        "insert into StockHistory (HistoryDate, InventoryCode, IDLocation, Notes, DoneBy, Action, InventoryCheckID) values (#$time#, '$code', $location, '', 'AA', 'New', $inventoryCheckID)"
                    val q = Query(query)
                    val s: Boolean = q.execute(Constants.url)

                    val queryLoc = "update Inventory set CurrentLocationID = $location WHERE InventoryCode = '$code'"
                    val q2 = Query(queryLoc)
                    val s2: Boolean = q2.execute(Constants.url)
                }
            } else {
                for (code in detectedCodes) {
                    //d'abord on doit mettre "transferred" dans l'ancienne location

                    var query =
                        "select IDLocation FROM StockHistory WHERE InventoryCode = '$code' and Action='New' Order By HistoryDate Desc"
                    var q = Query(query)
                    var s: Boolean = q.execute(Constants.url)
                    Log.e("id loc mec", q.res.toString())
                    if (s && q.res.size > 0) {
                        val idLoc = q.res[0].getOrDefault("IDLocation", "")
                        if (idLoc.isNotEmpty() && idLoc != location.toString()) {
                            query =
                                "insert into StockHistory (HistoryDate, InventoryCode, IDLocation, Notes, DoneBy, Action, InventoryCheckID) values (#$time#, '$code', $idLoc, '', 'AA', 'Transferred', $inventoryCheckID)"
                            q = Query(query)
                            s = q.execute(Constants.url)
                        }
                        query =
                            "insert into StockHistory (HistoryDate, InventoryCode, IDLocation, Notes, DoneBy, Action, InventoryCheckID) values (#$time#, '$code', 65, '', 'AA', 'Transferred', $inventoryCheckID)"
                        q = Query(query)
                        s = q.execute(Constants.url)
                    }

                    query =
                        "insert into StockHistory (HistoryDate, InventoryCode, IDLocation, Notes, DoneBy, Action, InventoryCheckID) values (#$time#, '$code', $location, '', 'AA', 'New', $inventoryCheckID)"
                    q = Query(query)
                    s = q.execute(Constants.url)

                    val queryLoc = "update Inventory set CurrentLocationID = $location WHERE InventoryCode = '$code'"
                    val q2 = Query(queryLoc)
                    val s2: Boolean = q2.execute(Constants.url)
                }
            }

            //lorsqu'on ne trouve pas le code alors on le met dans 65 (new) et on dit qu'il est parti de la location ou il etait supposé etre trouvé
            for (code in notFound) {
                val query =
                    "insert into StockHistory (HistoryDate, InventoryCode, IDLocation, Notes, DoneBy, Action, InventoryCheckID) values (#$time#, '$code', 65, '', 'AA', 'New', $inventoryCheckID)"
                val q = Query(query)
                val s: Boolean = q.execute(Constants.url)

                val queryLoc = "update Inventory set CurrentLocationID = 65 WHERE InventoryCode = '$code'"
                val q2 = Query(queryLoc)
                val s2: Boolean = q2.execute(Constants.url)
            }
            for (code in notFound) {
                val query =
                    "insert into StockHistory (HistoryDate, InventoryCode, IDLocation, Notes, DoneBy, Action, InventoryCheckID) values (#$time#, '$code', $location, '', 'AA', 'Transferred', $inventoryCheckID)"
                val q = Query(query)
                val s: Boolean = q.execute(Constants.url)

                val queryLoc = "update Inventory set CurrentLocationID = 65 WHERE InventoryCode = '$code'"
                val q2 = Query(queryLoc)
                val s2: Boolean = q2.execute(Constants.url)
            }

            insertingItems.value = false
            fetchLocationInventory(inventoryListState.value.currentLocation)
        }


    }




    suspend fun changeShowMissing() {
        return withContext(Dispatchers.IO) {
            inventoryListState.value = inventoryListState.value.copy(loading = true)
            showMissingLocation.value = !showMissingLocation.value

            if (showMissingLocation.value) {
                inventoryListState.value =
                    inventoryListState.value.copy(
                        inventoryList = getInventoryWithoutLocation(),
                    )
            } else {
                fetchLocationInventory(inventoryListState.value.currentLocation)
            }
            inventoryListState.value = inventoryListState.value.copy(loading = false)
        }
    }

    fun addRemoveSelected(selected: String, remove: Boolean) {
        val newList = mutableListOf<String>()
        newList.addAll(inventoryListState.value.selectedCodes)
        if (remove) {
            newList.remove(selected)
        } else {
            newList.add(selected)
        }
        inventoryListState.value = inventoryListState.value.copy(selectedCodes = newList)
    }


    fun saveLocation() {
        val state = inventoryListState.value
        val scannedData = ScannedLocationData(
            inventoryList = state.inventoryList.map { it.inventoryCode }.toMutableList(),
            detectedCodes = detectedInventoryCodes.value.toMutableList(),
            scanned = true
        )
        scannedLocationData.value[(state.currentLocation + 1).toString()] = scannedData

        Log.e("scannedlocdata", scannedLocationData.toString())

        if (inventoryCheckState.value == InventoryCheckState.check_started) {
            viewModelScope.launch { saveCurrentCheck() }
        }
    }

    private suspend fun saveCurrentCheck(): Unit {
        inventoryListState.value = inventoryListState.value.copy(loading = true)
        val location = inventoryListState.value.currentLocation + 1
        return withContext(Dispatchers.IO) {
            val inventoryList: List<String> =

                inventoryListState.value.inventoryList.map { it.inventoryCode }
            val detectedCodes = detectedInventoryCodes.value
            val foundCodes = inventoryList.intersect(detectedCodes.filter { it.new }.map { it.code }
                .toSet())

            if (currentCheckID.value == -1) {
                val query =
                    "insert into InventoryChecks (CheckDate) values (#${getDate()}#)"
                val q = Query(query)
                currentCheckID.value = q.execute(Constants.url, insert = true)
            }

            var ok = true
            for (code in foundCodes) {
                val query =
                    "insert into StockHistory (HistoryDate, InventoryCode, IDLocation, Notes, DoneBy, Action, InventoryCheckID) values (#${getTime()}#, '$code', ${location.convertMiloID()}, '', 'Antoine', 'Verified', ${currentCheckID.value})"
                val q = Query(query)
                val s: Boolean = q.execute(Constants.url)
                if (s) {
                    val newDet = detectedCodes
                    val det = newDet.find { it.code == code }
                    if (det != null) {
                        det.new = false
                        detectedInventoryCodes.value = newDet
                    }
                } else {
                    ok = false
                }
            }
            scannedLocationData.value[location.toString()]?.scanned = true

            saveCheck()

            withContext(Dispatchers.Main) {
                if (ok) {
                    Toast.makeText(
                        ManagementApplication.getAppContext(),
                        "Location saved successfully",
                        Toast.LENGTH_LONG
                    ).show()


                } else {
                    Toast.makeText(
                        ManagementApplication.getAppContext(),
                        "Error saving location",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            inventoryListState.value = inventoryListState.value.copy(loading = false)

        }
    }

    fun removeNewItem(code: String, rfid_tag: String) {
        Log.e("is busy", busy.toString())
//        if (!busy) {

        timer.cancel()
        suppressedCode.add(code)
        detectedRFID.removeIf { it == rfid_tag }
        val newDet = detectedInventoryCodes.value.toMutableList()
        newDet.removeIf { it.code == code }
        detectedInventoryCodes.value = newDet.toSet()
        val list = newInventoryList.value.toMutableList()
        list.removeIf { it.inventoryCode == code }
        newInventoryList.value = list.toSet()
        startTimer()
//        }
    }

    suspend fun startTransfer(): Boolean {
//        if (inventoryListState.value.showTransferPopup){
//            //start transfer
//            inventoryListState.value.
//            return false
//        }
        return withContext(Dispatchers.IO) {
            if (!inventoryListState.value.startTransfer) {
                inventoryListState.value = inventoryListState.value.copy(startTransfer = true)
                return@withContext true
            } else {
                if (inventoryListState.value.selectedCodes.isNotEmpty() && !inventoryListState.value.showTransferPopup) {
                    inventoryListState.value =
                        inventoryListState.value.copy(showTransferPopup = true)
                    return@withContext false
                } else {



                    inventoryListState.value =
                        inventoryListState.value.copy(
                            startTransfer = false,
                            selectedCodes = emptyList(),
                        )

                }

            }
            return@withContext false
        }
    }

    suspend fun validateSelected(validate: Boolean, index: Int?) {
        return withContext(Dispatchers.IO) {
            inventoryListState.value =
                inventoryListState.value.copy(
                  loading = true
                )
            if (validate && index != null) {
                Log.e("start transfer access", index.toString())
                updateInventoryLocation(
                    inventoryListState.value.selectedCodes,
                    index.convertMiloID(),
                    (inventoryListState.value.currentLocation+1).convertMiloID()
                )
                inventoryListState.value =
                    inventoryListState.value.copy(
                        loading = false,
                        startTransfer = false,
                        selectedCodes = emptyList(),
                        showTransferPopup = false
                    )

                fetchLocationInventory(inventoryListState.value.currentLocation)

            }
            else{

                inventoryListState.value =
                    inventoryListState.value.copy(
                        loading = false,
                        startTransfer = false,
                        selectedCodes = emptyList(),
                        showTransferPopup = false
                    )
            }


        }
    }


    fun addRemoveCode(code: String, add: Boolean) {
        Log.e("addRemove", add.toString())
        val newDet = detectedInventoryCodes.value.toMutableList()
        if (add) {
            newDet.add(DetectedCodes(code = code, new = true))
            Log.e("new add", code)
        } else {
            newDet.removeIf { it.code == code }
        }
        detectedInventoryCodes.value = newDet.toSet()
        Log.e("new det", detectedInventoryCodes.value.toString())
    }

    fun goToCheckInventory() {
        viewModelScope.launch {
//            Log.e("finishing chekc", "fabino")
////            saveCheck()
            val allCodes = mutableSetOf<String>()
            for (codes in scannedLocationData.value.values) {
                allCodes.addAll(codes.detectedCodes.map { it.code }.toSet())
                allCodes.addAll(codes.inventoryList.toSet())
            }
            Log.e("all codes", allCodes.toString())
//
            imageLinks.value = getImagesFromCodes(allCodes.toList())
            showCheckScreen.value = true
        }
    }

    suspend fun fetchLocationInventory(locationID: Int) {

        inventoryListState.value = inventoryListState.value.copy(loading = true)
        inventoryListState.value =
            inventoryListState.value.copy(
                inventoryList = getInventoryForLocation(locationID + 1).toMutableList(),
                loading = false,
                currentLocation = locationID
            )
        val scannedData = scannedLocationData.value.getOrDefault(
            (locationID + 1).toString(), ScannedLocationData(
                mutableListOf(), mutableListOf(), false
            )
        )
        scannedData.inventoryList =
            inventoryListState.value.inventoryList.map { it.inventoryCode }.toMutableList()
        scannedLocationData.value[(locationID + 1).toString()] = scannedData



        Log.e("scanned loc data after fetch", scannedLocationData.value.toString())

        Log.e("scanned 1", scannedLocationData.value["1"].toString())
        detectedInventoryCodes =
            if (inventoryCheckState.value == InventoryCheckState.check_started) {
                mutableStateOf(scannedData.detectedCodes.toSet())
            } else {
                mutableStateOf(emptySet())
            }
        newInventoryList.value = emptySet()
        detectedRFID = mutableSetOf()
        previousRFID = mutableSetOf()
        suppressedCode = mutableSetOf()
    }

    fun continueInventoryCheck() {
        val scannedData = scannedLocationData.value.getOrDefault(
            (inventoryListState.value.currentLocation + 1).toString(), ScannedLocationData(
                mutableListOf(), mutableListOf(), false
            )
        )
        detectedInventoryCodes = mutableStateOf(scannedData.detectedCodes.toSet())
    }


    fun startTimer() {
        timer = Timer()

        detectedRFID = mutableSetOf()
        previousRFID = mutableSetOf()
        val expectedCodes = inventoryListState.value.inventoryList.map { it.inventoryCode }
        timer.schedule(object : TimerTask() {
            override fun run() {


                val newRFID = mutableSetOf<String>()
                newRFID.addAll(detectedRFID)
//                newRFID.removeAll(previousRFID)

                Log.e("new Codes", newInventoryList.value.map { it.inventoryCode }.toString())

                if (newRFID.isNotEmpty() and !busy) {
                    busy = true
                    var allTags = ""
                    for (newTag in newRFID) {
                        allTags += "'$newTag', "
                    }
                    allTags = allTags.substring(0, allTags.length - 2)
                    Log.e("new rfid", newRFID.toString())
                    val q = String.format(
                        "select * FROM StockHistory left JOIN Inventory ON StockHistory.InventoryCode = Inventory.InventoryCode" + " where Inventory.rfidTag in (%s) AND StockHistory.Action = 'New' Order By StockHistory.HistoryDate Desc",
                        allTags
                    )
                    val query = Query(q)
                    val s: Boolean = query.execute(Constants.url)
                    val newCodes =
                        query.res.stream().map { stringStringMap: Map<String, String> ->
                            stringStringMap["InventoryCode"] ?: ""
                        }.collect(Collectors.toList())
                    val newC = detectedInventoryCodes.value.map { it.code }.toMutableSet()
                    newC.addAll(newCodes)
                    detectedInventoryCodes.value = newC.filter { it !in suppressedCode }
                        .map { DetectedCodes(code = it, new = true) }.toSet()
//                    Log.e("new codes", query.res.toString());
                    newInventoryList.value = query.res.filter { map: Map<String, String> ->
                        map["InventoryCode"] !in expectedCodes
                    }.map {
                        Inventory(
                            name = it["Name"]!!,
                            inventoryCode = it["InventoryCode"]!!,
                            imageUrl = Constants.imageUrl + it["Image"]!!,
                            rfidTag = it["rfidTag"]!!,
                            idLocation = it["IDLocation"]!!
                        )
                    }.distinctBy { it.inventoryCode }
                        .filter { it.inventoryCode !in suppressedCode }
                        .toSet()

//                    newInventoryCodes = detectedInventoryCodes.value.toMutableSet()


//                    newInventoryCodes = detectedInventoryCodes.value.toMutableSet()
//                    newInventoryCodes.removeAll(expectedCodes.toSet())


                }



                previousRFID.addAll(detectedRFID)
                busy = false

//                        Log.e("all rfid", previousRFID.toString());
            }
        }, 0, 1000)
    }


    fun updateInventory(newTag: String) {
        //if we scan the entire location
        if (scanningLocation.value) {
            detectedRFID.add(newTag)
            Log.e("inventory", detectedRFID.toString())
        }
        //if we edit or add a tag to an item
        else {
            val listcopy = mutableListOf<Inventory>()
            listcopy.addAll(inventoryListState.value.inventoryList.toList())
            val inventory = inventoryListState.value.currentItem ?: return
            val index = listcopy.indexOf(inventory)
            Log.e("inventory passed", inventory.toString())
            Log.e("index", index.toString())
            if (index >= 0) {
                val item = listcopy.removeAt(index)
                listcopy.add(index, item.copy(rfidTag = newTag))
            }

//        inventoryListState.value = inventoryListState.value.copy(inventoryList = emptyList())
            inventoryListState.value =
                inventoryListState.value.copy(inventoryList = listcopy.toList())
            inventory.rfidTag = newTag
        }

    }

    private suspend fun getImagesFromCodes(codes: List<String>): Map<String, String> {
        return withContext(Dispatchers.IO) {
            var codes_string = ""
            if (codes.isNotEmpty()) {
                codes.forEach { codes_string += ("'$it', ") }
                codes_string =
                    codes_string.removeRange(codes_string.length - 2, codes_string.length)
                Log.e("ids", codes_string)
            }
            if (codes_string.isEmpty()) {
                return@withContext mapOf()
            }

            val query =
                "select InventoryCode, Image FROM Inventory WHERE InventoryCode in ($codes_string);"
            val q = Query(query)
            val s = q.execute(Constants.url)
//            Log.e("images links", q.res.associate { it["InventoryCode"] to it["Image"] }.toString())
//            q.res.map { it[""] }
            return@withContext q.res.associate { it["InventoryCode"]!! to it["Image"]!! }
        }
    }

    suspend fun updateInventoryItem(inventory: Inventory): Boolean {
        return withContext(Dispatchers.IO) {
            val query =
                "update Inventory SET rfidTag = '${inventory.rfidTag}' where InventoryCode = '${inventory.inventoryCode}'"
            val q = Query(query)
            val s = q.execute(Constants.url)
            Log.e("update rfid", s.toString())
            return@withContext s
        }
    }

    suspend fun getInventoryFromRfidTag(rfid_tag: String): Inventory? {
        return withContext(Dispatchers.IO) {
            //restrictive
//            val query =
//                "select * FROM StockHistory left JOIN Inventory ON StockHistory.InventoryCode = Inventory.InventoryCode" + " where Inventory.rfidTag = '${rfid_tag}' AND Inventory.Status = 'Stock' AND StockHistory.Action = 'New' Order By StockHistory.HistoryDate Desc"

            //less restrictive
            val query =
                "select * FROM StockHistory left JOIN Inventory ON StockHistory.InventoryCode = Inventory.InventoryCode" + " where Inventory.rfidTag = '${rfid_tag}' Order By StockHistory.HistoryDate Desc"
            val q = Query(query)
            val s = q.execute(Constants.url)
            if (!s) {
                return@withContext null
            }
            if (q.res.size == 0) {
                return@withContext null
            } else {
                val map = q.res[0]
                return@withContext Inventory(
                    name = map["Name"]!!,
                    inventoryCode = map["InventoryCode"]!!,
                    imageUrl = Constants.imageUrl + map["Image"]!!,
                    rfidTag = map["rfidTag"]!!,
                    idLocation = map["IDLocation"]!!
                )
            }
        }
    }

    fun updateCurrentItem(newItem: Inventory?) {
        inventoryListState.value = inventoryListState.value.copy(currentItem = newItem)
    }

    fun updateConnectionStatus(status: Boolean) {
        deviceConnected.value = status
    }

    fun Int.convertMiloID(): Int {
//        val newID =
//            inventoryListState.value.allLocations[this - 1].split(" ")[0]

        return this
    }

    suspend fun closeCheck() {
        withContext(Dispatchers.IO) {
            checkScreenLoading.value = true

            val status_list = ManagementApplication.getOptionValueList("InventoryStatus")
//            Log.e("status list", status_list.toString())
            val stat_data = mutableMapOf<String, List<String>>()
            for (status in status_list) {
                val inventory = StatisticsViewModel().getInventoryByStatus(status)
                stat_data.putIfAbsent(status, inventory)
            }
            val jsonStatistics = Gson().toJson(stat_data)
//            Log.e("current check index", index.toString())
//            Log.e("json", jsonStatistics)
            StatisticsViewModel().updateCurrentCheckStock(
                jsonStatistics,
                checkID = currentCheckID.value.toString()
            )
            checkScreenLoading.value = false
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    ManagementApplication.getAppContext(),
                    "check saved successfully",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private suspend fun getInventoryForLocationWithStockHistory(locationID: Int, withVerified: Boolean = false): List<Inventory> {
//        convertMiloID(locationID)
        return withContext(Dispatchers.IO) {
            val query =
                "select * FROM StockHistory left JOIN Inventory ON StockHistory.InventoryCode = Inventory.InventoryCode where IDLocation = ${locationID.convertMiloID()} and Status='Stock' order by Inventory.CatalogCode asc;"
            val q = Query(query)
            val s = q.execute(Constants.url)
            val res = mutableListOf<Inventory>()
            val filtered = filterLocationData(q.res, withVerified)
//            val catCodes = mutableListOf<String>()
            filtered.forEach { map ->
                val catCode = map["CatalogCode"]!!
//                if (catCode !in catCodes) {
//                    catCodes.add(catCode)
                val inv = Inventory(
                    name = map["Name"]!!,
                    inventoryCode = map["InventoryCode"]!!,
                    imageUrl = Constants.imageUrl + map["Image"]!!,
                    rfidTag = map["rfidTag"]!!,
                    idLocation = map["IDLocation"]!!
                )
                res.add(inv)
//                    Log.e("inventory", map["Image"]!!)
            }
//            }

            Log.e("location size: ", res.size.toString())
            return@withContext res
        }
    }


    private suspend fun getInventoryForLocation(locationID: Int): List<Inventory> {
        Log.e("location id man", locationID.convertMiloID().toString())
        return withContext(Dispatchers.IO) {
            val query =
                "select * FROM Inventory where CurrentLocationID = ${locationID.convertMiloID()} and Status='Stock' order by Inventory.CatalogCode asc;"
            val q = Query(query)
            val s = q.execute(Constants.url)
            val res = mutableListOf<Inventory>()
            q.res.forEach { map ->
//                val catCode = map["CatalogCode"]!!
//
                val inv = Inventory(
                    name = map["Name"]!!,
//                    catalogCode = catCode,
                    inventoryCode = map["InventoryCode"]!!,
                    imageUrl = Constants.imageUrl + map["Image"]!!,
                    rfidTag = map["rfidTag"]!!,
                    idLocation = map.getOrDefault("CurrentIDLocation", "")
                )
                res.add(inv)
            }
//            }

            Log.e("location size: ", res.size.toString())
            return@withContext res
        }
    }


    suspend fun getLocationItemNumber(locationID: Int): Int {
        return withContext(Dispatchers.IO) {
            val query =
                "select * FROM StockHistory left JOIN Inventory ON StockHistory.InventoryCode = Inventory.InventoryCode where IDLocation = ${locationID.convertMiloID()} and Status='Stock' order by Inventory.CatalogCode asc;"
            val q = Query(query)
            val s = q.execute(Constants.url)
            val res = mutableListOf<Inventory>()
            val filtered = filterLocationData(q.res)


            return@withContext filtered.size
        }
    }

    suspend fun getLocationItemNumber(): List<Int> {
        return withContext(Dispatchers.IO) {

            val query =
                "select ItemNumber FROM StockLocation ORDER BY ID"
            val q = Query(query)
            val s = q.execute(Constants.url)
            return@withContext q.res.map { it["ItemNumber"]?.toInt() ?: 0 }
        }
    }

    suspend fun updateLocationItemNumber(locationID: Int, itemNumber: Int) {
        return withContext(Dispatchers.IO) {
            val query =
                "update StockLocation set ItemNumber = $itemNumber WHERE ID=${locationID.convertMiloID()}"
            val q = Query(query)
            val s = q.execute(Constants.url)
        }
    }

    private suspend fun getLocationInfo() {
        return withContext(Dispatchers.IO) {
            val query =
                "select * FROM StockLocation order by ID"
            val q = Query(query)
            val s = q.execute(Constants.url)
            Log.e("nb loc", q.res[0].getOrDefault("0", "no val"))

            val locNumber = q.res.size
            Log.e("location number", locNumber.toString())
            val locationList = q.res.map { it.getOrDefault("PlaceNumber", "") }

            withContext(Dispatchers.Main) {

                inventoryListState.value =
                    inventoryListState.value.copy(
                        totalLocation = locNumber,
                        allLocations = locationList
                    )
            }

        }
    }

    suspend fun saveCheck() {
        return withContext(Dispatchers.IO) {
            if (currentCheckID.value > 0) {
                scannedLocationData.value =
                    scannedLocationData.value.filter { it.value.scanned }.toMutableMap()
//                Log.e("scannedFilterer", scannedFiltered.toString())
                val json = Gson().toJson(scannedLocationData.value)
////            val data = Gson().fromJson<MutableMap<String, ScannedLocationData>>(json,
////                object : TypeToken<Map<String, ScannedLocationData>>() {}.type)
////            Log.e("data before check", data.toString())
                val query =
                    "update InventoryChecks set Data = '$json' where ID = ${currentCheckID.value}"
                val q = Query(query)
                val s = q.execute(Constants.url)
                if (s) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            ManagementApplication.getAppContext(),
                            "check saved successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }


    suspend fun retrieveCurrentCheck(): Int {
        return withContext(Dispatchers.IO) {
            Log.e("current Date", getDate())

            val query =
                "select * FROM InventoryChecks where CheckDate = #${getDate()}# Order By CheckDate Desc"
            val q = Query(query)
            val s = q.execute(Constants.url)
            Log.e("hein ?", q.res.toString())

            if (q.res.size == 0) {
                return@withContext -1
            } else {
                val checkID = q.res[0].getOrDefault("ID", "-1").toInt()
                currentCheckID.value = checkID
                val data =
                    Gson().fromJson<MutableMap<String, ScannedLocationData>>(
                        q.res[0].getOrDefault(
                            "Data",
                            ""
                        ),
                        object : TypeToken<Map<String, ScannedLocationData>>() {}.type
                    )

                Log.e("data before check", data.toString())
                scannedLocationData.value = data

//                val query =
//                    "select * FROM StockHistory where InventoryCheckID = '$checkID' AND Action = 'Verified'"
//                val q = Query(query)
//                val s = q.execute(Constants.url)
//
//                Log.e("already verified", q.res.toString())
//                q.res.forEach {
//                    val idLoc = it["IDLocation"]!!
//                    val scannedData = scannedLocationData.value.getOrDefault(
//                        idLoc, ScannedLocationData(
//                            mutableListOf(), mutableListOf()
//                        )
//                    )
//                    it["InventoryCode"]?.let { it1 ->
//                        scannedData.detectedCodes.add(
//                            DetectedCodes(
//                                code = it1,
//                                new = false
//                            )
//                        )
//                    }
//                    Log.e("scanned", scannedData.toString())
//                    scannedLocationData.value[idLoc] = scannedData
//                }
//                Log.e("scanned updated", scannedLocationData.value.toString())

                return@withContext currentCheckID.value
            }

        }
    }
}
    }