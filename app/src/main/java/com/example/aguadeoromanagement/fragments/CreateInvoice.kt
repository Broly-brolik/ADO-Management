package com.example.aguadeoromanagement.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.adapters.CreateInvoiceStockAdapter
import com.example.aguadeoromanagement.databinding.FragmentCreateInvoiceBinding
import com.example.aguadeoromanagement.models.*
import com.example.aguadeoromanagement.networking.APIFetcher
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CreateInvoice : Fragment() {
    private val binding by lazy {
        FragmentCreateInvoiceBinding.inflate(
            layoutInflater
        )
    }
    private val args by navArgs<CreateInvoiceArgs>()
    private var supplierOrders: Array<SupplierOrderMain> = arrayOf()
    private var optionValues: List<OptionValue> = listOf()
    private var stockHistory: List<StockHistory> = listOf()
    private lateinit var supplier: Contact
    private lateinit var newInvoice: Invoice

    private val formatter = Constants.forUsFormatter
    val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()

    private val toSelectHistory: MutableSet<Int> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args.supplierOrderMain.let {
            supplierOrders = it
        }
        supplier = args.supplier
        (activity as? AppCompatActivity)?.supportActionBar?.title = supplier.name

        val apiFetcher = APIFetcher(requireActivity())

        optionValues = ManagementApplication.getAppOptionValues()

        apiFetcher.getStockHistory(supplier.name, onlyHistory = true) { data2, s2 ->
            if (s2) {
                stockHistory = data2
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error getting stock history",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nowFormatted = now.format(Constants.forUsFormatter)


        fun setDueOnDate() {
            try {
                val daysFromNow = binding.dueOn.text.toString().toInt()
                binding.dueDate.setText(
                    now.plusDays(daysFromNow.toLong()).format(Constants.forUsFormatter)
                )
            } catch (_: Exception) {

            }
        }
        binding.invoiceDate.setOnClickListener {
            displayDate(1, "Invoice Date")
        }
        binding.invoiceDate.setText(nowFormatted)
        binding.receivedDate.setOnClickListener {
            displayDate(2, "Received Date")
        }
        binding.receivedDate.setText(nowFormatted)

        binding.dueOn.setText("30")
        setDueOnDate()
        binding.dueOn.doAfterTextChanged { setDueOnDate() }

        binding.dueDate.setOnClickListener {
            displayDate(3, "Due Date")
        }


        val currencies: MutableList<String> = mutableListOf()
        val approval: MutableList<String> = mutableListOf()
        val instructions: MutableList<String> = mutableListOf()
        optionValues.forEach { optionValue ->
            if (optionValue.type == "Currency") {
                currencies.add(optionValue.optionValue)
            } else if (optionValue.type == "Approval") {
                approval.add(optionValue.optionValue)
            }
        }
        instructions.add(" - ")
        supplierOrders.forEach {
            instructions.add(it.supplierOrderNumber + " " + it.instruction)
        }
        val currencyAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currency.adapter = currencyAdapter
        val approvalAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, approval)
        approvalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.registeredBy.adapter = approvalAdapter


//        val ordersAdapter =
//            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, instructions)
//        ordersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.ordersSpinner.adapter = ordersAdapter

        val recyclerView = binding.recyclerViewStockCreateInvoice
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                recyclerView.context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            )
        )

        val supplierOrders = stockHistory.map { it.orderNumber }.distinct()
        val stockHistoryByOrders = mutableMapOf<String, MutableList<StockHistory>>()
        supplierOrders.forEach {
            stockHistoryByOrders.putIfAbsent(it, mutableListOf())
            stockHistoryByOrders[it]!!.addAll(stockHistory.filter { stock->stock.orderNumber == it })
        }

        Log.e("stock historyByorder", stockHistoryByOrders.toString())
        val adapter = CreateInvoiceStockAdapter(
            stockHistory,
            stockHistoryByOrders,
            toSelectHistory,
            requireActivity(),
            requireContext()
        )

        recyclerView.adapter = adapter

//        binding.addButton.setOnClickListener {
//            if (binding.ordersSpinner.selectedItemPosition != 0) {
//                val orderToInsert = supplierOrders[binding.ordersSpinner.selectedItemPosition - 1]
//                adapter.addSupplierOrder(orderToInsert)
//            } else {
//                adapter.addEmpty()
//            }
//        }
//        binding.removeButton.setOnClickListener {
//            adapter.removeLastSupplierOrder()
//        }
        binding.saveButton.setOnClickListener {
//            val supplierOrdersSelected = adapter.getSuppliersOrders()
            saveInvoice()
        }
    }

    private fun displayDate(type: Int, title: String) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        datePicker.show(parentFragmentManager, "DATE_PICKER")
        datePicker.addOnPositiveButtonClickListener {
            val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()

            val formattedDate = date.format(Constants.forUsFormatter)
            when (type) {
                1 -> {
                    binding.invoiceDate.setText(formattedDate.toString())
                    binding.receivedDate.performClick()
                }
                2 -> {
                    binding.receivedDate.setText(formattedDate.toString())
                }
                3 -> {

                    val diff = ChronoUnit.DAYS.between(now, date)

                    if (diff < 0) {
                        return@addOnPositiveButtonClickListener
                    }

                    binding.dueOn.setText(diff.toString())
                    binding.dueDate.setText(formattedDate)
                }
            }
        }
    }

    private fun saveInvoice() {


        val createdDate = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
            .format(formatter)

        val invoiceDate = LocalDate.parse(binding.invoiceDate.text.toString(), formatter)


        val receivedDate = LocalDate.parse(binding.receivedDate.text.toString(), formatter)
            .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        val deadline = invoiceDate.plusDays(binding.dueOn.text.toString().toLong())

        Log.e("deadline", deadline.toString())


        var newInvoice = Invoice(
            id = 0,
            supplierId = supplier.idToInsert,
            invoiceDate = invoiceDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
            invoiceNumber = binding.invoiceNumber.text.toString(),
            currency = binding.currency.selectedItem.toString(),
            receivedDate = receivedDate,
            createdDate = createdDate,
            registeredBy = binding.registeredBy.selectedItem.toString(),
            remark = binding.remark.text.toString(),
            dueOn = deadline.format(DateTimeFormatter.ISO_DATE),
            discountPercentage = binding.discount.text.toString().toDoubleOrNull() ?: 0.0,
            amount = 0.0,
            items = listOf(),
            paid = 0.0,
            remain = 0.0,
            status = "Open"
        )
//        Log.e("NEW INVOICE", newInvoice.toString())
//        Log.e("due on", newInvoice.dueOn

//        lifecycleScope.launch {
        val history = mutableListOf<StockHistory>()
        toSelectHistory.forEach {
            history.add(stockHistory[it])
        }
//            linkStockHistoryToInvoice(
//                newInvoice.invoiceNumber,
//                history.toList()
//            )
//        }
        val action =
            CreateInvoiceDirections.actionCreateInvoiceToInvoiceDetails(
                newInvoice,
                supplier.name,
                history.toTypedArray(),
            )

        findNavController().navigate(action)
//        requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()

    }
}