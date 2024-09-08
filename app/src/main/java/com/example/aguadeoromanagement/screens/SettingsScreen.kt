package com.example.aguadeoromanagement.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.aguadeoromanagement.ManagementApplication

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    logAsAdmin: (String) -> Boolean,
    logOut: () -> Unit
) {
    var password by remember {
        mutableStateOf("")
    }
    var errorMessage by remember {
        mutableStateOf("")
    }

    Column(Modifier.padding(16.dp)) {
        Column {
            if (!ManagementApplication.isAdmin()) {
                Text("You are not logged as admin")
                TextField(
                    value = password,
                    onValueChange = { newVal -> password = newVal },
                    placeholder = { Text("password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Button(onClick = {
                    if (logAsAdmin(password)) {
                        password = ""
                        errorMessage = ""
                    } else {
                        errorMessage = "Password incorrect"
                    }
                }) {
                    Text("Log as admin")
                }
            } else {
                Text("You are logged as admin")

                Button(onClick = { logOut() }) {
                    Text("Log out")
                }
            }
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, style = TextStyle(color = Color.Red))
            }
        }
    }
}