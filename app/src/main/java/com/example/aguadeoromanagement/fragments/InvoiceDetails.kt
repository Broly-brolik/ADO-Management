package com.example.aguadeoromanagement.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.MainActivity
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.adapters.invoiceDetails.InvoiceDetailsAdapter
import com.example.aguadeoromanagement.adapters.invoiceDetails.InvoiceHistoryAdapter
import com.example.aguadeoromanagement.databinding.FragmentInvoiceDetailsBinding
import com.example.aguadeoromanagement.dialogs.AddItemDialog
import com.example.aguadeoromanagement.models.*
import com.example.aguadeoromanagement.networking.*
import com.example.aguadeoromanagement.networking.api.*
import com.example.aguadeoromanagement.utils.toPrice
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class InvoiceDetails : Fragment() {
    private val binding by lazy {
        FragmentInvoiceDetailsBinding.inflate(
            layoutInflater
        )
    }
    private val args by navArgs<InvoiceDetailsArgs>()
    private lateinit var invoice: Invoice
    private var supplierOrders: MutableList<SupplierOrderMain> = mutableListOf()
    var invoiceItems: MutableList<InvoiceItem> = mutableListOf()
    var itemsToUpdate: MutableSet<Int> = mutableSetOf()
    private var supplierName = ""
    private var optionValues: List<OptionValue> = listOf()
    private lateinit var apiFetcher: APIFetcher

    lateinit var recyclerView: RecyclerView

    lateinit var textViewTotal: TextView
    lateinit var textViewRemain: TextView
    lateinit var textViewDiscount: TextView
    private lateinit var buttonSave: Button
    private lateinit var buttonAdd: Button
    var currentSupplierOrderIndex = 0
    var currentAccountCode = "4200"
    var currentVAT = 7.7

    private val gson = Gson()
    private val prefs = ManagementApplication.getAppPref()
    private val jsonVAT = prefs.getString("defaultVAT", "")
    private val jsonAccountCode = prefs.getString("defaultAccountCode", "")
    private val mapTypeVAT: Type = object : TypeToken<HashMap<String, Double>>() {}.type
    private val mapTypeAccountCode: Type = object : TypeToken<HashMap<String, String>>() {}.type
    private var defaultVATValues =
        gson.fromJson<HashMap<String, Double>>(jsonVAT, mapTypeVAT) ?: HashMap()
    private var defaultAccountCodeValues =
        gson.fromJson<HashMap<String, String>>(jsonAccountCode, mapTypeAccountCode) ?: HashMap()

    val toShow = mutableMapOf<String, Boolean>()

    private lateinit var recyclerViewHistory: RecyclerView
    private var invoiceHistory: List<InvoiceHistory> = listOf()

    val job = Job()

    lateinit var invoiceDetailsAdapter: InvoiceDetailsAdapter

    var stockHistoryForOrders: Map<String, MutableList<StockHistory>> = mutableMapOf()

    private lateinit var mainContentLayout: ConstraintLayout
    lateinit var progressLayout: LinearLayout
    private lateinit var progressStatus: TextView
    private lateinit var selectedStockHistory: List<StockHistory>
    private lateinit var stockHistory: List<StockHistory>
    private var fromCreateInvoice = false;


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_invoice_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_payment -> {
//                Toast.makeText(requireContext(), "yo", Toast.LENGTH_SHORT).show()
                if (invoice.id > 0) {
                    showPaymentDialog()
//                    showUpdateStatusDialog()
                } else {
                    Toast.makeText(
                        ManagementApplication.getAppContext(),
                        "Please save invoice before adding a payment",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            R.id.action_add_invoice_history -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.dialog_add_invoice_history,
                    null
                )
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Add History")
                builder.setView(view)
                builder.create()
                builder.setCancelable(false)

                builder.setPositiveButton("Add") { dialog, _ ->
                    val message =
                        view.findViewById<EditText>(R.id.editTextInvoiceHistoryRemark).text.toString()
                    CoroutineScope(job + Dispatchers.Main).launch {
                        updateHistory(message)
                    }
                    dialog.dismiss()

                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                if (invoice.id > 0) {
                    builder.show()
                } else {
                    Toast.makeText(
                        ManagementApplication.getAppContext(),
                        "Please save invoice before modifying its history",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            R.id.action_home -> {
                findNavController().navigate(R.id.action_invoiceDetails_to_FirstFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend fun updateHistory(message: String) {
        insertInHistory(
            invoice.id,
            message
        )
        invoiceHistory = getInvoiceHistory(invoice.id)
        recyclerViewHistory.adapter = InvoiceHistoryAdapter(invoiceHistory, invoice, requireActivity(), recyclerViewHistory)
    }

    private fun resetBackButton() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.back_button_action = {
            try {
                findNavController().popBackStack()
            } catch (_: Exception) {
            }
        }
        mainActivity.onBackPressedDispatcher.addCallback(this) {
            try {
                findNavController().popBackStack()
            } catch (_: Exception) {
            }
        }
    }

    private fun changeBackButton() {
        //            Toast.makeText(activity.getApplicationContext(), contact.getContacts(), Toast.LENGTH_SHORT).show();
        val bundle = Bundle()
        bundle.putInt("contact_id", 0)
        bundle.putString("contact_name", supplierName)

        val mainActivity = requireActivity() as MainActivity
        mainActivity.back_button_action = {
            findNavController().navigate(R.id.action_invoiceDetails_to_suppliersManagement, bundle)
//            findNavController().popBackStack()
//            findNavController().popBackStack()
//            resetBackButton()
        }
        mainActivity.onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_invoiceDetails_to_suppliersManagement, bundle)
//            findNavController().popBackStack()
//            findNavController().popBackStack()
//            resetBackButton()
        }
    }

    fun showPaymentDialog() {
        val view = requireActivity().layoutInflater.inflate(
            R.layout.dialog_add_payment,
            null
        )
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Payment")

        val editTextAmount = view.findViewById<EditText>(R.id.amount)
        val spinnerPaymentMethod = view.findViewById<Spinner>(R.id.spinnerPaymentMethod)
        val spinnerCheckedBy = view.findViewById<Spinner>(R.id.spinnerApproval)
        val dateP = view.findViewById<DatePicker>(R.id.date)

//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatter = DateTimeFormatter.ISO_DATE
        val formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        var paymentDate: LocalDate? = null


        try {
            paymentDate = LocalDate.parse(invoice.dueOn, formatter)
        } catch (e: Exception) {
            paymentDate = LocalDate.parse(invoice.dueOn, formatter2)
        }
        Log.e("created date", paymentDate.toString())
//        Log.e("year", paymentDate.monthValue.toString())
        if (paymentDate != null) {
            dateP.updateDate(
                paymentDate.year,
                paymentDate.monthValue - 1,
                paymentDate.dayOfMonth
            )
        }


        val paymentMethods =
            optionValues.filter { optionValue -> optionValue.type == "SupplierPaymentMethod" }
                .map { optionValue -> optionValue.optionValue }
        spinnerPaymentMethod.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, paymentMethods
        )

        editTextAmount.setText(invoice.remain.toPrice().toString())

        val approvals = optionValues.filter { optionValue -> optionValue.type == "Approval" }
            .map { optionValue -> optionValue.optionValue }

        val approvalAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, approvals)
        approvalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCheckedBy.adapter = approvalAdapter

        builder.setView(view)
        builder.create()
        builder.setCancelable(false)


        builder.setPositiveButton("Add") { dialog, _ ->
            val mode = spinnerPaymentMethod.selectedItem.toString()
            val amount = view.findViewById<EditText>(R.id.amount).text.toString()
            val remark = view.findViewById<EditText>(R.id.remark).text.toString()

            val c = Calendar.getInstance()
            c.set(dateP.year, dateP.month, dateP.dayOfMonth)
            val date = c.time
            val dateFormat: DateFormat =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val strDate = "${dateP.month + 1}/${dateP.dayOfMonth}/${dateP.year}"


            val payment = Payment(
                id = 0,
                amountPaid = amount.toDouble(),
                remarks = remark,
                by = mode,
                scheduledDate = strDate,
                invoiceID = invoice.id,
                executed = false,
                checkedBy = spinnerCheckedBy.selectedItem.toString()
            )

            CoroutineScope(job + Dispatchers.Main).launch {
                val id = createPayment(payment)
                if (id > 0) {
                    payment.id = id
                    invoiceHistory = getInvoiceHistory(invoice.id)
                    recyclerViewHistory.adapter = InvoiceHistoryAdapter(invoiceHistory, invoice, requireActivity(), recyclerViewHistory)
                    Toast.makeText(requireContext(), "payment created", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error creating payment",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (invoiceItems.any { invoiceItem -> invoiceItem.supplierOrderID.isNotEmpty() } || stockHistory.any { it.orderNumber.isNotEmpty() }) {
                    showUpdateStatusDialog()
                }
                dialog.dismiss()
            }




            return@setPositiveButton
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            if (invoiceItems.any { invoiceItem -> invoiceItem.supplierOrderID.isNotEmpty() } || stockHistory.any { it.orderNumber.isNotEmpty() }) {
                showUpdateStatusDialog()
            }
            dialog.dismiss()

        }


        builder.show()

    }

    fun showUpdateStatusDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Update Status")
        val context = requireContext()

        val view = LinearLayout(requireContext())
        view.orientation = LinearLayout.VERTICAL

        val supplierOrderInInvoice =
            invoiceItems.map { invoiceItem -> invoiceItem.supplierOrderID }.distinct()
                .toMutableList()

        supplierOrderInInvoice.addAll(stockHistory.map { it.orderNumber }.distinct())
        CoroutineScope(Dispatchers.Main + job).launch {

            val supplierOrderMainInInvoice =
                getSupplierOrderMainHistoryForNumbers(supplierOrderInInvoice.toList())


            val spinnerList = mutableListOf<Spinner>()

            supplierOrderMainInInvoice.forEach { supplierOrderMain ->

                val layout = LinearLayout(context)
                Log.e("sup", supplierOrderMain.supplierOrderNumber)
                val textView = TextView(context)
                textView.text = supplierOrderMain.supplierOrderNumber

                layout.addView(textView)

                val spinner = Spinner(context)

                val supplierOrderStatus =
                    optionValues.filter { optionValue -> optionValue.type == "SupplierOrderStatus" }
                        .map { optionValue -> optionValue.optionValue }

                spinner.adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_dropdown_item, supplierOrderStatus
                )
                spinner.setSelection(supplierOrderStatus.indexOf(supplierOrderMain.status))

                spinnerList.add(spinner)

                layout.addView(spinner)

                view.addView(layout)

            }

            builder.setPositiveButton("Save") { dialog, _ ->
                supplierOrderMainInInvoice.forEachIndexed { index, supplierOrderMain ->
                    val newStatus = spinnerList[index].selectedItem.toString()
                    Log.e("order", supplierOrderMain.supplierOrderNumber)
                    Log.e("index", newStatus)
                    var ok = true

                    apiFetcher.updateSupplierOrderStatus(
                        supplierOrderMain.supplierOrderNumber,
                        newStatus
                    ) { s ->
                        if (!s) {
                            ok = false
                        } else {
                            supplierOrderMain.status = newStatus
                        }
                        if (index == supplierOrderMainInInvoice.size - 1) {
                            if (ok) {
                                Toast.makeText(context, "update successful", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(context, "update fail", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }



            builder.setView(view)
            builder.create()
            builder.setCancelable(false)
            builder.show()
        }
    }

    private fun computeTotal(): Double {
        var p = invoiceItems.filter { invoiceItem -> invoiceItem.product == 0 }
            .fold(0.0) { acc, invoiceItem -> acc + (invoiceItem.quantity * invoiceItem.unitPrice * (1 + invoiceItem.vat / 100)) }
            .toPrice()
        p += stockHistory.fold(0.0) { acc, stock -> acc + (stock.cost * stock.quantity) }.toPrice()
        return p
    }

    private fun computeRemaining(): Double {
        return computeTotal() - computeDiscountAmount()
    }

    private fun computeDiscountAmount(): Double {
        return (computeTotal() * (invoice.discountPercentage) / 100).toPrice()
    }

    @SuppressLint("SetTextI18n")
    fun update(showDialogAfter: Boolean = true) {

        defaultVATValues[supplierName] = currentVAT
        defaultAccountCodeValues[supplierName] = currentAccountCode
        val editor = prefs.edit()
        editor.putString("defaultVAT", gson.toJson(defaultVATValues))
        editor.putString("defaultAccountCode", gson.toJson(defaultAccountCodeValues))
        editor.commit()
//        invoiceDetailsAdapter.notifyDataSetChanged()
        invoiceDetailsAdapter.updateItems(invoiceItems)
//        Log.e("count", invoiceDetailsAdapter.itemCount.toString())

//        recyclerView.adapter?.notifyItemInserted(recyclerView.adapter!!.itemCount)
//        recyclerView.adapter = InvoiceDetailsAdapter(
//            invoiceItems,
//            supplierName,
//            requireActivity(),
//            invoice,
//            toShow = toShow,
//            invoiceDetails = this@InvoiceDetails,
//            optionValues = optionValues
//
//        )

        val total = computeTotal()
        textViewTotal.text = total.toString() + " ${invoice.currency}"
        textViewRemain.text = computeRemaining().toString() + " ${invoice.currency}"
        invoice.remain = computeRemaining().toPrice()

        if (invoice.discountPercentage > 0) {
            textViewDiscount.text =
                "${computeDiscountAmount()} ${invoice.currency} (${invoice.discountPercentage}%)"

        } else {
            textViewDiscount.text = "no discount"
        }
        Log.e("new items", invoiceItems.toString())
        if (showDialogAfter) {
            buttonAdd.performClick()
        }

    }

    private suspend fun saveItems() {
        val newItems =
            invoiceItems.filter { invoiceItem -> invoiceItem.id == 0 }
        if (newItems.isEmpty()) {
            Toast.makeText(
                ManagementApplication.getAppContext(),
                "nothing to insert",
                Toast.LENGTH_SHORT
            ).show()
            updateItems()
            return
        }
        this@InvoiceDetails.progressStatus.text = "Inserting items"
        var okInsert = true

        newItems.forEachIndexed { index, newItem ->
            newItem.invoiceId = invoice.id
            val id = insertInvoiceItem(newItem, invoice)
            if (id > 0) {
                newItem.id = id
            } else {
                okInsert = false
            }
        }

        if (okInsert) {
            Toast.makeText(
                requireContext(),
                "Items inserted successfully",
                Toast.LENGTH_SHORT
            ).show()
            updateItems()
        } else {
            Toast.makeText(
                requireContext(),
                "Fail to insert items",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private suspend fun updateItems() {
        if (itemsToUpdate.isEmpty()) {
            Toast.makeText(
                ManagementApplication.getAppContext(),
                "nothing to delete",
                Toast.LENGTH_SHORT
            ).show()
            update(false)
            showPaymentDialog()
            return
        }
        this@InvoiceDetails.progressStatus.text = "Updating items"

        var okUpdate = true
        itemsToUpdate.forEach { id ->
            invoiceItems.find { item -> item.id == id }?.let {
                if (!updateInvoiceItem(it)) {
                    okUpdate = false
                }
            }
        }
        if (okUpdate) {
            Toast.makeText(
                ManagementApplication.getAppContext(),
                "items updated successfully",
                Toast.LENGTH_SHORT
            ).show()
            update(false)
            showPaymentDialog()
        } else {
            Toast.makeText(
                ManagementApplication.getAppContext(),
                "fail to update items",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun save() {
        Log.e("Save", "save")
        //TODO inclure update
        invoice.amount = computeTotal()
        invoice.remain = computeRemaining()
        invoice.discountAmount = computeDiscountAmount()
        CoroutineScope(Dispatchers.Main + job).launch {
            this@InvoiceDetails.progressLayout.visibility = View.VISIBLE
            mainContentLayout.visibility = View.GONE
            if (invoice.id > 0) {
                this@InvoiceDetails.progressStatus.text = "Updating invoice"
                val s = updateInvoice(invoice)
                if (s) {
                    updateHistory("Invoice updated")
                    saveItems()
                } else {
                    Toast.makeText(requireContext(), "Fail to update invoice", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                this@InvoiceDetails.progressStatus.text = "Inserting invoice"

                val newId = createInvoice(invoice)
                if (newId > 0) {

//                    changeBackButton()

                    invoice.id = newId
                    insertInHistory(invoice.id, "Invoice created")
                    val s = linkStockHistoryToInvoice(newId.toString(), stockHistory.map { it.id })
                    if (s) {
                        Toast.makeText(
                            requireContext(),
                            "Linking stock history successful",
                            Toast.LENGTH_LONG
                        ).show()
                        saveItems()
                    }

                } else {
                    Toast.makeText(requireContext(), "Fail to create invoice", Toast.LENGTH_LONG)
                        .show()
                }
            }

            this@InvoiceDetails.progressLayout.visibility = View.GONE
            this@InvoiceDetails.progressStatus.text = ""

            mainContentLayout.visibility = View.VISIBLE
        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO refactor all the process of getting the data
        super.onCreate(savedInstanceState)
//        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
//            Log.e("back", "pressed")
//        }



//        requireActivity().actionBar.home
        var start = System.currentTimeMillis();

        setHasOptionsMenu(true)

        args.let {
            invoice = it.invoice
            supplierName = it.contactName
            selectedStockHistory = it.stockHistory.toList()
            fromCreateInvoice = it.fromCreateInvoice
        }
        currentVAT = defaultVATValues.getOrDefault(supplierName, 7.7)
        currentAccountCode = defaultAccountCodeValues.getOrDefault(supplierName, "4200")
        apiFetcher = APIFetcher(requireActivity())
        Log.e("selected stock history", selectedStockHistory.toString())
        Log.e("from create invoice", fromCreateInvoice.toString())

    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            invoiceDetailsContent.visibility = View.GONE
            this@InvoiceDetails.progressLayout = progressLayout
            this@InvoiceDetails.progressStatus = progressStatus
            mainContentLayout = invoiceDetailsContent
        }
//        refresh()
        CoroutineScope(Dispatchers.Main + job).launch {
//            supplierOrderMainHistory =
//                getSupplierOrderMainHistory(supplierName).filter { it.status != "Status 3" && it.status != "Status 0" }
//                    .toMutableList()
//            Log.e("su main history", supplierOrderMainHistory.toString())
            optionValues = ManagementApplication.getAppOptionValues()
            if (invoice.id > 0) {
                invoiceItems = getInvoiceItems(invoice).toMutableList()
                stockHistory = getStockHistoryForInvoice(invoice.id)
            } else {
                stockHistory = selectedStockHistory

            }
            invoiceHistory = getInvoiceHistory(invoiceId = invoice.id)
//            Log.e("history", invoiceHistory.toString())
//            Log.e("stock history men", stockHistory.toString())

            refresh()
        }
    }

    fun refresh() {

        val invoiceDateDate: LocalDate = try {
            LocalDate.parse(
                invoice.createdDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            )
        } catch (e: Exception) {
            LocalDate.parse(
                invoice.createdDate, Constants.forUsFormatter
            )
        }

        var dueOnDate: LocalDate? = null
        if (invoice.dueOn.isNotEmpty()) {
            dueOnDate = try {
                LocalDate.parse(
                    invoice.dueOn, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
            } catch (e: Exception) {
                LocalDate.parse(
                    invoice.dueOn, DateTimeFormatter.ISO_DATE
                )
            }
        }


//        val deadline = invoiceDateDate.plusDays(invoice.dueOn.toLong())
//        dueDaysNumber = Duration.between(
//            LocalDateTime.now(),
//            deadline.atStartOfDay()
//        ).toDays().toInt()
        val status = mutableListOf<String>()
        optionValues.forEach {
            if (it.type == "InvoiceStatus") {
                status.add(it.optionValue)
            }
        }
        binding.apply {
            contact.text = supplierName
            invoiceNumber.text = invoice.invoiceNumber
            invoiceDate.text =
                invoiceDateDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
            currency.text = invoice.currency
            textViewDiscount = discount
            if (invoice.discountPercentage > 0) {
                discount.text =
                    "${computeDiscountAmount()} ${invoice.currency} (${invoice.discountPercentage}%)"

            } else {
                discount.text = "no discount"
            }

            registeredBy.text = invoice.registeredBy
            remark.text = invoice.remark
            dueDays.text = dueOnDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
            statusSpinner.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, status
            )
            statusSpinner.setSelection(status.indexOf(invoice.status), false)
            textViewTotal = total
            textViewTotal.text =
                computeTotal().toString() + " ${invoice.currency}"
            paid.text = "${invoice.paid} ${invoice.currency}"
            textViewRemain = remaining
            textViewRemain.text = "${invoice.remain} ${invoice.currency}"

            buttonSave = buttonSaveInvoice
            buttonAdd = buttonAddItem


            buttonSave.setOnClickListener {
                save()
            }

            this@InvoiceDetails.recyclerViewHistory = binding.recyclerViewHistory


            statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val previousStatus = invoice.status
                    invoice.status = status[position]
                    CoroutineScope(job + Dispatchers.Main).launch {
                        val s = updateInvoiceStatus(invoice)
                        if (s) {
                            Toast.makeText(requireContext(), "Status updated", Toast.LENGTH_SHORT)
                                .show()
                            updateHistory("status updated from $previousStatus to ${invoice.status}")

                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error updating status",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //Do nothing
                }
            }

            recyclerView = binding.invoiceItemsList
            recyclerView.layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )
            recyclerView.addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    recyclerView.context,
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
            )

            Log.e("stock passed to inout", stockHistory.toString())
            invoiceDetailsAdapter = InvoiceDetailsAdapter(
                invoiceItems,
                stockHistory,
                supplierName,
                requireActivity(),
                invoice,
                toShow = toShow,
                invoiceDetails = this@InvoiceDetails,
                optionValues = optionValues
            )
            recyclerView.adapter = invoiceDetailsAdapter


            recyclerViewHistory.layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )
            recyclerViewHistory.addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    recyclerView.context,
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
            )
//            progressBarHistory.visibility = View.GONE
//            recyclerViewHistory.adapter = InvoiceHistoryAdapter(invoiceHistory)
            recyclerViewHistory.adapter = InvoiceHistoryAdapter(invoiceHistory, invoice, requireActivity(), recyclerViewHistory)

            buttonAdd.setOnClickListener {
//                if (supplierOrderMainHistory.isEmpty()) {
//                    Toast.makeText(context, "no open orders", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
                val dialog = AddItemDialog()
                dialog.showDialog(
                    requireActivity(),
                    invoice,
                    listOf(),
                    this@InvoiceDetails
                )
            }

            if (invoice.id > 0) {
//                buttonAdd.visibility = View.GONE
//                buttonSave.visibility = View.GONE
            } else {
//                buttonAdd.performClick()
            }
            invoiceDetailsContent.visibility = View.VISIBLE
            this@InvoiceDetails.progressLayout.visibility = View.GONE
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
}