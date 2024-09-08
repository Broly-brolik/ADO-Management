package com.example.aguadeoromanagement.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.components.Spinner
import com.example.aguadeoromanagement.models.Inventory
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties

@Composable
fun ProductDetailsScreen(
    currentCode: String,
    changeCurrentCode: (String) -> Unit,
    allCodes: List<String>,
    onCodeSelected: () -> Unit,
    inventory: Inventory? = null,
    productHistory: List<Map<String, String>>
) {

    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {

        if (inventory == null) {
            SelectCode(
                currentCode = currentCode,
                onChange = { newCode: String -> changeCurrentCode(newCode) },
                allCodes = allCodes,
                onSelect = onCodeSelected
            )
//            return@Box
        } else {
            ProductDetails(inventory = inventory, productHistory = productHistory)
//            return@Box
        }
    }

}

@Composable
fun SelectCode(
    currentCode: String,
    onChange: (String) -> Unit,
    allCodes: List<String>,
    onSelect: () -> Unit
) {
    Column() {
        TextField(
            value = currentCode,
            onValueChange = { newVal: String -> onChange(newVal) },
            placeholder = {
                Text(
                    text = "Inventory code"
                )
            })
        if (currentCode.length > 1) {
            LazyColumn() {
                items(allCodes.filter { it.contains(currentCode) }) {
                    Text(it, modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            onChange(it)
                            onSelect()
                        })
                }
            }
        }
    }
}

@Composable
fun ProductDetails(inventory: Inventory, productHistory: List<Map<String, String>>) {
    var showChangeStatus by remember { mutableStateOf(false) }
    var newStatusIndex by remember {
        mutableStateOf(0)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .weight(2f)
        ) {
            Text(
                inventory.inventoryCode,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column() {
                    AsyncImage(
                        inventory.imageUrl,
                        contentDescription = "",
                        modifier = Modifier.width(300.dp)
                    )
                }
                Column() {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Status", fontSize = 20.sp)
                        Text(inventory.status, fontSize = 20.sp)
                    }
                    Row() {

                        if (!showChangeStatus) {
                            Button(onClick = { showChangeStatus = !showChangeStatus }) {
                                Text("Change status")
                            }
                        } else {
                            Button(onClick = { Log.e("validate", "") }) {
                                Text("Validate status")
                            }
                        }
                        if (showChangeStatus) {
                            Spinner(
                                entries = ManagementApplication.getAppOptionValues().filter { it.type == "InventoryAction" }.map{it.optionValue},
                                index = newStatusIndex,
                                label = "New status",
                                onValueChange = { newVal -> newStatusIndex = newVal })
                        }
                    }
                }
            }

            val memberList = Inventory::class.memberProperties.filter {
                it.name !in listOf(
                    "imageUrl",
                    "inventoryCode",
                    "status"
                )
            }.map { prop ->
                val value =
                    Inventory::class.declaredMemberProperties.find { it.name == prop.name }!!
                        .get(inventory)
                "${prop.name} - $value"
            }.toList()
//                          
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(memberList) {
                    Text(it)
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text("Summary", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(productHistory) {
                    val status = it.getOrDefault("Action", "undefined")
                    val location = it.getOrDefault("IDLocation", "undefined")
                    val date = it.getOrDefault("HistoryDate", "")

                    var content = ""
                    if (status == "New") {
                        content = "Product arrived in location $location."
                    } else if (status == "Transferred") {
                        content = "Product moved from location $location."
                    } else if (status == "Verified") {
                        content = "Product verified at location $location."
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(date, fontWeight = FontWeight.Bold)
                        Text(content)

                    }
                }
            }
        }
    }

}