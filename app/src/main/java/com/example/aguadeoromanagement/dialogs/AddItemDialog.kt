package com.example.aguadeoromanagement.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.adapters.invoiceDetails.InOutsAdapter
import com.example.aguadeoromanagement.fragments.InvoiceDetails
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.InvoiceItem
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.models.SupplierOrderMain
import com.example.aguadeoromanagement.networking.APIFetcher
import kotlinx.coroutines.*
import org.w3c.dom.Text
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class AddItemDialog(
    private val item: InvoiceItem? = null,
    private val stock: StockHistory? = null
) {
    fun showDialog(
        context: FragmentActivity,
        invoice: Invoice,
        supplierOrderMains: List<SupplierOrderMain>,
        invoiceDetails: InvoiceDetails? = null
    ) {
        Log.e("orders", supplierOrderMains.toString())
        val supOrderNumbers = mutableListOf<String>()
        supOrderNumbers.addAll(supplierOrderMains.map { supplierOrderMain -> supplierOrderMain.supplierOrderNumber })

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Add item")
        val view = context.layoutInflater.inflate(
            R.layout.dialog_add_invoice_item,
            null
        )
        val approvedBySpinner = view.findViewById<Spinner>(R.id.approvedBySpinner)
        val unitSpinner = view.findViewById<Spinner>(R.id.unitSpinner)
        val flowSpinner = view.findViewById<Spinner>(R.id.flowSpinner)
        val processSpinner = view.findViewById<Spinner>(R.id.processSpinner)
        val supplierOrderSpinner = view.findViewById<Spinner>(R.id.spinnerSupplierOrderNumbers)
        val recyclerInOuts = view.findViewById<RecyclerView>(R.id.recyclerViewInOuts)
        val textViewProduct = view.findViewById<EditText>(R.id.product)
        val textViewQuantity = view.findViewById<EditText>(R.id.quantity)
        val textViewUnitPrice = view.findViewById<EditText>(R.id.unitPrice)
        val textViewAccountCode = view.findViewById<EditText>(R.id.accountCode)
        val textViewVat = view.findViewById<EditText>(R.id.vat)
        val textViewDetail = view.findViewById<EditText>(R.id.details)
        val textViewInstruction = view.findViewById<TextView>(R.id.textViewInstruction)
        val textViewStatus = view.findViewById<TextView>(R.id.textViewStatus)

        val buttonSearchProduct = view.findViewById<ImageView>(R.id.buttonSearchProduct)
        val textViewResultSearch = view.findViewById<TextView>(R.id.textViewResultProductSearch)
        val progressBarSearchProduct = view.findViewById<ProgressBar>(R.id.progressBarSearchProduct)


        val apiFetcher = APIFetcher(context)
        val approvers = mutableListOf<String>()
        val units = mutableListOf<String>()
        val flows = mutableListOf<String>()
        val processes = mutableListOf<String>()
        val stockHistories = mutableListOf<StockHistory>()

        //initialisation
        textViewVat.setText(invoiceDetails?.currentVAT.toString())
        textViewAccountCode.setText(invoiceDetails?.currentAccountCode)




        units.add("pieces")
        units.add("Gramms")
        apiFetcher.getOptionValues { optionValues, s ->
            if (s) {
                optionValues.forEach {
                    if (it.type == "Approval") {
                        approvers.add(it.optionValue)
                    }
                }
                apiFetcher.getFlows { strings, s2 ->
                    if (s2) {
                        flows.addAll(strings)
                        apiFetcher.getProcesses { strings2, s3 ->
                            processes.addAll(strings2)
                        }
                    }
                }
            }
        }

        fun activateDisableFlowSpinner(yes: Boolean) {
            flowSpinner.isEnabled = yes
            processSpinner.isEnabled = yes
            textViewVat.setText(if (!yes) invoiceDetails?.currentVAT.toString() else "0")
            textViewVat.isEnabled = !yes
            textViewAccountCode.setText(if (!yes) invoiceDetails?.currentAccountCode else "0")
            textViewAccountCode.isEnabled = !yes

        }

        fun getSupplierOrderFromNumber(number: String): SupplierOrderMain? {
            return supplierOrderMains.find { supplierOrderMain -> supplierOrderMain.supplierOrderNumber == number }
        }


        approvedBySpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            approvers
        )
        unitSpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            units
        )
        flowSpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            flows
        )
        processSpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            processes
        )

        activateDisableFlowSpinner(textViewProduct.text.isNotEmpty())

        textViewProduct.addTextChangedListener(
            object : TextWatcher {

                override fun afterTextChanged(s: Editable) {
                    activateDisableFlowSpinner(s.toString().isNotEmpty())


                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {

                }
            }
        )

        recyclerInOuts.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )



        if (item != null) {
            textViewProduct.setText(item.product.toString())
            textViewDetail.setText(item.detail)
            textViewAccountCode.setText(item.accountCode)
            textViewQuantity.setText(item.quantity.toString())
            textViewVat.setText(item.vat.toString())
            textViewUnitPrice.setText(item.unitPrice.toString())

            supOrderNumbers.add(item.supplierOrderID)
        }

        if (stock != null) {
            textViewProduct.setText(stock.productId.toString())
            textViewDetail.setText(stock.detail)
            textViewQuantity.setText(stock.quantity.toString())
            supOrderNumbers.add(stock.orderNumber)
        }

        supplierOrderSpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            supOrderNumbers
        )

        supplierOrderSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                @SuppressLint("SetTextI18n")
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    invoiceDetails?.currentSupplierOrderIndex = p2
                    apiFetcher.getStockHistoryForOrder(1, supOrderNumbers[p2]) { stock, s ->
//                    Log.e("Stock", stock.map { st->st.productId }.toString())
                        stockHistories.removeAll(stockHistories)
                        stockHistories.addAll(stock)
                        recyclerInOuts.adapter = InOutsAdapter(stockHistories, onClick = {
                            textViewProduct.setText(it.productId.toString())
                        })
                        val so = getSupplierOrderFromNumber(supOrderNumbers[p2])
                        if (so != null) {
                            textViewInstruction.text = "${so.instruction} - ${so.remark}"
                            textViewStatus.text = so.status
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
//                TODO("Not yet implemented")
                }
            }

        supplierOrderSpinner.setSelection(invoiceDetails?.currentSupplierOrderIndex!!)

        buttonSearchProduct.setOnClickListener {
            val product = textViewProduct.text.toString()
            if (product.isNotEmpty()) {
                progressBarSearchProduct.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        Log.e("yo", "go")
                        apiFetcher.getProduct(product.toInt()) { product, s ->
                            Log.e("yo", "finito")
                            launch {
                                withContext(Dispatchers.Main) {
                                    if (s) {
                                        Log.e("product", product.toString())
                                        textViewResultSearch.text =
                                            product!!.productCode + " - " + product!!.description
                                        val unit = product!!.unit
                                        if (unit.isNotEmpty()) {
                                            if (unit == "Gramms") {
                                                unitSpinner.setSelection(1)
                                            }
                                            if (unit == "Pieces") {
                                                unitSpinner.setSelection(0)

                                            }
                                        }
                                        progressBarSearchProduct.visibility = View.GONE

                                    } else {
                                        progressBarSearchProduct.visibility = View.GONE
                                        textViewResultSearch.text = "no product found..."

                                    }
                                }
                            }
                        }
                    }

                }


            }
        }


        // --- end view ---


        builder.setView(view)


        var positive_text = if (item == null) "Add Item" else "Save"

        builder.setPositiveButton(positive_text) { dialog, _ ->

            if (textViewProduct.text.toString().isEmpty()) {
                invoiceDetails?.currentVAT = textViewVat.text.toString().toDoubleOrNull() ?: 0.0
                invoiceDetails?.currentAccountCode = textViewAccountCode.text.toString()
            }

            try {
                var soid = "";
                if (supplierOrderSpinner.selectedItem != null) {
                    soid = supplierOrderSpinner.selectedItem.toString()
                }
                val newItem = InvoiceItem(
                    invoiceId = invoice.id,
                    product = textViewProduct.text.toString()
                        .toIntOrNull() ?: 0,
                    quantity = textViewQuantity.text.toString()
                        .toDoubleOrNull() ?: 0.0,
                    unitPrice = textViewUnitPrice.text.toString()
                        .toDoubleOrNull() ?: 0.0,
                    accountCode = textViewAccountCode.text.toString(),
                    approved = approvedBySpinner.selectedItem.toString(),
                    approvedDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        .toString(),
                    unit = unitSpinner.selectedItem.toString(),
                    supplierOrderID = soid,
                    flow = flowSpinner.selectedItem.toString(),
                    process = processSpinner.selectedItem.toString(),
                    vat = textViewVat.text.toString().toDoubleOrNull()
                        ?: 0.0,
                    detail = textViewDetail.text.toString(),
                    uuid = UUID.randomUUID().toString(),
                    orderNumber = supplierOrderMains.find { it.supplierOrderNumber == soid }?.orderNumber
                        ?: "0"
                )
                Log.e("new item", newItem.toString())

//                if (newItem.product == 0) {
                if (item == null) {
                    invoiceDetails.invoiceItems.add(newItem)
                } else {
                    val itemInList = invoiceDetails.invoiceItems.indexOf(item)
                    newItem.id = item.id
                    invoiceDetails.invoiceItems[itemInList] = newItem
                    Log.e("item index", itemInList.toString())
                    if (item.id > 0) {
                        invoiceDetails.itemsToUpdate.add(item.id)
                    }
                }
                invoiceDetails.update()

//
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    "Error: could not insert invoice, check the fields are correctly filled",
                    Toast.LENGTH_SHORT
                ).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }


}