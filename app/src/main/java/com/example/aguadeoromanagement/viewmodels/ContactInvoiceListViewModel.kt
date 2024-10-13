package com.example.aguadeoromanagement.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aguadeoromanagement.models.*
import com.example.aguadeoromanagement.networking.api.getInvoiceItems
import com.example.aguadeoromanagement.networking.getSupplierInvoices
import kotlinx.coroutines.launch

class InvoiceListViewModelFactory(private val contact: Contact) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ContactInvoiceListViewModel(contact) as T
}

class ContactInvoiceListViewModel(contact: Contact) : ViewModel() {
    val openInvoices = mutableStateOf(emptyList<Invoice>())
    val closedInvoices = mutableStateOf(emptyList<Invoice>())
    val loading = mutableStateOf(true)
    val loadingItems = mutableStateOf(false)
    var invoiceItems = mutableStateOf(emptyList<InvoiceItem>())


    init {
        viewModelScope.launch {
            Log.e("lets goo", "come on")
            openInvoices.value = getSupplierInvoices(contact).first
            closedInvoices.value = getSupplierInvoices(contact, true).first
            loading.value = false
        }
    }


    fun fetchInvoiceItems(invoice: Invoice){
        viewModelScope.launch {
            loadingItems.value = true
            invoiceItems.value = getInvoiceItems(invoice)
            loadingItems.value = false
        }
    }

}