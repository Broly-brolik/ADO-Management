package com.example.aguadeoromanagement.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aguadeoromanagement.enums.OrderGroupBy
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.networking.api.getStockForSimilarOrders
import kotlinx.coroutines.launch

class StockHistoryViewModelFactory(
    private val groupBy: String,
    private val orderNumber: String,
    private val supplier: String
) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        StockHistoryViewModel(groupBy, orderNumber, supplier) as T
}

class StockHistoryViewModel(groupBy: String, orderNumber: String, supplier: String) : ViewModel() {

    var orderNumbers = mutableStateOf(listOf<StockHistory>())

    //    var orderNumbers= mutableStateOf(mapOf<String, List<StockHistory>>())
    init {
        Log.e("init stock history viewmodel", "$groupBy - $orderNumber - $supplier")
        viewModelScope.launch {
            if (groupBy == OrderGroupBy.order_number.name) {
                orderNumbers.value = getStockForSimilarOrders(orderNumber)
            }
            else if(groupBy == OrderGroupBy.supplier.name){

            }

        }
    }

}