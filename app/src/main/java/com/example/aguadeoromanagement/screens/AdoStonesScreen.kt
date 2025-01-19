package com.example.aguadeoromanagement.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.aguadeoromanagement.models.Product
import com.example.aguadeoromanagement.viewmodels.AdoStonesViewModel


@Composable
fun AdoStonesScreen(
    viewModel: AdoStonesViewModel
) {
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

        if (products.value.isEmpty()) {
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
        Text(text = "Quantity: ${product.quantity}")

        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (product.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                )
            }
        }
    }
}

