package com.example.aguadeoromanagement.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowDropDown
import androidx.compose.material.icons.sharp.KeyboardArrowDown
import androidx.compose.material.icons.sharp.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.aguadeoromanagement.ui.theme.GrayBorder
import com.example.aguadeoromanagement.ui.theme.Shapes
import com.example.aguadeoromanagement.ui.theme.White


@Composable
fun Spinner(
    entries: List<String>,
    index: Int,
    label: String,
    modifier: Modifier = Modifier,
    showError: Boolean = false,
    onValueChange: (newVal: Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedValue = if (index < 0) label else entries[index]


    Box(
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = modifier
//                .fillMaxWidth()
                .clickable(onClick = { expanded = !expanded })
                .background(color = White, shape = Shapes.small)
                .border(
                    border = BorderStroke(width = 2.dp, color =  GrayBorder),
                    shape = Shapes.small
                )
                .padding(start = 16.dp, end = 21.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                selectedValue,
            )
            val icon = if (expanded) Icons.Sharp.KeyboardArrowUp else Icons.Sharp.KeyboardArrowDown
            Icon(icon, contentDescription = "")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(White)
//                .fillMaxWidth()
//                .fillMaxHeight(0.33F),

        ) {
            entries.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    onValueChange(index)
                    expanded = false
                }) {
                    Text(text = s, color = Color.Black)
                }
            }
        }
    }
}
