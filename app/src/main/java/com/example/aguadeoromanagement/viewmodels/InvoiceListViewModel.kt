package com.example.aguadeoromanagement.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.models.*
import com.example.aguadeoromanagement.networking.*
import com.example.aguadeoromanagement.networking.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class InvoiceData(
    val payments: MutableList<Payment> = mutableListOf(),
    val invoiceItems: MutableList<InvoiceItem> = mutableListOf(),
    val inOuts: MutableList<StockHistory> = mutableListOf()
)


class InvoiceListViewModel() : ViewModel() {
    val invoices = mutableStateOf(emptyList<Invoice>())
    val loading = mutableStateOf(true)
    val loadingItems = mutableStateOf(false)
    var invoiceItems = mutableStateOf(emptyList<InvoiceItem>())
    var paymentList = mutableStateOf(emptyList<Payment>())
    var inOutList = mutableStateOf(emptyList<StockHistory>())
    var invoicesToCheck = mutableStateOf(emptyList<Invoice>())
    var daysBeforeNow = mutableIntStateOf(6)
    val invoicesData = mutableMapOf<String, InvoiceData>()
    val selectedDate = mutableStateOf(LocalDate.now())


    init {
        viewModelScope.launch {
            invoices.value = getInvoicesWithFilter("", daysBeforeNow.value)
            loading.value = false
        }
    }

    fun updateDate(newDay: Int, newDate: LocalDate) {
        viewModelScope.launch {
            loading.value = true
            daysBeforeNow.value = newDay
            selectedDate.value = newDate
            invoices.value = getInvoicesWithFilter("", daysBeforeNow.value, selectedDate.value)
            loading.value = false
        }
    }

    fun addRemoveInvoiceToCheck(invoice: Invoice, add: Boolean) {
        val invoices = invoicesToCheck.value.toMutableList()
        if (add) {
            invoices.add(invoice)
        } else {
            invoices.remove(invoice)
        }
        invoicesToCheck.value = invoices
    }

    fun validateInvoices(invoiceList: List<Invoice>) {
        viewModelScope.launch {
            loading.value = true
            val success = updateInvoiceCheck(invoiceList)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    ManagementApplication.getAppContext(),
                    if (success) "Update successful" else "Failed to update",
                    Toast.LENGTH_LONG
                ).show()
            }
            invoicesToCheck.value = emptyList()
            invoices.value = getInvoicesWithFilter("")
            loading.value = false
        }
    }

    fun fetchInvoiceItems(invoice: Invoice) {
        viewModelScope.launch {
            loadingItems.value = true
            if (invoicesData[invoice.invoiceNumber]?.invoiceItems?.isNotEmpty() == true) {
                invoiceItems.value = invoicesData[invoice.invoiceNumber]!!.invoiceItems
                loadingItems.value = false
                return@launch
            } else {
                val items = getInvoiceItems(invoice)
                val invoiceData = invoicesData.getOrDefault(invoice.invoiceNumber, InvoiceData())
                invoiceData.invoiceItems.addAll(items)
                invoicesData[invoice.invoiceNumber] = invoiceData
                invoiceItems.value = items
                loadingItems.value = false
            }
        }
    }

    fun fetchPaymentList(invoice: Invoice) {
        viewModelScope.launch {
            loadingItems.value = true
            if (invoicesData[invoice.invoiceNumber]?.payments?.isNotEmpty() == true) {
                paymentList.value = invoicesData[invoice.invoiceNumber]!!.payments
                loadingItems.value = false
                return@launch
            } else {
                val where = "WHERE InvoiceID = ${invoice.id}"
                val payments = getPayments(0, true, where, noContactName = true).first
                val invoiceData = invoicesData.getOrDefault(invoice.invoiceNumber, InvoiceData())
                invoiceData.payments.addAll(payments)
                invoicesData[invoice.invoiceNumber] = invoiceData
                paymentList.value = payments
                loadingItems.value = false
            }
        }
    }

    suspend fun fetchInOuts(invoice: Invoice) {
//        return withContext(Dispatchers.IO) {
        viewModelScope.launch {
            loadingItems.value = true

            if (invoicesData[invoice.invoiceNumber]?.inOuts?.isNotEmpty() == true) {
                Log.e("already loaded", "")
                inOutList.value = invoicesData[invoice.invoiceNumber]!!.inOuts
                loadingItems.value = false
                return@launch
            } else {
//                val supplierName = ManagementApplication.getSuppliersInfo()
//                    .getOrDefault(invoice.supplierId.toString(), "")
//                val invoiceItems = getInvoiceItems(invoice)
//                val distinctOrders =
//                    invoiceItems.map { invoiceItem -> invoiceItem.supplierOrderID }
//                        .distinct()
////            val supplierOrders = getSupplierOrdersOfIds(distinctOrders).toMutableList()
////            val supplierOrderMainHistory =
////                getSupplierOrderMainHistory(
////                    supplierName
////                ).filter { it.status != "Status 3" }
////            Log.e("supplier history",
////                supplierOrderMainHistory.map{it.supplierOrderNumber}.toString()
////            )
//                val stockHistoryForOrders = getStockHistoryForOrders(
//                    1,
//                    distinctOrders
//                )
//
//                val stockHistory = stockHistoryForOrders.map { it.value }.flatten()
//                    .filter { it.supplier == supplierName }.sortedBy { it.type }
//
                val stockHistory = getStockHistoryForInvoice(invoice.id)
                val invoiceData = invoicesData.getOrDefault(invoice.invoiceNumber, InvoiceData())
                invoiceData.inOuts.addAll(stockHistory)
                invoicesData[invoice.invoiceNumber] = invoiceData

                inOutList.value = stockHistory
                loadingItems.value = false
            }
        }
    }
}