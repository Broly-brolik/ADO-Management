package com.example.aguadeoromanagement.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aguadeoromanagement.models.Product
import com.example.aguadeoromanagement.viewmodels.AdoStonesViewModel
import java.text.SimpleDateFormat
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import com.example.aguadeoromanagement.components.Spinner
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun AdoStonesScreen(
    viewModel: AdoStonesViewModel
) {
    // Flag to toggle between product list and create screen
    val isCreatingNewProduct = remember { mutableStateOf(false) }

    if (isCreatingNewProduct.value) {
        // Show the create new product screen
        CreateNewProduct(
            viewModel = viewModel,
            onProductCreated = { isCreatingNewProduct.value = false }
        )
    } else {
        // Existing UI for search and product list
        val productId = remember { mutableStateOf("") }
        val products = viewModel.products.collectAsState(initial = emptyList())
        val isSearchTriggered = remember { mutableStateOf(false) }

        LaunchedEffect(isSearchTriggered.value) {
            if (isSearchTriggered.value) {
                viewModel.searchProductById(productId.value)
                isSearchTriggered.value = false
            }
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            TextField(
                value = productId.value,
                onValueChange = { productId.value = it },
                label = { Text("Enter Product ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    isSearchTriggered.value = true
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Search")
            }

            Button(
                onClick = {
                    // Switch to create product mode
                    isCreatingNewProduct.value = true
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Create New Product")
            }

            if (products.value.isEmpty()) {
                Text(
                    text = "No product found.",
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    items(products.value) { product ->
                        ProductItem(product)
                    }
                }
            }
        }
    }
}


@Composable
fun ProductItem(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "ID: ${product.id}")
        Text(text = "Product Code: ${product.productCode}")
        Text(text = "Description: ${product.description}")
        Text(text = "Unit: ${product.unit}")
        Text(text = "Category: ${product.category}")
        Text(text = "SubCategory: ${product.subCategory}")
        Text(text = "Type: ${product.type}")
        Text(text = "Entry Date: ${product.entryDate}")
        Text(text = "Line: ${product.line}")
        Text(text = "Color ADO: ${product.colorAdo}")
        Text(text = "Quantity: ${product.quantity}")
        // + image
        Log.e("zzz", product.image)
        /*if (product.image.isNotEmpty()) {
            val decodedString = Base64.decode(product.image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Product Image")
        }*/
    }
}


@Composable
fun CreateNewProduct(
    viewModel: AdoStonesViewModel,
    onProductCreated: () -> Unit
) {
    // Initialize all the fields as empty.
    val productCode = remember { mutableStateOf("") }
    val orderTypeOptions = listOf("LossOrders", "FinishedOrders")
    val selectedOrderTypeIndex = remember { mutableIntStateOf(0) }
    val shortCode = remember { mutableStateOf("") }
    val size = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val unitOptions = listOf("Pieces", "Gramms")
    val selectedUnitIndex = remember { mutableIntStateOf(0) }
    val categoryOptions = listOf("Stones", "Jewellery", "Labor")
    val selectedCategoryIndex = remember { mutableIntStateOf(0) }
    val subCategoryOptions = listOf(
        "Round Melee", "Round", "Oval", "Princess", "Marquise", "Baguette", "Tapers", "Trillion",
        "Pear Shapes", "Drops", "Cushion", "Octagone", "Antique", "Cabochon", "Emerald",
        "Cabochon Marquise", "Cabochon Round", "Rectangle Flat", "Cabochon Rectangle", "Heart",
        "Cabochon Antique", "Cabochon Pear Shapes", "Mosaic Antique", "Oval Drill",
        "Cabochon Drop Drill", "Oval flat", "Rectangular", "triangle", "HalfCabouchon",
        "Octagone plat", "CVD", "Checkerbox square", "HPHT", "Earrings", "Rings", "Necklace",
        "Bracelets", "Rose Pear", "Chain", "Pearls", "Sugar loaf", "Fancy", "Metals", "Aascher",
        "Slices", "Ball", "Beads", "Radiant", "Ecrou", "Labor cost"
    )
    val selectedSubCategoryIndex = remember { mutableIntStateOf(0) }
    val productTypeOptions = listOf("AU 750", "Color", "Diam", "Diam cognac", "Diam noir", "Pearls", "PT 950", "AG 925", "CZ", "Diam Green")
    val selectedTypeIndex = remember { mutableIntStateOf(0) }
    val entryDate = remember { mutableStateOf("") }

    val productLineOptions = listOf("Line", "Linea Verde", "Mined", "Max H", "PX Impact", "Standard")
    val selectedLineIndex = remember { mutableIntStateOf(0) }

    val colorAdoOptions = listOf(
        "B", "B1", "B10", "B11", "B12", "B13", "B14", "B15", "B16", "B17", "B18", "B19",
        "B2", "B20", "B21", "B22", "B23", "B24", "B25", "B26", "B28", "B29", "B3",
        "B30", "B31", "B32", "B33", "B4", "B5", "B6", "B7", "B8", "B9", "CZ",
        "G1", "G10", "G11", "G12", "G13", "G14", "G15", "G16", "G17", "G18", "G19",
        "G2", "G20", "G21", "G3", "G4", "G5", "G6", "G7", "G8", "G9", "K1", "K2", "K3",
        "K4", "K5", "K6", "K7", "O", "O1", "O2", "O3", "O4", "O5", "O6", "O7", "O8",
        "P1", "P2", "P3", "P4", "P5", "R1", "R2", "R3", "R4", "R5", "V1", "V2", "V3",
        "V4", "V5", "V6", "V7", "V8", "Y1", "Y2", "Y3", "Y4", "Y5"
    )
    val selectedColorAdoIndex = remember { mutableIntStateOf(0) }
    val listPrice = remember { mutableIntStateOf(0) }
    val remarks = remember { mutableStateOf("") }
    val remarks2 = remember { mutableStateOf("") }
    val standardCost = remember { mutableIntStateOf(0) }
    val discontinued = remember { mutableIntStateOf(-1) }
    val discontinuedOptions = listOf("Yes", "No")
    val discontinuedIndex = remember {
        mutableIntStateOf(if (discontinued.intValue == -1) 0 else 1)
    }
    val reorderLevel = remember { mutableIntStateOf(0) }
    // + image

    val currentDate = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }
    entryDate.value = currentDate

    val newId = remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        newId.intValue = viewModel.getNextId()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Enables vertical scrolling

    ) {
        Text(
            text = "New ID: ${newId.intValue}",
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = "Entry Date: $currentDate",
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = "Product Line:",
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spinner(
            entries = productLineOptions,
            index = selectedLineIndex.intValue,
            label = "Product Line",
            onValueChange = { selectedLineIndex.intValue = it }
        )
        Text(text = "Category:", modifier = Modifier.padding(vertical = 8.dp))
        Spinner(
            entries = categoryOptions,
            index = selectedCategoryIndex.intValue,
            label = "Category",
            onValueChange = { selectedCategoryIndex.intValue = it }
        )
        Text(text = "Product SubCategory:", modifier = Modifier.padding(vertical = 8.dp))
        Spinner(
            entries = subCategoryOptions,
            index = selectedSubCategoryIndex.intValue,
            label = "Product SubCategory",
            onValueChange = { selectedSubCategoryIndex.intValue = it }
        )
        Text(
            text = "Product Type:",
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spinner(
            entries = productTypeOptions,
            index = selectedTypeIndex.intValue,
            label = "Product Type",
            onValueChange = { selectedTypeIndex.intValue = it }
        )
        TextField(
            value = productCode.value,
            onValueChange = { productCode.value = it },
            label = { Text("Product Code") },
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "Order Type:", modifier = Modifier.padding(vertical = 8.dp))
        Spinner(
            entries = orderTypeOptions,
            index = selectedOrderTypeIndex.intValue,
            label = "Order Type",
            onValueChange = { selectedOrderTypeIndex.intValue = it }
        )
        TextField(
            value = shortCode.value,
            onValueChange = { shortCode.value = it },
            label = { Text("Short Code") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = size.value,
            onValueChange = { size.value = it },
            label = { Text("Size") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "Unit:", modifier = Modifier.padding(vertical = 8.dp))
        Spinner(
            entries = unitOptions,
            index = selectedUnitIndex.intValue,
            label = "Unit",
            onValueChange = { selectedUnitIndex.intValue = it }
        )
        Text(text = "Color ADO:", modifier = Modifier.padding(vertical = 8.dp))
        Spinner(
            entries = colorAdoOptions,
            index = selectedColorAdoIndex.intValue,
            label = "Color ADO",
            onValueChange = { selectedColorAdoIndex.intValue = it }
        )
        TextField(
            value = listPrice.intValue.toString(),
            onValueChange = { newValue ->
                listPrice.intValue = newValue.toIntOrNull() ?: 0
            },
            label = { Text("List Price") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = remarks.value,
            onValueChange = { remarks.value = it },
            label = { Text("Remarks") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = remarks2.value,
            onValueChange = { remarks2.value = it },
            label = { Text("Remarks 2") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = standardCost.intValue.toString(),
            onValueChange = { newValue ->
                standardCost.value = newValue.toIntOrNull() ?: 0
            },
            label = { Text("Standard Cost") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(
            text = "Discontinued:",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Spinner(
            entries = discontinuedOptions,
            index = discontinuedIndex.intValue,
            label = "Discontinued",
            onValueChange = { index ->
                discontinuedIndex.intValue = index
                discontinued.intValue = if (discontinuedOptions[index] == "Yes") -1 else 0
            }
        )
            /*TextField(
                value = discontinued.value,
                onValueChange = { },
                label = { Text("Discontinued") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded.value = true },
                readOnly = true
            )
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    DropdownMenuItem(onClick = {
                        discontinued.value = option
                        expanded.value = false
                    }) {
                        Text(text = option)
                    }
                }
            }*/
        TextField(
            value = reorderLevel.intValue.toString(),
            onValueChange = { newValue ->
                reorderLevel.intValue = newValue.toIntOrNull() ?: 0
            },
            label = { Text("Reorder Level") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        // + image
/*        Button(
            LoadImage
        )*/
        Button(
            onClick = {
                val newProduct = Product(
                    id = 0, // assuming id is auto-generated on insert
                    productCode = productCode.value,
                    orderType = orderTypeOptions[selectedOrderTypeIndex.intValue],
                    shortCode = shortCode.value,
                    size = size.value,
                    description = description.value,
                    unit = unitOptions[selectedUnitIndex.intValue],
                    category = selectedCategoryIndex.intValue+1,
                    subCategory = selectedSubCategoryIndex.intValue+1,
                    type = productTypeOptions[selectedTypeIndex.intValue],
                    entryDate = entryDate.value,
                    line = productLineOptions[selectedLineIndex.intValue],
                    colorAdo = colorAdoOptions[selectedColorAdoIndex.intValue],
                    listPrice = listPrice.intValue,
                    remarks = remarks.value,
                    remarks2 = remarks2.value,
                    standardCost = standardCost.intValue,
                    discontinued = discontinued.value,
                    reorderLevel = reorderLevel.intValue,
                    image = "",
                    quantity = 0.0
                )
                // Insert into the database.
                viewModel.createNewProduct(newProduct)
                // After saving, invoke the callback to return to the main screen.
                onProductCreated()
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Save Product")
        }
    }
}

