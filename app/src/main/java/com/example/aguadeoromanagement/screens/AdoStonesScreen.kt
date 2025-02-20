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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
    val selectedProduct = remember { mutableStateOf<Product?>(null) }

    // New state to choose search mode: "id" or "filters"
    val searchMode = remember { mutableStateOf("id") }

    if (isCreatingNewProduct.value) {
        CreateNewProduct(
            viewModel = viewModel,
            onProductCreated = { isCreatingNewProduct.value = false },
            templateProduct = selectedProduct.value
        )
    } else {
        val products = viewModel.products.collectAsState(initial = emptyList())

        // UI to switch between search modes
        Column(modifier = Modifier.padding(16.dp)) {
            // Toggle buttons for search mode
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                Button(onClick = { searchMode.value = "id" }) {
                    Text("Search by ID")
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Button(onClick = { searchMode.value = "filters" }) {
                    Text("Search by Filters")
                }
            }

            if (searchMode.value == "id") {
                // Existing search by ID UI
                val productId = remember { mutableStateOf("") }
                TextField(
                    value = productId.value,
                    onValueChange = { productId.value = it },
                    label = { Text("Enter Product ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        viewModel.searchProductById(productId.value)
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Search")
                }
            } else {
                // New filter search UI
                // Product Line options (from your existing lists)
                val productLineOptions = listOf("Line", "Linea Verde", "Mined", "Max H", "PX Impact", "Standard")
                val selectedLineIndex = remember { mutableIntStateOf(0) }
                Text(text = "Product Line:", modifier = Modifier.padding(vertical = 8.dp))
                Spinner(
                    entries = productLineOptions,
                    index = selectedLineIndex.intValue,
                    label = "Product Line",
                    onValueChange = { selectedLineIndex.intValue = it }
                )

                // Category options
                val categoryOptions = listOf("Stones", "Jewellery", "Labor")
                val selectedCategoryIndex = remember { mutableIntStateOf(0) }
                Text(text = "Category:", modifier = Modifier.padding(vertical = 8.dp))
                Spinner(
                    entries = categoryOptions,
                    index = selectedCategoryIndex.intValue,
                    label = "Category",
                    onValueChange = { selectedCategoryIndex.intValue = it }
                )

                // SubCategory options
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
                Text(text = "SubCategory:", modifier = Modifier.padding(vertical = 8.dp))
                Spinner(
                    entries = subCategoryOptions,
                    index = selectedSubCategoryIndex.intValue,
                    label = "SubCategory",
                    onValueChange = { selectedSubCategoryIndex.intValue = it }
                )

                // Product Type options
                val productTypeOptions = listOf("AU 750", "Color", "Diam", "Diam cognac", "Diam noir", "Pearls", "PT 950", "AG 925", "CZ", "Diam Green")
                val selectedTypeIndex = remember { mutableIntStateOf(0) }
                Text(text = "Product Type:", modifier = Modifier.padding(vertical = 8.dp))
                Spinner(
                    entries = productTypeOptions,
                    index = selectedTypeIndex.intValue,
                    label = "Product Type",
                    onValueChange = { selectedTypeIndex.intValue = it }
                )

                Button(
                    onClick = {
                        viewModel.searchProductsByFilters(
                            productLineOptions[selectedLineIndex.intValue],
                            selectedCategoryIndex.intValue,
                            selectedSubCategoryIndex.intValue,
                            productTypeOptions[selectedTypeIndex.intValue]
                        )
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Search")
                }
            }

            // The rest of your UI to display products
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
                        Button(
                            onClick = {
                                isCreatingNewProduct.value = true
                                selectedProduct.value = product
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text("Use as Template")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    val categoryText = when (product.category) {
        0 -> "Stones"
        1 -> "Jewellery"
        2 -> "Labor"
        else -> "Unknown"
    }
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

    val subCategoryText = if (product.subCategory in 1..subCategoryOptions.size) {
        subCategoryOptions[product.subCategory - 1]
    } else {
        "Unknown"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "Product ID: ${product.id}")
        Text(text = "Product Code: ${product.productCode}")
        Text(text = "Description: ${product.description}")
        Text(text = "Unit: ${product.unit}")
        Text(text = "Category: $categoryText")
        Text(text = "SubCategory: $subCategoryText")
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
    onProductCreated: () -> Unit,
    templateProduct: Product? = null,
) {
    // Initialize all the fields as empty.
    val productCode = remember { mutableStateOf(templateProduct?.productCode ?: "") }
    val orderTypeOptions = listOf("LossOrders", "FinishedOrders")
    val orderTypeIndex = orderTypeOptions.indexOf(templateProduct?.orderType ?: "LossOrders")
    val selectedOrderTypeIndex = remember {
        mutableIntStateOf(if (orderTypeIndex < 0) 0 else orderTypeIndex)
    }
    val shortCode = remember { mutableStateOf(templateProduct?.shortCode ?: "") }
    val size = remember { mutableStateOf(templateProduct?.size ?: "") }
    val description = remember { mutableStateOf(templateProduct?.description ?: "") }
    val unitOptions = listOf("Pieces", "Gramms")
    val unitIndex = unitOptions.indexOf(templateProduct?.unit ?: "Pieces")
    val selectedUnitIndex = remember {
        mutableIntStateOf(if (unitIndex < 0) 0 else unitIndex)
    }
    val categoryOptions = listOf("Stones", "Jewellery", "Labor")
    val selectedCategoryIndex = remember { mutableIntStateOf(templateProduct?.category ?: 0) }
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
    val selectedSubCategoryIndex = remember { mutableIntStateOf(templateProduct?.subCategory ?: 0) }
    val productTypeOptions = listOf("AU 750", "Color", "Diam", "Diam cognac", "Diam noir", "Pearls", "PT 950", "AG 925", "CZ", "Diam Green")
    val selectedTypeIndex = remember { mutableIntStateOf(productTypeOptions.indexOf(templateProduct?.type ?: "AU 750")) }
    val entryDate = remember { mutableStateOf(templateProduct?.entryDate ?: "") }

    val productLineOptions = listOf("Line", "Linea Verde", "Mined", "Max H", "PX Impact", "Standard")
    val selectedLineIndex = remember { mutableIntStateOf(productLineOptions.indexOf(templateProduct?.line ?: "Line")) }

    val colorAdoOptions = listOf(
        "B", "B1", "B10", "B11", "B12", "B13", "B14", "B15", "B16", "B17", "B18", "B19",
        "B2", "B20", "B21", "B22", "B23", "B24", "B25", "B26", "B28", "B29", "B3",
        "G1", "G10", "G11", "G12", "G13", "G14", "G15", "G16", "G17", "G18", "G19",
        "G2", "G20", "G21", "G3", "G4", "G5", "G6", "G7", "G8", "G9", "K1", "K2", "K3",
        "K4", "K5", "K6", "K7", "O", "O1", "O2", "O3", "O4", "O5", "O6", "O7", "O8",
        "P1", "P2", "P3", "P4", "P5", "R1", "R2", "R3", "R4", "R5", "V1", "V2", "V3",
        "V4", "V5", "V6", "V7", "V8", "Y1", "Y2", "Y3", "Y4", "Y5"
    )
    val colorAdoIndex = colorAdoOptions.indexOf(templateProduct?.colorAdo ?: "B")
    val selectedColorAdoIndex = remember {
        mutableIntStateOf(if (colorAdoIndex < 0) 0 else colorAdoIndex)
    }
    val listPrice = remember { mutableIntStateOf(templateProduct?.listPrice ?: 0) }
    val remarks = remember { mutableStateOf(templateProduct?.remarks ?: "") }
    val remarks2 = remember { mutableStateOf(templateProduct?.remarks2 ?: "") }
    val standardCost = remember { mutableIntStateOf(templateProduct?.standardCost ?: 0) }
    val discontinued = remember { mutableIntStateOf(templateProduct?.discontinued ?: 0) }
    val discontinuedOptions = listOf("No", "Yes")
    val discontinuedIndex = remember {
        mutableIntStateOf(if (discontinued.intValue == 0) 0 else 1)
    }
    val reorderLevel = remember { mutableIntStateOf(templateProduct?.reorderLevel ?: 0) }
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
        Log.d("zzz", "selectedOrderTypeIndex: ${selectedOrderTypeIndex.intValue}")
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
                discontinued.intValue = if (discontinuedOptions[index] == "No") 0 else -1
            }
        )
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
                    id = newId.intValue,
                    productCode = productCode.value,
                    orderType = orderTypeOptions[selectedOrderTypeIndex.intValue],
                    shortCode = shortCode.value,
                    size = size.value,
                    description = description.value,
                    unit = unitOptions[selectedUnitIndex.intValue],
                    category = selectedCategoryIndex.intValue,
                    subCategory = selectedSubCategoryIndex.intValue,
                    type = productTypeOptions[selectedTypeIndex.intValue],
                    entryDate = entryDate.value,
                    line = productLineOptions[selectedLineIndex.intValue],
                    colorAdo = colorAdoOptions[selectedColorAdoIndex.intValue],
                    listPrice = listPrice.intValue,
                    remarks = remarks.value,
                    remarks2 = remarks2.value,
                    standardCost = standardCost.intValue,
                    discontinued = discontinued.intValue,
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

// recup les fields des tables par queries et non hardcoded. TODO
// recup les images des produits
// load les images depuis la tablette
