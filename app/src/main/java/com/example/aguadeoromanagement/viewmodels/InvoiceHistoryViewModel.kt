package com.example.aguadeoromanagement.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.Contact
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.OptionValue
import com.example.aguadeoromanagement.models.Payment
import com.example.aguadeoromanagement.networking.Query
import com.example.aguadeoromanagement.networking.api.getPayments
import com.example.aguadeoromanagement.networking.getInvoicesWithFilter
import com.example.aguadeoromanagement.networking.getPaymentsListWithFilter
import com.example.aguadeoromanagement.networking.getSuppliers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class InvoiceHistoryViewModel : ViewModel() {


    val paymentsList = mutableStateOf(emptyList<Payment>())
    val optionValues = mutableStateOf(emptyList<OptionValue>())
    val suppliers = mutableStateOf(emptyList<Contact>())
    val selectedDate = mutableStateOf(LocalDate.now())
    var daysBeforeNow = mutableIntStateOf(6)

    init {
        viewModelScope.launch {
            updatePayments()
            suppliers.value = getSuppliers()
        }
    }

    suspend fun updatePayments(daysBeforeNow: Int = 6, future: Boolean = false) {
        withContext(Dispatchers.IO) {
            val res = getPayments(daysBeforeNow, future)
            if (res.second) {
                paymentsList.value = res.first
            }
            optionValues.value = getOptionValues()
        }

    }

    fun updateDate(newDay: Int, newDate: LocalDate) {
        viewModelScope.launch {
            daysBeforeNow.value = newDay
            selectedDate.value = newDate
            paymentsList.value = getPaymentsListWithFilter("", daysBeforeNow.value, selectedDate.value)
        }
    }

    private suspend fun getOptionValues(): List<OptionValue> {
        return withContext(Dispatchers.IO) {
            val q = Query("select * from OptionValues")
            val s = q.execute(Constants.url)
            val res = mutableListOf<OptionValue>()
            q.res.forEach { map ->
                res.add(
                    OptionValue(
                        map["ID"]!!.toInt(),
                        map["Type"]!!,
                        map["OptionKey"]!!,
                        map["OptionValue"]!!
                    )
                )
            }
            return@withContext res
        }
    }

    suspend fun updatePaymentItem(invoiceID: Int, totalPrice: Double, paidPrice: Double) {
        return withContext(Dispatchers.IO) {
            val q = Query(
                "select * From InvoiceItems WHERE InvoiceID = $invoiceID"
            )
            val s = q.execute(Constants.url)
            for (item in q.res) {
                try {
                    val itemID = item.getOrDefault("ID", "")
                    if (itemID.isEmpty()) {
                        Log.e("item not found", "")
                        continue
                    }
                    var calculatedAmount = item.getOrDefault("CalculatedAmount", "0").toDouble()
                    var paidAmount = calculatedAmount
                    var alreadyPaid = if (item["AmountPaid"]!!.isEmpty()) {
                        0.0
                    } else {
                        item["AmountPaid"]!!.toDouble()
                    }

                    if (paidPrice < totalPrice) {
                        val percentage = paidPrice / totalPrice
                        paidAmount *= percentage
                    }
                    paidAmount += alreadyPaid

                    val q2 = Query(
                        "update InvoiceItems set AmountPaid = $paidAmount where ID = $itemID"
                    )
                    val s2 = q2.execute(Constants.url)
                    if (s2) {
                        Log.e("update successful for item", itemID)
                    }


//                    val accountCode = item.getOrDefault("AccountCode", "")
//                    Log.e("calc amount for $accountCode", calculatedAmount)
                } catch (e: Error) {
                    e.printStackTrace()
                }
            }
        }
    }


    suspend fun getInvoiceFromID(id: String): Pair<Invoice, Boolean> {
        return withContext(Dispatchers.IO) {
            val q = Query(
                "select * From Invoice WHERE ID = ${id}"
            )
//            WHERE PaymentDate >= #$dateLimit#
            val s = q.execute(Constants.url)
            val res = mutableListOf<Invoice>()
            q.res.forEach { invoice ->
                res.add(
                    Invoice(
                        id = invoice["ID"]!!.toInt(),
                        supplierId = invoice["Supplier"]?.toIntOrNull() ?: 0,
                        invoiceDate = invoice["InvoiceDate"]!!,
                        invoiceNumber = invoice["InvNumber"]!!,
                        amount = invoice["Amount"]?.toDoubleOrNull() ?: 0.0,
                        receivedDate = invoice["ReceivedDate"]!!,
                        remark = invoice["Remark"]!!,
                        items = mutableListOf(),
                        dueOn = invoice["DueOn"] ?: "",
                        paid = invoice["Paid"]?.toDoubleOrNull() ?: 0.0,
                        remain = invoice["Remain"]?.toDoubleOrNull() ?: 0.0,
                        status = invoice["Status"] ?: "",
                        discountPercentage = invoice["Discount"]?.toDoubleOrNull() ?: 0.0,
                        registeredBy = invoice["RegisteredBy"] ?: "",
                        createdDate = invoice["CreatedDate"] ?: "",
                        currency = invoice["Curr"] ?: "",
                    )
                )
            }
            return@withContext Pair(res[0], s)

        }
    }


}