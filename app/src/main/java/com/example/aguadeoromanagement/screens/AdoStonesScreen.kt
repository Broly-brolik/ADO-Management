package com.example.aguadeoromanagement.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.aguadeoromanagement.models.Product
import com.example.aguadeoromanagement.viewmodels.AdoStonesViewModel


@Composable
fun AdoStonesScreen(
    viewModel: AdoStonesViewModel
) {
    val productId = remember { mutableStateOf("") }
    val products = viewModel.products.collectAsState(initial = emptyList())
    val error = viewModel.error.collectAsState(initial = null)
    val isSearchTriggered = remember { mutableStateOf(false) }
    val filteredProducts = products.value.filter { it.productCode.contains(productId.value, ignoreCase = true) }

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

        if (!error.value.isNullOrEmpty()) {
            Text(
                text = "Error: ${error.value}",
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else if (products.value.isEmpty()) {
            Text(
                text = "No product found.",
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                items(products.value) { product ->
                    ProductItem(product)
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

        if (!product.picture.isNullOrEmpty()) {
            Image(
                painter = rememberImagePainter(product.picture),
                contentDescription = "Product Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

