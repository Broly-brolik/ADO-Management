package com.example.aguadeoromanagement.fragments

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.Navigation
import com.example.aguadeoromanagement.BuildConfig
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.screens.CheckInventoryScreen
import com.example.aguadeoromanagement.screens.InventoryScreen
import com.example.aguadeoromanagement.screens.SettingsScreen
import com.example.aguadeoromanagement.ui.theme.ManagementTheme
import com.example.aguadeoromanagement.viewmodels.InventoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)



        return ComposeView(requireContext()).apply {

            setContent {
                ManagementTheme {
                    SettingsScreen(
                        logAsAdmin = { pass -> logAsAdmin(pass, requireActivity()) },
                        logOut = { logOut(requireActivity()) })
                }
            }
        }
    }
}

fun logAsAdmin(pass: String, activity: FragmentActivity): Boolean {
    if (pass == Constants.password) {
        ManagementApplication.changeAdmin(true)
        val prefs = ManagementApplication.getAppPref()
        val editor = prefs.edit()
        editor.putBoolean("isAdmin", true)
        editor.apply()
        val textView = activity.findViewById<TextView>(R.id.textViewVersionNumber)
        textView.text = "${BuildConfig.VERSION_NAME} (admin)"
        return true
    }
    return false
}

fun logOut(activity: FragmentActivity) {
    ManagementApplication.changeAdmin(false)
    val prefs = ManagementApplication.getAppPref()
    val editor = prefs.edit()
    editor.putBoolean("isAdmin", false)
    editor.apply()
    val textView = activity.findViewById<TextView>(R.id.textViewVersionNumber)
    textView.text = "${BuildConfig.VERSION_NAME}"
}
