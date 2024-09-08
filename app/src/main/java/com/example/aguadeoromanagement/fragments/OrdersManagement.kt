package com.example.aguadeoromanagement.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aguadeoromanagement.adapters.OrdersManagementAdapter
import com.example.aguadeoromanagement.databinding.FragmentOrdersManagementBinding
import com.example.aguadeoromanagement.models.Payment
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.models.SupplierOrder
import com.example.aguadeoromanagement.networking.APIFetcher
import com.google.android.material.datepicker.MaterialDatePicker
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread


class OrdersManagement : Fragment(), OrdersManagementAdapter.CheckboxListener {
    private var _binding: FragmentOrdersManagementBinding? = null
    private val binding get() = _binding!!
    private var supplierOrdersData: MutableList<SupplierOrder> = mutableListOf()
    private var filteredSupplierOrdersData: MutableList<SupplierOrder> = mutableListOf()
    private var paymentsData = mutableListOf<Payment>()
    private var stockHistoryData = mutableListOf<StockHistory>()
    private var suppliers = arrayOf<String>()
    private var approvers = arrayOf<String>()
    private lateinit var adapter: OrdersManagementAdapter
    private var supplierSelected = ""
    private var total = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_EVEN)
    private var totalApproved = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_EVEN)
    private var totalGrams = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_EVEN)
    private var totalApprovedGrams = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_EVEN)
    private var balanceTotal = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_EVEN)
    private var toApprove: MutableMap<String, String> = mutableMapOf()

    private val datePicker =
        MaterialDatePicker.Builder.datePicker().setTitleText("Select date").build()
    private var dateSelected = ""
    private var daysSelected = ""
    private var dateFrom = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersManagementBinding.inflate(inflater, container, false)

        var spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            arrayOf("All", "Approved & unpaid", "Approved & paid")
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filter.adapter = spinnerAdapter
        var days: Array<String> = arrayOf()
        for (i in 1..31) {
            days += "" + i
        }
        spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            days
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dateSpinner.adapter = spinnerAdapter
        binding.dateSpinner.setSelection(5)
        daysSelected = binding.dateSpinner.selectedItem.toString()
        thread {
            loadSuppliers()
            loadApprovers()
        }
        binding.suppliers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long
            ) {
                supplierSelected = parentView.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        binding.filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long
            ) {
                filter(parentView.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        binding.approveBtn.setOnClickListener {
            it.isEnabled = false
            binding.loading.visibility = View.VISIBLE
            approve()
        }
        binding.dateBtn.setOnClickListener {
            datePicker.show(childFragmentManager, "datePicker")
        }
        datePicker.addOnPositiveButtonClickListener {
            dateSelected = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            daysSelected = binding.dateSpinner.selectedItem.toString()
            dateFrom = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                .minusDays(daysSelected.toLong() - 1)
                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            thread {
                refresh()
            }
        }
        val recyclerView = binding.ordersList
        recyclerView.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                recyclerView.context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            )
        )
        adapter = OrdersManagementAdapter(requireContext(), supplierOrdersData, stockHistoryData)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.setCheckboxListener(this)

        return binding.root
    }

    private fun approve() {
        APIFetcher(requireActivity()).verifyItem(toApprove) { s ->
            if (s) {
                binding.loading.visibility = View.GONE
                binding.approveBtn.isEnabled = true
                Toast.makeText(requireContext(), "Data updated", Toast.LENGTH_SHORT).show()
                setTotalText(filteredSupplierOrdersData)
                requireActivity().runOnUiThread {
                    toApprove.forEach { (key, _) ->
                        filteredSupplierOrdersData.find {
                            it.supplierOrderId == key
                        }?.let {
                            it.approveUpdated = true
                            it.approvedDate =
                                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                    .toString()
                        }
                    }
                    adapter.setData(filteredSupplierOrdersData, stockHistoryData)
                }
            } else {
                Toast.makeText(requireContext(), "Could not update", Toast.LENGTH_SHORT).show()
                requireActivity().runOnUiThread {
                    adapter.setData(filteredSupplierOrdersData, stockHistoryData)
                }
            }
        }
    }

    private fun paid(currentItem: SupplierOrder): Boolean {
        val amount = BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP)
        val total = BigDecimal(currentItem.quantity * currentItem.unitPrice).setScale(
            2,
            BigDecimal.ROUND_HALF_UP
        )
        var paymentExist = false
        for (item in supplierOrdersData) {
            if (item.invoiceId == currentItem.invoiceId) {
                for (payment in paymentsData) {
                    if (payment.invoiceID == currentItem.invoiceId.toInt() && payment.executed) {
                        amount.add(BigDecimal(item.unitPrice * item.quantity))
                        paymentExist = true
                    }
                }
            }
        }
        return if (paymentExist) {
            amount.compareTo(total) == 0
        } else {
            false
        }
    }

    private fun setTotalText(data: List<SupplierOrder>) {
        total = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_EVEN)
        totalApproved = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_EVEN)
        totalGrams = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_EVEN)
        totalApprovedGrams = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_EVEN)
        balanceTotal = BigDecimal(0.0).setScale(2, BigDecimal.ROUND_HALF_EVEN)
        for (order in data) {
            if (order.unit == "pieces") {
                if (toApprove.containsKey(order.supplierOrderId)) {
                    totalApproved = totalApproved.add(
                        BigDecimal(order.unitPrice * order.quantity).setScale(
                            2, BigDecimal.ROUND_HALF_UP
                        )
                    )
                } else {
                    total = total.add(
                        BigDecimal(order.unitPrice * order.quantity).setScale(
                            2, BigDecimal.ROUND_HALF_UP
                        )
                    )
                }
            } else {
                if (toApprove.containsKey(order.supplierOrderId)) {
                    totalApprovedGrams = totalApprovedGrams.add(
                        BigDecimal(order.grams).setScale(
                            2, BigDecimal.ROUND_HALF_UP
                        )
                    )
                } else {
                    totalGrams = totalGrams.add(
                        BigDecimal(order.grams).setScale(
                            2, BigDecimal.ROUND_HALF_UP
                        )
                    )
                }
            }
        }
        stockHistoryData.forEach { line ->
            balanceTotal = if (line.type == 1) {
                balanceTotal.add(
                    BigDecimal(line.quantity).setScale(
                        2,
                        BigDecimal.ROUND_HALF_UP
                    )
                )
            } else {
                balanceTotal.subtract(
                    BigDecimal(line.quantity).setScale(
                        2,
                        BigDecimal.ROUND_HALF_UP
                    )
                )
            }
        }
        binding.total.text = "$total CHF"
        binding.totalApproved.text = "$totalApproved CHF"
        binding.totalBalance.text = "${balanceTotal}g"
        binding.totalApprovedGrams.text = "${totalApprovedGrams}g"
        binding.totalGrams.text = "${totalGrams}g"
    }

    private fun filter(filter: String) {
        when (filter) {
            "All" -> {
                filteredSupplierOrdersData = supplierOrdersData
            }
            "Approved & unpaid" -> {
                filteredSupplierOrdersData = supplierOrdersData.filter {
                    it.approved.isNotEmpty() && !paid(it)
                }.toMutableList()
            }
            "Approved & paid" -> {
                filteredSupplierOrdersData = supplierOrdersData.filter {
                    it.approved.isNotEmpty() && paid(it)
                }.toMutableList()
            }
        }
        requireActivity().runOnUiThread {
            setTotalText(filteredSupplierOrdersData)
            adapter.setData(filteredSupplierOrdersData, stockHistoryData)
        }
    }

    private fun refresh() {
        requireActivity().runOnUiThread {
            binding.loading.visibility = View.VISIBLE
        }
        loadHistory(supplierSelected)
        APIFetcher(requireActivity()).getSupplierOrders(
            supplierSelected,
            dateSelected,
            dateFrom
        ) { data, s ->
            supplierOrdersData = data
            filteredSupplierOrdersData = data
            if (s) {
                APIFetcher(requireActivity()).getPayments { payments, s2 ->
                    if (s2) {
                        requireActivity().runOnUiThread {
                            paymentsData = payments
                            adapter.setData(filteredSupplierOrdersData, stockHistoryData)
                            binding.loading.visibility = View.GONE
                            setTotalText(data)
                        }
                    }
                }
            }
        }
    }

    override fun onCheckboxClicked(data: SupplierOrder, isChecked: Boolean) {
        val approver = binding.approvalSpinner.selectedItem.toString()
        if (isChecked) {
            toApprove[data.supplierOrderId] = approver
            data.approved = approver
        } else {
            toApprove.remove(data.supplierOrderId)
            data.approved = ""
        }
        Log.d("toApprove $isChecked", toApprove.toString())
        adapter.updateCheck(data)
    }

    private fun loadSuppliers() {
        requireActivity().runOnUiThread {
            binding.loading.visibility = View.VISIBLE
        }
        APIFetcher(requireActivity()).getSupplierRecipients { data, s ->
            requireActivity().runOnUiThread {
                binding.loading.visibility = View.GONE
                suppliers += "All"
                for (i in data) {
                    suppliers += i.name
                }
                if (s) {
                    if (data.isNotEmpty()) {
                        val spinnerAdapter = ArrayAdapter(
                            requireContext(), android.R.layout.simple_spinner_item, suppliers
                        )
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.suppliers.adapter = spinnerAdapter
                    }
                }
            }
        }
    }

    private fun loadApprovers() {
        requireActivity().runOnUiThread {
            binding.loading.visibility = View.VISIBLE
        }
        APIFetcher(requireActivity()).getPeopleThatCanApprove { data, s ->
            requireActivity().runOnUiThread {
                binding.loading.visibility = View.GONE
                for (i in data) {
                    approvers += i["OptionValue"]!!
                }
                if (s) {
                    if (data.isNotEmpty()) {
                        val spinnerAdapter = ArrayAdapter(
                            requireContext(), android.R.layout.simple_spinner_item, approvers
                        )
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.approvalSpinner.adapter = spinnerAdapter
                    }
                }
            }
        }
    }

    private fun loadHistory(supplier: String) {
        requireActivity().runOnUiThread {
            binding.loading.visibility = View.VISIBLE
        }
        APIFetcher(requireActivity()).getStockHistory(supplier) { data, s ->
            if (s) {
                stockHistoryData = data.toMutableList()
            }
        }
    }
}