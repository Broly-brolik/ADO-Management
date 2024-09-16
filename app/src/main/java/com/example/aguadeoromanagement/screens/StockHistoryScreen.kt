package com.example.aguadeoromanagement.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.aguadeoromanagement.models.StockHistory

@Composable
fun StockHistoryScreen(orderNumbers:List<StockHistory>, loading: Boolean, navController: NavController, modifier: Modifier = Modifier) {
    if (loading) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {

        InOutContainerV2(
            inOutList = orderNumbers,
            loadingItems = false,
            modifier = Modifier.padding(16.dp),
            navController = navController
        )
    }
}