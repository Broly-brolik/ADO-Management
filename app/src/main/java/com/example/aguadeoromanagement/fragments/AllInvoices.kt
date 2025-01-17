package com.example.aguadeoromanagement.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aguadeoromanagement.models.Contact
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.screens.ContactInvoiceListScreen
import com.example.aguadeoromanagement.ui.theme.ManagementTheme
import com.example.aguadeoromanagement.viewmodels.ContactInvoiceListViewModel
import com.example.aguadeoromanagement.viewmodels.InvoiceListViewModelFactory


class AllInvoices : Fragment() {
    private val args by navArgs<AllInvoicesArgs>()
    private lateinit var contact: Contact


    fun navigate(invoice: Invoice) {
        val action =
            AllInvoicesDirections.actionAllInvoicesToInvoiceDetails(
                invoice,
                contact.name,
                emptyArray()
            )
        findNavController().navigate(action)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args.contact.let {
            contact = it
        }
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Invoices of ${contact.name}"

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = requireActivity()
        activity.title = "yo"

        return ComposeView(requireContext()).apply {
            setContent {
                ManagementTheme {
                    val viewModel: ContactInvoiceListViewModel =
                        viewModel(factory = InvoiceListViewModelFactory(contact))

                    val openInvoices = viewModel.openInvoices
                    val closedInvoices = viewModel.closedInvoices
                    val loading = viewModel.loading


                    ContactInvoiceListScreen(
                        openInvoices = openInvoices.value,
                        closedInvoices = closedInvoices.value,
                        loading = loading.value,
                        loadingItems = viewModel.loadingItems.value,
                        navigateToDetails = { invoice -> navigate(invoice) },
                        invoiceItems = viewModel.invoiceItems.value,
                        fetchInvoiceItems = {invoice -> viewModel.fetchInvoiceItems(invoice) }
                    )
                }
            }
        }
    }




}
