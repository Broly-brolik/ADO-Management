package com.example.aguadeoromanagement.fragments

import android.os.Bundle
import android.view.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.screens.InvoiceListScreen
import com.example.aguadeoromanagement.viewmodels.InvoiceHistoryViewModel
import com.example.aguadeoromanagement.ui.theme.ManagementTheme
import com.example.aguadeoromanagement.viewmodels.InvoiceListViewModel
import kotlinx.coroutines.launch

//import com.example.screens.PaymentListScreen

class InvoiceListFragment : Fragment() {


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
//        inflater.inflate(R.menu.menu_payments_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.action_connect_reader -> {
//                showPopup.value = true
//            }
//        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)



        return ComposeView(requireContext()).apply {

            setContent {
                ManagementTheme {
                    val viewModel: InvoiceListViewModel = viewModel()
                    val scope = viewModel.viewModelScope
                    InvoiceListScreen(
                        invoices = viewModel.invoices.value,
                        loading = viewModel.loading.value,
                        loadingItems = viewModel.loadingItems.value,
                        navigateToDetails = {
                            val navController = findNavController()
                            val supplierName = ManagementApplication.getSuppliersInfo()
                                .getOrDefault(it.supplierId.toString(), "")
                            val direction = InvoiceListFragmentDirections.actionInvoiceListToInvoiceDetails(it, supplierName,
                                emptyArray()
                            )
                            navController.navigate(direction)
                        },
                        invoiceItems = viewModel.invoiceItems.value,
                        paymentList = viewModel.paymentList.value,
                        inOutList = viewModel.inOutList.value,
                        fetchInvoiceItems = { invoice -> viewModel.fetchInvoiceItems(invoice) },
                        fetchPaymentList = { invoice -> viewModel.fetchPaymentList(invoice) },
                        fetchInOuts = { invoice: Invoice ->
                            scope.launch {
                                viewModel.fetchInOuts(invoice)
                            }
                        },
                        invoicesToCheck = viewModel.invoicesToCheck.value,
                        addRemoveInvoiceToCheck = { invoice, add ->
                            viewModel.addRemoveInvoiceToCheck(
                                invoice,
                                add
                            )
                        },
                        validateInvoices = { invoices -> viewModel.validateInvoices(invoices) },
                        daysBeforeNow = viewModel.daysBeforeNow.value,
                        selectedDate = viewModel.selectedDate.value,
                        updateDate = { newDays, newDate -> viewModel.updateDate(newDays, newDate) },
                        navController = findNavController()
                        )
                }
            }
        }
    }


}