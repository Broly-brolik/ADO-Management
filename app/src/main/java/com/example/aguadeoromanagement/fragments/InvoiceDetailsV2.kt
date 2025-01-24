package com.example.aguadeoromanagement.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.compose.ui.platform.ComposeView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.adapters.invoiceDetails.InvoiceDetailsAdapter
import com.example.aguadeoromanagement.adapters.invoiceDetails.InvoiceHistoryAdapter
import com.example.aguadeoromanagement.databinding.FragmentInvoiceDetailsBinding
import com.example.aguadeoromanagement.dialogs.AddItemDialog
import com.example.aguadeoromanagement.models.*
import com.example.aguadeoromanagement.networking.*
import com.example.aguadeoromanagement.networking.api.*
import com.example.aguadeoromanagement.screens.InvoiceDetailsScreen
import com.example.aguadeoromanagement.screens.StatisticsScreen
import com.example.aguadeoromanagement.ui.theme.ManagementTheme
import com.example.aguadeoromanagement.utils.toPrice
import com.example.aguadeoromanagement.viewmodels.StatisticsViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap


class InvoiceDetailsV2 : Fragment() {

    private val args by navArgs<InvoiceDetailsArgs>()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_invoice_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit_invoice_details -> {
                //showEditInvoiceDialog()
                true
            }
            R.id.action_add_payment -> {
//                Toast.makeText(requireContext(), "yo", Toast.LENGTH_SHORT).show()
//                if (invoice.id > 0) {
//                    showPaymentDialog()
//                } else {
//                    Toast.makeText(
//                        ManagementApplication.getAppContext(),
//                        "Please save invoice before adding a payment",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
            }
            R.id.action_add_invoice_history -> {
//                val view = requireActivity().layoutInflater.inflate(
//                    R.layout.dialog_add_invoice_history,
//                    null
//                )
//                val builder = AlertDialog.Builder(requireContext())
//                builder.setTitle("Add History")
//                builder.setView(view)
//                builder.create()
//                builder.setCancelable(false)
//
//                builder.setPositiveButton("Add") { dialog, _ ->
//                    val message =
//                        view.findViewById<EditText>(R.id.editTextInvoiceHistoryRemark).text.toString()
//                    CoroutineScope(job + Dispatchers.Main).launch {
//                        updateHistory(message)
//                    }
//                    dialog.dismiss()
//
//                }
//                builder.setNegativeButton("Cancel") { dialog, _ ->
//                    dialog.dismiss()
//                }
//
//                if (invoice.id > 0) {
//                    builder.show()
//                } else {
//                    Toast.makeText(
//                        ManagementApplication.getAppContext(),
//                        "Please save invoice before modifying its history",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//            R.id.action_home -> {
//                findNavController().navigate(R.id.action_invoiceDetails_to_FirstFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {

            setContent {
                ManagementTheme {
                    InvoiceDetailsScreen()
                }
            }
        }
    }

    /*private fun showEditInvoiceDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_invoice_details, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Invoice Details")
            .setView(dialogView)
            .create()

        val editInvoiceNumber = dialogView.findViewById<EditText>(R.id.editInvoiceNumber)
        val editInvoiceDate = dialogView.findViewById<EditText>(R.id.editInvoiceDate)
        val editCurrency = dialogView.findViewById<EditText>(R.id.editCurrency)
        val editDiscount = dialogView.findViewById<EditText>(R.id.editDiscount)
        val editRemarks = dialogView.findViewById<EditText>(R.id.editRemarks)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        // Populate fields with existing data
        editInvoiceNumber.setText(invoice.invoiceNumber)
        editInvoiceDate.setText(invoice.createdDate)
        editCurrency.setText(invoice.currency)
        editDiscount.setText(invoice.discountPercentage.toString())
        editRemarks.setText(invoice.remark)

        // Handle Save button click
        saveButton.setOnClickListener {
            invoice.invoiceNumber = editInvoiceNumber.text.toString()
            invoice.createdDate = editInvoiceDate.text.toString()
            invoice.currency = editCurrency.text.toString()
            invoice.discountPercentage = editDiscount.text.toString().toDoubleOrNull() ?: 0.0
            invoice.remark = editRemarks.text.toString()

            // Save changes to the backend or database
            CoroutineScope(Dispatchers.IO).launch {
                val success = saveInvoiceChanges(invoice)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(requireContext(), "Invoice updated", Toast.LENGTH_SHORT).show()
                        refresh() // Refresh UI
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Error saving changes", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }*/

}