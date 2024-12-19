package com.example.aguadeoromanagement.fragments

import android.os.Bundle
import android.view.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.Navigation
import com.example.aguadeoromanagement.viewmodels.InvoiceHistoryViewModel
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.screens.PaymentsListScreen
import com.example.aguadeoromanagement.ui.theme.ManagementTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//import com.example.screens.PaymentListScreen

class PaymentsList : Fragment() {

    var showPopup = mutableStateOf(false)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_payments_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_connect_reader -> {
                showPopup.value = true
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        return ComposeView(requireContext()).apply {

//            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ManagementTheme {
                    val viewModel: InvoiceHistoryViewModel = viewModel()

                    var payments = viewModel.paymentsList.value
                    var optionValues = viewModel.optionValues.value
                    var suppliers = viewModel.suppliers.value

                    fun navigateToInvoice(invoiceID: String, contactName: String) {
//                        val invoice: Invoice = Invoice()

                        GlobalScope.launch {
                            val res = viewModel.getInvoiceFromID(invoiceID)
                            if (res.second) {
                                val invoice = res.first
                                val action =
                                    PaymentsListDirections.actionPaymentsListToInvoiceDetails(
                                        invoice,
                                        contactName,
                                        emptyArray()
                                    )
                                launch {
                                    withContext(Dispatchers.Main) {
                                        Navigation.findNavController(requireView()).navigate(action)
                                    }
                                }
                            }
                        }
                    }

                    PaymentsListScreen(
                        payments = payments,
                        updatePayment = { daysBefore, future ->
                            viewModel.updatePayments(daysBefore, future)
                        },
                        optionValues = optionValues,
                        showPopup = showPopup.value,
                        dismissPopup = { showPopup.value = false },
                        navigateToInvoice = { invoiceID, contactName ->
                            navigateToInvoice(
                                invoiceID,
                                contactName
                            )
                        },
                        suppliers = suppliers,
                        updateInvoiceItems = { invoiceID, totalPrice, paidPrice ->
                            GlobalScope.launch {
                                viewModel.updatePaymentItem(invoiceID, totalPrice, paidPrice)
                            }
                        }
                    )
                }
            }
        }
    }

}