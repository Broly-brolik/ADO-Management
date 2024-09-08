package com.example.aguadeoromanagement.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.components.Spinner
import com.example.aguadeoromanagement.states.DetectedCodes
import com.example.aguadeoromanagement.states.ScannedLocationData
import com.example.aguadeoromanagement.ui.theme.Shapes

//TODO simuler dupplication item

val columnHeight = 250.dp

@Composable
fun CheckInventoryScreen(
    scannedData: Map<String, ScannedLocationData>,
    imageLinks: Map<String, String>,
    updateLocation: () -> Unit,
    savingInventoryReport: Boolean,
    totalLocation: Int,
    locations: List<String>,
    goToProduct: (String) -> Unit,
    closeCheck: () -> Unit,
    loading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (savingInventoryReport) {
            CircularProgressIndicator()
            return@Column
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Location",
                modifier = Modifier.weight(1f),
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Initial",
                modifier = Modifier.weight(1f),
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Checked",
                modifier = Modifier.weight(1f),
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Miss",
                modifier = Modifier.weight(1f),
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {
            items(scannedData.keys.toList().sortedBy { it.toInt() }) {
                val idLocation = it
                val initialInventory = scannedData[idLocation]!!.inventoryList.toSet()
                val detectedCodes =
                    scannedData[idLocation]!!.detectedCodes.map { det -> det.code }.toSet()
                val foundCodes = initialInventory.intersect(detectedCodes)

                val lostCodes = initialInventory.minus(detectedCodes).toMutableList()
                val transferredCodes = mutableMapOf<String, String>()
                for (lostCode in lostCodes) {
                    for (location in scannedData.keys) {
                        if (location == idLocation) {
                            continue
                        }
                        if (lostCode in scannedData[location]!!.detectedCodes.map { det -> det.code }) {
                            lostCodes.remove(lostCode)
                            transferredCodes[lostCode] = location
                        }
                    }
                }

                val newCodes = detectedCodes.minus(initialInventory).toMutableList()
                val newCodesMap = mutableMapOf<String, String>()
                for (newCode in newCodes) {
                    newCodesMap[newCode] = ""
                    for (location in scannedData.keys) {
                        if (location == idLocation) {
                            continue
                        }
                        if (newCode in scannedData[location]!!.inventoryList) {
                            newCodesMap[newCode] = location
                        }
                    }
                }




                LocationSummary(
                    idLocation = it,
                    initialInventory = initialInventory.toList(),
                    detectedCodes = detectedCodes.toList(),
                    foundItems = foundCodes.toList(),
                    lostCodes = lostCodes.toList(),
                    imageLinks = imageLinks,
                    totalLocation = totalLocation,
                    scannedData = scannedData,
                    allLocations = locations,
                    goToProduct = { code: String -> goToProduct(code) }
                )
            }
            item {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp), horizontalArrangement = Arrangement.Center
                ) {
                    if (!loading) {
                        Button(onClick = { closeCheck() }) {
                            Text("Validate & Close Check", fontSize = 24.sp)
                        }
                    } else {
                        Text("Updating...")
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

}

@Composable
fun LocationSummary(
    idLocation: String,
    initialInventory: List<String>,
    detectedCodes: List<String>,
    foundItems: List<String>,
    lostCodes: List<String>,
    imageLinks: Map<String, String>,
    totalLocation: Int,
    scannedData: Map<String, ScannedLocationData>,
    allLocations: List<String>,
    goToProduct: (String) -> Unit
) {

    val missing_size = initialInventory.size - foundItems.size

    Column {
        Divider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                allLocations[idLocation.toInt() - 1],
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Text(
                initialInventory.size.toString(),
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Text(
                foundItems.size.toString(),
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Text(
                missing_size.toString(),
                modifier = Modifier.weight(1f),
                fontSize = 20.sp, textAlign = TextAlign.Center
            )
        }
        if (missing_size > 0) {
            Column(Modifier.heightIn(0.dp, 200.dp)) {

                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 128.dp)) {
                    items(lostCodes) {
                        Button(onClick = { goToProduct(it) }) {
                            Text(it)
                        }
                    }
                }


            }
        }
        Divider()
    }

//    Column(
//        Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//            .border(2.dp, color = Color.Gray, shape = Shapes.medium)
//            .padding(8.dp)
//    ) {
//
//
//        Column(Modifier.padding(bottom = 8.dp)) {
//            Text("Location: $idLocation - scanned ${detectedCodes.size} items", fontSize = 24.sp)
//            Divider(color = Color.Black, thickness = 2.dp)
//        }
//
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(16.dp),
//        ) {
//            Column(
////                modifier = Modifier.weight(0.5f),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text("Found", fontSize = 20.sp)
//                Column(Modifier.width(IntrinsicSize.Min)) {
//                    Text("${foundItems.size}", fontSize = 24.sp)
//                    Divider(thickness = 5.dp, color = Color.Black)
//                    Text("${initialInventory.size}", fontSize = 24.sp)
//                }
//            }
//
//
//            //lost items
//            Column(
//                Modifier
//                    .weight(1f),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text("Lost items (${lostCodes.size})", fontSize = 20.sp)
//                LazyColumn(
//                    Modifier.heightIn(0.dp, columnHeight),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    items(lostCodes) {code->
//                        var currentLocation by remember {
//                            mutableStateOf(-1)
//                        }
//
//                        Row() {
//
//                            AsyncImage(
//                                Constants.imageUrl + imageLinks[code],
//                                contentDescription = "",
////                            modifier = Modifier.width(columnHeight)
//                            )
//                            Column() {
//                                Text(code)
//                                Row() {
//                                    Text("Found in ?")
//                                    Spinner(
//                                        entries = (1..totalLocation).toList().map { it.toString() },
//                                        index = currentLocation,
//                                        label = "Location",
//                                        onValueChange = { newVal: Int -> currentLocation = newVal
//
//                                            //TODO here
//                                            var newDet = scannedData[newVal.toString()]?.detectedCodes?.toMutableList()
//                                            if (newDet == null) {
//                                                newDet = mutableListOf()
//                                            }
//                                            newDet.add(DetectedCodes(code, true))
//
//
//                                        }
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}