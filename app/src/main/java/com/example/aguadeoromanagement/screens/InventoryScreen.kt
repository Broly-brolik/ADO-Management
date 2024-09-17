package com.example.aguadeoromanagement.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.components.Spinner
import com.example.aguadeoromanagement.enums.InventoryCheckState
import com.example.aguadeoromanagement.enums.rfid_state
import com.example.aguadeoromanagement.models.Inventory
import com.example.aguadeoromanagement.states.DetectedCodes
import com.example.aguadeoromanagement.states.InventoryListState
import com.example.aguadeoromanagement.ui.theme.MainGreen
import com.example.aguadeoromanagement.ui.theme.Shapes
import com.example.aguadeoromanagement.viewmodels.InventoryViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryScreen(
    inventoryState: InventoryListState,
//    inventoryList: List<Inventory>,
//    newItems: Set<Inventory> = emptySet<Inventory>(),
//    loading: Boolean,
    deviceConnected: Boolean,
//    currentItem: Inventory?,
//    currentLocation: Int,
//    totalLocation: Int,
//    locationList: List<String>,
    scanTag: (inventory: Inventory?) -> Unit,
    updateInventoryItem: suspend (inventory: Inventory) -> Boolean,
    getInventoryFromRfidTag: suspend (tag: String) -> Inventory?,
    fetchNewLocation: (newVal: Int) -> Unit,
    scanningLocation: Boolean,
    detectedCodes: Set<DetectedCodes>,
    removeNewItem: (code: String, tag: String) -> Unit,
    newInventoryList: Set<Inventory>,
    updateLocation: (ok: Boolean) -> Unit,
    showPopup: Boolean,
    finishLocation: () -> Unit,
    insertingItem: Boolean,
    inventoryCheckState: InventoryCheckState,
    addRemoveInventory: (code: String, add: Boolean) -> Unit,
    showPopupSummaryLocation: Boolean,
    locationItemNumber: List<Int>,
    dismissSummary: () -> Unit,
    navigateToProduct: (String) -> Unit,
    showMissingLocation: Boolean,
    changeShowMissing: () -> Unit,
    addRemoveSelected: (String, Boolean) -> Unit,
    validateSelected: (Boolean, Int?) -> Unit

) {

    val inventoryCodes = inventoryState.inventoryList.map { it.inventoryCode }.toSet()

    val newDetectedCodes = detectedCodes.filter { it.new }.map { it.code }.toSet()
    val alreadyDetectedCodes = detectedCodes.filter { !it.new }.map { it.code }.toSet()

    val allDetectedCodes = detectedCodes.map { it.code }

    val detected = allDetectedCodes.intersect(inventoryCodes.toSet())
    val new = newDetectedCodes.toMutableSet()
    new.removeAll(inventoryCodes)

//    var selectedCodes = remember { mutableStateListOf<String>() }


    if (showPopup) {
        Popup() {
            Box(
                modifier = Modifier
                    .background(color = Color.Black.copy(alpha = 0.8f), shape = Shapes.medium)
                    .fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .background(color = Color.White)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Are you sure to update the location ?")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(onClick = { updateLocation(false) }) {
                            Text("No")
                        }
                        Button(onClick = { updateLocation(true) }) {
                            Text("Yes")
                        }
                    }
                }

            }
        }
    }
    if (showPopupSummaryLocation) {
        Popup() {
            Box(
                modifier = Modifier
                    .background(color = Color.Black.copy(alpha = 0.8f), shape = Shapes.medium)
                    .fillMaxSize()
                    .clickable { dismissSummary() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .background(color = Color.White)
                        .padding(16.dp)
                        .height(400.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LazyVerticalGrid(columns = GridCells.Fixed(5), content = {
                        itemsIndexed(locationItemNumber) { index, itemNumber ->
                            Text(
                                "Location ${index + 1}: $itemNumber items",
                                Modifier.padding(16.dp)
                            )
                        }
                    })
                }

            }
        }
    }
    if (inventoryState.showTransferPopup) {

        var index by remember {
            mutableIntStateOf(0)
        }
        Popup {
            Box(
                modifier = Modifier
                    .background(color = Color.Black.copy(alpha = 0.8f), shape = Shapes.medium)
                    .fillMaxSize()
                    .clickable { dismissSummary() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .background(color = Color.White)
                        .padding(16.dp)
                        .height(400.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select destination location")
                    Spinner(
                        entries = inventoryState.allLocations,
                        index = index,
                        label = "Location",
                        onValueChange = { newVal: Int -> index = newVal }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(onClick = { validateSelected(false, null) }) {
                            Text("No")
                        }
                        Button(onClick = { validateSelected(true, index + 1) }) {
                            Text("Yes")
                        }
                    }
                }

            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp), contentAlignment = Alignment.Center
    ) {
        if (inventoryState.loading || insertingItem) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                if (insertingItem) {
                    Text("updating location...")
                }
            }
            return@Box
        }
        Column(modifier = Modifier.fillMaxHeight()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!showMissingLocation) {
                        Text("Location: ")
                        Column {
                            Log.d("myTag", inventoryState.currentLocation.toString())
                            Spinner(
                                entries = inventoryState.allLocations,
                                index = inventoryState.currentLocation,
                                label = "Location",
                                onValueChange = { newVal: Int -> fetchNewLocation(newVal) }
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(onClick = {
                                    if (inventoryState.currentLocation > 0) {
                                        fetchNewLocation(inventoryState.currentLocation - 1)
                                    }
                                }
                                ) {
                                    Text("Prev")
                                }
                                Button(onClick = {
                                    if (inventoryState.currentLocation < inventoryState.allLocations.size - 1) {
                                        fetchNewLocation(inventoryState.currentLocation + 1)
                                    }
                                }
                                ) {
                                    Text("Next")
                                }


                            }
                        }
                    }
                    Button(onClick = {
                        changeShowMissing()
                    }) {
                        Text(if (!showMissingLocation) "Show missing" else "Show Location")
                    }

                }
                Column() {
//                    Text(if (deviceConnected) "RFID reader connected" else "RFID not connected")
//                    Text("current item: $currentItem")
//                    Text(scanningLocation.toString())
                    Text("Scanned in location/total: ${detected.size}/${inventoryState.inventoryList.size}")
                    Text("new items: ${newInventoryList.size}")
                    Text("Total scanned: ${detected.size + newInventoryList.size}")
                }
            }
            Row() {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(
                        inventoryState.inventoryList.sortedBy { it.inventoryCode },
                        key = { index, inv -> inv.inventoryCode }) { index, it ->
                        val iniState =
                            if (it.rfidTag.isEmpty()) rfid_state.ADD_TAG else rfid_state.EDIT_TAG
                        var tagState by remember {
                            mutableStateOf(iniState)
                        }
                        val button_text = when (tagState) {
                            rfid_state.ADD_TAG -> "Add RFID tag"
                            rfid_state.ADDING_TAG -> "Validate RFID tag"
                            rfid_state.EDIT_TAG -> "Edit RFID tag"
                            rfid_state.UPDATING_DATABASE -> "Updating database..."
                        }
                        val scope = rememberCoroutineScope()

                        val color =
                            if (it.inventoryCode in allDetectedCodes || it.inventoryCode in inventoryState.selectedCodes) Color.Green else Color.Transparent

                        Text((index + 1).toString(), fontSize = 18.sp)
                        Row(
                            modifier = Modifier
                                .background(color = color)
                                .fillMaxWidth(0.4f)
                        ) {
                            AsyncImage(
                                it.imageUrl,
                                contentDescription = "",
                                modifier = Modifier
                                    .width(200.dp)
                                    .combinedClickable(
                                        enabled = true,
                                        onClick = {},
                                        onLongClick = { navigateToProduct(it.inventoryCode) })
                            )
                            Column() {
                                if (it.inventoryCode in newDetectedCodes && inventoryCheckState != InventoryCheckState.check_started) {
                                    Button(onClick = {
                                        removeNewItem(
                                            it.inventoryCode,
                                            it.rfidTag
                                        )
                                    }) {
                                        Text("Unselect item")
                                    }
                                }
                                if (it.name.isNotEmpty()) {
                                    Text(it.name)
                                }
                                Text("inventory code: ${it.inventoryCode}")
//                        Column() {
                                if (inventoryState.startTransfer) {
                                    val selected = it.inventoryCode in inventoryState.selectedCodes
                                    Button(onClick = {
                                        if (!selected) {
                                            addRemoveSelected(it.inventoryCode, false)
                                        } else {
                                            addRemoveSelected(it.inventoryCode, true)
                                        }
                                    }) {
                                        Text(if (selected) "Unselect" else "Select")
                                    }
                                }
                                if (deviceConnected) {
                                    if ((inventoryState.currentItem == null) || (inventoryState.currentItem == it)) {
                                        Button(onClick = {
                                            if (tagState == rfid_state.ADDING_TAG) {
                                                if (it.rfidTag.isEmpty()) {
                                                    scanTag(null)
                                                    tagState = rfid_state.ADD_TAG
                                                } else {
                                                    tagState = rfid_state.UPDATING_DATABASE
                                                    scope.launch {

                                                        val otherItem =
                                                            getInventoryFromRfidTag(it.rfidTag)
                                                        if (otherItem != null) {
                                                            Toast.makeText(
                                                                ManagementApplication.getAppContext(),
                                                                "The tag already corresponds to the article with inventory code: ${otherItem.inventoryCode} located in location ${otherItem.idLocation}.",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            tagState = rfid_state.EDIT_TAG
                                                            scanTag(null)

                                                            return@launch
                                                        }


                                                        val ok = updateInventoryItem(it)
                                                        if (ok) {
                                                            Toast.makeText(
                                                                ManagementApplication.getAppContext(),
                                                                "update successful",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            tagState = rfid_state.EDIT_TAG
                                                            scanTag(null)
                                                        } else {
                                                            tagState = rfid_state.ADDING_TAG
                                                            Toast.makeText(
                                                                ManagementApplication.getAppContext(),
                                                                "error in update",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    }
                                                    //                                                {
//                                                    scanTag(null)
//                                                    tagState = rfid_state.EDIT_TAG
//
//                                                }
                                                }
//                                            tagState = if (it.rfidTag.isEmpty()) {
//                                                rfid_state.ADD_TAG
//                                            } else {
//                                                rfid_state.EDIT_TAG
//                                            }

                                            } else if (tagState == rfid_state.EDIT_TAG || tagState == rfid_state.ADD_TAG) {
                                                tagState = rfid_state.ADDING_TAG
                                                scanTag(it)
                                            }
                                        }) {
                                            Text(button_text)
                                        }
                                    }
//                            }
                                    Text(it.rfidTag.ifEmpty { "no tag" })
                                }

                                if (inventoryCheckState == InventoryCheckState.check_started && it.inventoryCode !in alreadyDetectedCodes) {
                                    val text =
                                        if (it.inventoryCode in newDetectedCodes) "Remove detected" else "Detected"
                                    val color =
                                        if (it.inventoryCode !in newDetectedCodes) MainGreen else Color.Red
                                    Button(
                                        onClick = {
                                            addRemoveInventory(
                                                it.inventoryCode,
                                                it.inventoryCode !in newDetectedCodes
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(backgroundColor = color)
                                    ) {
                                        Text(text)
                                    }
                                }


                            }
                        }
                    }
                }
                LazyColumn() {
                    items(newInventoryList.toList().sortedBy { it.inventoryCode }) {

                        val scope = rememberCoroutineScope()


                        Row(
//                            modifier = Modifier.fillMaxWidth(0.4f)
                        ) {
                            AsyncImage(
                                it.imageUrl,
                                contentDescription = "",
                                modifier = Modifier.width(200.dp)
                            )
                            Column() {
                                if (it.name.isNotEmpty()) {
                                    Text(it.name)
                                }

                                Text("inventory code: ${it.inventoryCode}")
                                Button(onClick = { removeNewItem(it.inventoryCode, it.rfidTag) }) {
                                    Text("remove")
                                }

                            }
                        }
                    }
                }
            }

        }
    }
}