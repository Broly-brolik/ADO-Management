package com.example.aguadeoromanagement.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.example.aguadeoromanagement.adapters.CashReportAdapter
import com.example.aguadeoromanagement.databinding.FragmentReportCashBinding
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.Payment
import com.example.aguadeoromanagement.models.Supplier
import com.example.aguadeoromanagement.networking.APIFetcher
import com.google.android.material.datepicker.MaterialDatePicker
import java.math.BigDecimal
import kotlin.concurrent.thread

class CashReportFragment : Fragment() {
    private val binding: FragmentReportCashBinding by lazy {
        FragmentReportCashBinding.inflate(
            layoutInflater
        )
    }
    private var suppliers = arrayOf<String>()
    private var suppliersData = listOf<Supplier>()
    private var selectedSupplier = ""
    private var invoices = listOf<Invoice>()
    private lateinit var adapter: CashReportAdapter
    private var payments = listOf<Payment>()
    private val datePicker = MaterialDatePicker.Builder.dateRangePicker().setSelection(
        Pair(
            MaterialDatePicker.thisMonthInUtcMilliseconds(),
            MaterialDatePicker.todayInUtcMilliseconds()
        )
    ).setTitleText("Select date").build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = binding.list
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                recyclerView.context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            )
        )
        binding.datePickerBtn.setOnClickListener {
            datePicker.show(childFragmentManager, "datePicker")
        }
        datePicker.addOnPositiveButtonClickListener {
            val supplierId = suppliersData.find { it.name == selectedSupplier }?.id ?: -1
            thread {
                loadInvoices(supplierId, it.first, it.second)
            }
        }
        binding.datePickerBtn.isEnabled = false
        adapter =
            CashReportAdapter(requireContext(), invoices.toMutableList(), payments.toMutableList())
        recyclerView.adapter = adapter
        thread {
            loadSuppliers()
        }
    }

    private fun loadInvoices(supplierId: Int, from: Long, to: Long) {
        requireActivity().runOnUiThread {
            binding.loading.visibility = View.VISIBLE
        }
        if (supplierId >= 0) {
            APIFetcher(requireActivity()).getInvoices(supplierId, from, to) { data, s ->
                APIFetcher(requireActivity()).getPayments { payments, s2 ->
                    requireActivity().runOnUiThread {
                        binding.loading.visibility = View.GONE
                        if (s) {
                            var amountTotal =
                                BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_EVEN)
                            var paidTotal = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_UP)
                            data.forEach { invoice ->
                                amountTotal = amountTotal.add(
                                    BigDecimal(invoice.amount).setScale(
                                        2, BigDecimal.ROUND_HALF_UP
                                    )
                                )
                                payments.forEach { payment ->
                                    if (payment.invoiceID == invoice.id) {
                                        if (payment.executed) {
                                            paidTotal = paidTotal.add(
                                                BigDecimal(payment.amountPaid).setScale(
                                                    2, BigDecimal.ROUND_HALF_UP
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            binding.remainingTotal.text =
                                "Remaining : ${amountTotal.subtract(paidTotal)}CHF"
                            invoices = data
                            this.payments = payments
                            adapter.setData(invoices.toMutableList(), payments)
                        }
                    }
                }
            }
        } else {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Supplier not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSuppliers() {
        requireActivity().runOnUiThread {
            binding.loading.visibility = View.VISIBLE
        }
        APIFetcher(requireActivity()).getSupplierRecipients { data, s ->
            requireActivity().runOnUiThread {
                binding.loading.visibility = View.GONE
                binding.datePickerBtn.isEnabled = true
                suppliers += "Select Supplier"
                suppliersData = data
                for (supplier in data) {
                    suppliers += supplier.name
                }
                if (s) {
                    if (data.isNotEmpty()) {
                        val spinnerAdapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            suppliers
                        )
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.supplierSpinner.adapter = spinnerAdapter
                        binding.supplierSpinner.isSelected = false
                        binding.supplierSpinner.setSelection(0, true)
                        binding.supplierSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                                ) {
                                    val supplier = parent?.getItemAtPosition(position) as String
                                    selectedSupplier = supplier
                                    val supplierId =
                                        suppliersData.find { it.name == selectedSupplier }?.id ?: -1
                                    thread {
                                        loadInvoices(
                                            supplierId,
                                            datePicker.selection!!.first,
                                            datePicker.selection!!.second
                                        )
                                    }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {
                                    selectedSupplier = ""
                                }
                            }
                    }
                }
            }
        }
    }
}