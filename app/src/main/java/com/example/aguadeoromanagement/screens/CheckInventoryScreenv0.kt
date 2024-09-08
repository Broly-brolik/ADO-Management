//package com.example.aguadeoromanagement.screens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.Button
//import androidx.compose.material.CircularProgressIndicator
//import androidx.compose.material.Divider
//import androidx.compose.material.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
//import com.example.aguadeoromanagement.Constants
//import com.example.aguadeoromanagement.states.ScannedLocationData
//import com.example.aguadeoromanagement.ui.theme.Shapes
//
////TODO simuler dupplication item
//
//
//@Composable
//fun CheckInventoryScreenV0(
//    scannedData: MutableMap<String, ScannedLocationData>,
//    imageLinks: Map<String, String>,
//    updateLocation: () -> Unit,
//    savingInventoryReport: Boolean
//) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        if (savingInventoryReport) {
//            CircularProgressIndicator()
//            return@Column
//        }
//        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
//            items(scannedData.keys.toList()) {
//                val idLocation = it
//                val initialInventory = scannedData[idLocation]!!.inventoryList.toSet()
//                val detectedCodes = scannedData[idLocation]!!.detectedCodes.toSet()
//                val foundCodes = initialInventory.intersect(detectedCodes)
//
//                val lostCodes = initialInventory.minus(detectedCodes).toMutableList()
//                val transferredCodes = mutableMapOf<String, String>()
//                for (lostCode in lostCodes) {
//                    for (location in scannedData.keys) {
//                        if (location == idLocation) {
//                            continue
//                        }
//                        if (lostCode in scannedData[location]!!.detectedCodes) {
//                            lostCodes.remove(lostCode)
////                            transferredCodes[lostCode] = location
//                        }
//                    }
//                }
//
//                val newCodes = detectedCodes.minus(initialInventory).toMutableList()
//                val newCodesMap = mutableMapOf<String, String>()
//                for (newCode in newCodes) {
//                    newCodesMap[newCode] = ""
//                    for (location in scannedData.keys) {
//                        if (location == idLocation) {
//                            continue
//                        }
//                        if (newCode in scannedData[location]!!.inventoryList) {
//                            newCodesMap[newCode] = location
//                        }
//                    }
//                }
//
//                LocationSummaryV0(
//                    idLocation = it,
//                    initialInventory = initialInventory.toList(),
//                    detectedCodes = detectedCodes.toList(),
//                    foundItems = foundCodes.toList(),
//                    lostCodes = lostCodes.toList(),
//                    transferredCodes = transferredCodes,
//                    newItems = newCodesMap,
//                    imageLinks = imageLinks
//                )
//            }
//        }
//        Button(onClick = { updateLocation() }) {
//            Text("Validate Check")
//        }
//    }
//
//}
//
//@Composable
//fun LocationSummaryV0(
//    idLocation: String,
//    initialInventory: List<String>,
//    detectedCodes: List<String>,
//    foundItems: List<String>,
//    lostCodes: List<String>,
//    transferredCodes: Map<String, String>,
//    newItems: Map<String, String>,
//    imageLinks: Map<String, String>
////
//) {
//
//
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
//            //new items
//            Column(
//                Modifier
//                    .weight(1f),
//                horizontalAlignment = Alignment.CenterHorizontally
//
//
//            ) {
//                Text("New items (${newItems.size})", fontSize = 20.sp)
//                LazyColumn(
//                    Modifier.heightIn(0.dp, columnHeight),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    items(newItems.keys.toList()) {
//                        Text(it, fontSize = 18.sp)
//                        Text("Previously in loc: ${newItems[it]}")
//                        AsyncImage(
//                            Constants.imageUrl + imageLinks[it],
//                            contentDescription = "",
////                            modifier = Modifier.width(columnHeight)
//                        )
//                    }
//                }
//            }
//
//
//            //transferred items
//            Column(
//                Modifier
//                    .weight(1f),
//                horizontalAlignment = Alignment.CenterHorizontally
//
//
//            ) {
//                Text("Transferred items (${transferredCodes.size})", fontSize = 20.sp)
//                LazyColumn(
//                    Modifier.heightIn(0.dp, columnHeight),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    items(transferredCodes.keys.toList()) {
//                        Text(it, fontSize = 18.sp)
//                        Text("Transferred to location ${transferredCodes[it]}")
//                        AsyncImage(
//                            Constants.imageUrl + imageLinks[it],
//                            contentDescription = "",
////                            modifier = Modifier.width(columnHeight)
//                        )
//                    }
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
//                    items(lostCodes) {
//                        Text(it)
//                        AsyncImage(
//                            Constants.imageUrl + imageLinks[it],
//                            contentDescription = "",
////                            modifier = Modifier.width(columnHeight)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}