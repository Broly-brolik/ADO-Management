package com.example.aguadeoromanagement

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.aguadeoromanagement.models.Contact
import com.example.aguadeoromanagement.models.OptionValue
import com.example.aguadeoromanagement.models.Supplier
import com.example.aguadeoromanagement.networking.getCategories
import com.example.aguadeoromanagement.networking.getContacts
import com.example.aguadeoromanagement.networking.getOptionValues
import com.example.aguadeoromanagement.networking.getSuppliers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.cert.PKIXRevocationChecker.Option
import kotlin.properties.Delegates

class ManagementApplication : Application() {
    init {
        app = this
        GlobalScope.launch {
            optionValues = getOptionValues()
            categories = getCategories()
            suppliers = getContacts()
            isAdmin.value = getAppPref().getBoolean("isAdmin", false)

        }
    }

    companion object {
        private lateinit var app: ManagementApplication
        lateinit var optionValues: List<OptionValue>
        lateinit var suppliers: Map<String, String>
        lateinit var categories: Map<Int, String>
        private var isAdmin: MutableState<Boolean> = mutableStateOf(false)
        fun getAppContext(): Context = app.applicationContext
        fun isAdmin(): Boolean = isAdmin.value
        fun changeAdmin(newVal: Boolean) {
            isAdmin.value = newVal
        }

        //        @JvmName("getOptionValues1")
        fun getAppOptionValues(): List<OptionValue> = optionValues
        fun getSuppliersInfo(): Map<String, String> = suppliers
        fun getCategoriesInfo(): Map<Int, String> = categories

        fun getOptionValueList(type: String): List<String> =
            optionValues.filter { it.type == type }.map { it.optionValue }

        fun getAppPref(): SharedPreferences = app.getSharedPreferences("", MODE_PRIVATE)
    }
}