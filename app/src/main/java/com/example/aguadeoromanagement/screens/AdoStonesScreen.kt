package com.example.aguadeoromanagement.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun AdoStonesScreen(
    productId : String,
) {
    Column {
        var text = "test2"
        TextField(
            value = "",
            onValueChange = { text = it },
            label = { Text("Label") },
            modifier = Modifier.width(200.dp)
        )
        Text(
            "Type :\n" +
                    "SubCategory:\n" +
                    "Line: \n" +
                    "Color_ADO:"
        )
    }
}
