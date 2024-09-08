package com.example.aguadeoromanagement.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.InventoryCheck
import com.example.aguadeoromanagement.states.ScannedLocationData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatisticsScreen(
    loading: Boolean,
//    statistics: Map<String, List<String>>,
//    inventoryData: Map<String, ScannedLocationData>,
    inventoryChecks: List<InventoryCheck>,
    retrieveCodeInfo: (List<String>) -> Unit,
    images: Map<String, String>
) {

    var showMissing by remember {
        mutableStateOf(false)
    }
    var showNotFound by remember {
        mutableStateOf(false)
    }
    var inventoryData by remember {
        mutableStateOf(mapOf<String, ScannedLocationData>())
    }

    var checksToCompare by remember {
        mutableStateOf(listOf<InventoryCheck>())
    }
    var selectForComparison by remember {
        mutableStateOf(false)
    }

    var showComparisonScreen by remember {
        mutableStateOf(false)
    }

    //comparison screen
    if (showComparisonScreen) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column() {
                val sold1 = checksToCompare[0].stockSnapshot["Vendu"]!!.toSet()
                val sold2 = checksToCompare[1].stockSnapshot["Vendu"]!!.toSet()
                val diff = sold2.subtract(sold1)
                val diff2 = sold1.subtract(sold2)
                Text(sold1.size.toString())
                Text(sold2.size.toString())

                Text(diff.toString())
                Text(diff2.toString())
            }
        }
    }


    // main screen
    if (!showComparisonScreen) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    if (selectForComparison && checksToCompare.size == 2) {
                        Button(onClick = { showComparisonScreen = true}) {
                            Text("Compare")
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(inventoryChecks) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp), modifier =
                                Modifier
                                    .background(color = if (it in checksToCompare) Color.Green else Color.Transparent)
                                    .combinedClickable(
                                        onClick = {
                                            if (selectForComparison) {
                                                if (it in checksToCompare) {
                                                    val newList = checksToCompare.toMutableList()
                                                    newList.remove(it)
                                                    checksToCompare = newList
                                                    if (checksToCompare.isEmpty()) {
                                                        selectForComparison = false
                                                    }
                                                } else {
                                                    val newList = checksToCompare.toMutableList()
                                                    newList.add(it)
                                                    checksToCompare = newList
                                                }
                                            }
                                        },
                                        onLongClick = {
                                            if (!selectForComparison) {
                                                selectForComparison = true
                                                val newList = checksToCompare.toMutableList()
                                                newList.add(it)
                                                checksToCompare = newList
                                            }
                                        },
                                    )
                            ) {
                                val allDetectedCodes =
                                    it.inventoryData.flatMap { it.value.detectedCodes }
                                        .map { it.code }
                                val allStockCodes = it.stockSnapshot["Stock"]?.toMutableSet()
                                val notInStockCode = allStockCodes?.minus(allDetectedCodes.toSet())
                                var showMissing by remember {
                                    mutableStateOf(false)
                                }
                                Text(
//                                LocalDateTime.parse(it.checkDate, Constants.accessFormaterDateTime).format(Constants.forUsFormatter),
                                    it.checkDate.replace("00:00:00", ""),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                for (key in it.stockSnapshot.keys) {
                                    if (key == "Stock") {
                                        Row() {
                                            Text(
                                                "$key: ${it.stockSnapshot[key]!!.size}",
                                                fontSize = 16.sp
                                            )
                                            Text(" - ")
                                            Text(notInStockCode?.size.toString(), color = Color.Red)
//                                        Button(onClick = {
//                                            showMissing = !showMissing
//                                            if (showMissing) retrieveCodeInfo(notInStockCode!!.toList())
//                                        }) {
//                                            Text(if (showMissing) "-" else "+")
//                                        }
                                        }
//                                    if (showMissing) {
////                                Text(notInStockCode.toString())
////                                Text(images.toString())
//

                                    } else {
                                        Text("$key: ${it.stockSnapshot[key]!!.size}")
                                    }
                                }
                                Button(onClick = {
                                    retrieveCodeInfo(notInStockCode!!.toList())
                                    showMissing = true
                                    showNotFound = false
                                }) {
                                    Text("Show not in location")
                                }
                                Button(onClick = {
                                    showMissing = false
                                    showNotFound = true
                                    inventoryData = it.inventoryData
                                }) {
                                    Text("Show not found")
                                }
                            }
                        }
                    }

                    if (showNotFound) {
                        Column(Modifier.weight(1f)) {
                            for (key in inventoryData.keys) {
                                val scannedLocationData = inventoryData[key]!!
                                val detectedCodes =
                                    scannedLocationData.detectedCodes.map { it.code }.toSet()
                                val inventory = scannedLocationData.inventoryList.toSet()
                                var missing = inventory.minus(detectedCodes)
//                        Text("$key - ${detectedCodes} - ${inventory}")

                                if (missing.isEmpty()) {
                                    continue
                                } else {
                                    Text("$key -  ${missing}")
                                }

                            }
                        }
                    }


//                if (showMissing) {
//                    Text("hey")
                    Column(Modifier.weight(1f)) {


                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(images.keys.toList()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .border(2.dp, color = Color.Black)
                                        .padding(8.dp)
                                ) {
                                    AsyncImage(
                                        Constants.imageUrl + images.getOrDefault(
                                            it,
                                            ""
                                        ), contentDescription = ""
                                    )
                                    Text(it)
                                }
                            }
                        }
                    }
//                }
                }
//
            }
        }
    }
}