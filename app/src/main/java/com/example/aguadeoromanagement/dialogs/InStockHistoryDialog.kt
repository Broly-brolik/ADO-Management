package com.example.aguadeoromanagement.dialogs

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.models.*
import com.example.aguadeoromanagement.networking.Query
import com.example.aguadeoromanagement.networking.api.getOrderComponentById
import com.example.aguadeoromanagement.networking.api.searchProductById
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class InStockHistoryDialog(context: Activity? = null) {
    private lateinit var editTextProductId: EditText
    private lateinit var imageViewSearch: ImageView
    private lateinit var textViewCategory: TextView
    private lateinit var textViewSubcategory: TextView
    private lateinit var textViewType: TextView
    private lateinit var textViewProductCode: TextView
    private lateinit var typeSpinner: Spinner
    private lateinit var editTextQuantity: EditText
    private lateinit var editTextCost: EditText
    private lateinit var textViewTotal: TextView
    private lateinit var editTextDetail1: EditText
    private lateinit var editTextRemark: EditText
    private lateinit var processSpinner: Spinner
    private lateinit var flowSpinner: Spinner
    private lateinit var buttonVerify: Button
    val dateTime = DateTimeFormatter
        .ofPattern("yyyy/MM/dd HH:mm:ss")
        .withZone(ZoneOffset.ofHours(1))
        .format(Instant.now())

    // This property will hold the currently selected StockHistory record.
    private var selectedStockHistory: StockHistory? = null

    fun showDialog(context: Context, selectedItems: List<SupplierOrderMain>) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_add_item_stockhistory, null)
        val container: LinearLayout = dialogView.findViewById(R.id.containerSelectedItems)
        val containerStockHistory: TableLayout = dialogView.findViewById(R.id.containerStockHistory)
        val selectedItemsTextView: TextView = dialogView.findViewById(R.id.textViewSelectedItems)

        editTextProductId = dialogView.findViewById(R.id.editTextProductId)

        textViewCategory = dialogView.findViewById(R.id.TextViewCategory)
        textViewSubcategory = dialogView.findViewById(R.id.TextViewSubcategory)
        textViewType = dialogView.findViewById(R.id.TextViewType)
        textViewProductCode = dialogView.findViewById(R.id.TextViewProductCode)
        imageViewSearch = dialogView.findViewById(R.id.search)

        editTextDetail1 = dialogView.findViewById(R.id.editTextDetail1)
        typeSpinner = dialogView.findViewById(R.id.spinnerTextType)
        editTextQuantity = dialogView.findViewById(R.id.editTextQuantity)
        editTextCost = dialogView.findViewById(R.id.editTextCost)
        textViewTotal = dialogView.findViewById(R.id.TextViewTotalCost)
        editTextRemark = dialogView.findViewById(R.id.editTextRemark)
        processSpinner = dialogView.findViewById(R.id.spinnerTextProcess)
        flowSpinner = dialogView.findViewById(R.id.spinnerFlow)

        val adapterType = ArrayAdapter.createFromResource(context, R.array.type_array, android.R.layout.simple_spinner_item)
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapterType

        val adapterProcess = ArrayAdapter.createFromResource(context, R.array.process_array, android.R.layout.simple_spinner_item)
        adapterProcess.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        processSpinner.adapter = adapterProcess

        val adapterFlow = ArrayAdapter.createFromResource(context, R.array.flow_array, android.R.layout.simple_spinner_item)
        adapterFlow.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        flowSpinner.adapter = adapterFlow

        val textViewResume: TextView = dialogView.findViewById(R.id.textViewResume)
        buttonVerify = dialogView.findViewById(R.id.buttonVerify)
        val buttonSave: Button = dialogView.findViewById(R.id.buttonSave)
        var selectedOrder: String? = null

        setupDynamicTotal(editTextQuantity, editTextCost, textViewTotal)

        selectedItems.forEach { order ->
            val textView = TextView(context).apply {
                text = order.supplierOrderNumber.substringBeforeLast("_")
                textSize = 16f
                setPadding(8, 8, 8, 8)
                setTextColor(context.getColor(android.R.color.black))
                setOnClickListener {
                    toggleFieldVisibility(
                        listOf(
                            editTextProductId, imageViewSearch, textViewCategory, textViewSubcategory, buttonVerify,
                            textViewType, textViewProductCode, typeSpinner, editTextQuantity, editTextCost, textViewTotal,
                            processSpinner, flowSpinner, editTextDetail1, editTextRemark
                        )
                    )
                    selectedOrder = order.supplierOrderNumber.substringBeforeLast("_")
                    selectedItemsTextView.text = "$selectedOrder :"
                    selectedItemsTextView.setTextColor(Color.BLUE)
                    selectedItemsTextView.textSize = 20f
                    CoroutineScope(Dispatchers.Main).launch {
                        //val stockHistoryList = getOrderComponentBySupplier(selectedOrder!!)
                        val stockHistoryList = getOrderComponentById(order.id)
                        val sortedStockHistoryList: List<StockHistory> = stockHistoryList.sortedWith(
                            compareBy<StockHistory> { it.productId }
                                .thenByDescending { it.historicDate }
                        )
                        displayStockHistory(containerStockHistory, sortedStockHistoryList)
                    }
                }
            }
            container.addView(textView)
        }

        imageViewSearch.setOnClickListener {
            val productId = editTextProductId.text.toString().trim()
            if (productId.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    val product = searchProductById(productId)
                    val product2 = product.firstOrNull()
                    textViewCategory.text = product2?.category.toString() ?: ""
                    textViewSubcategory.text = product2?.subCategory.toString() ?: ""
                    textViewType.text = product2?.type
                    textViewProductCode.text = product2?.productCode
                }
            } else {
                Toast.makeText(context, "Invalid Product ID", Toast.LENGTH_SHORT).show()
            }
        }

        buttonVerify.setOnClickListener {
            val details = StringBuilder()
            if (textViewProductCode.visibility == View.VISIBLE) {
                val productId = editTextProductId.text.toString().toIntOrNull() ?: 0
                details.append("Product ID: $productId\n")
                /*val category = textViewCategory.text.toString()
                details.append("Category: $category\n")
                val subCategory = textViewSubcategory.text.toString()
                details.append("Subcategory: $subCategory\n")
                val type = textViewType.text.toString()
                details.append("Type: $type\n")
                val productCode = textViewProductCode.text.toString()
                details.append("Product Code: $productCode\n")*/
                val typeSpinnerStr = typeSpinner.selectedItem.toString()
                details.append("Type Spinner: $typeSpinnerStr\n")
                val process = processSpinner.selectedItem.toString()
                details.append("Process: $process\n")
                val flow = flowSpinner.selectedItem.toString()
                details.append("Flow: $flow\n")
                val quantity = editTextQuantity.text.toString().toDoubleOrNull() ?: 0.0
                details.append("Quantity: $quantity\n")
                val cost = editTextCost.text.toString().toDoubleOrNull() ?: 0.0
                details.append("Cost: $cost\n")
                val totalCost = textViewTotal.text.toString().trim()
                details.append("Total Cost: $totalCost\n")
                val detail = editTextDetail1.text.toString().trim()
                details.append("Detail: $detail\n")
            }
            textViewResume.text = details.toString()
            textViewResume.visibility = View.VISIBLE
        }

        buttonSave.setOnClickListener {
            val productID = editTextProductId.text.toString().toIntOrNull() ?: 0
            val detail = editTextDetail1.text.toString()
            val type = typeSpinner.selectedItemPosition + 1
            val quantity = editTextQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val cost = editTextCost.text.toString().toDoubleOrNull() ?: 0.0
            val totalCost = quantity * cost
            textViewTotal.text = totalCost.toString()
            val remark = editTextRemark.text.toString()
            val process = processSpinner.selectedItemPosition + 1
            val flow = flowSpinner.selectedItemPosition + 1
            if (validateInputs(quantity, cost)) {
            selectedItems.find { it.supplierOrderNumber.contains(selectedOrder ?: "") }?.let { order ->
                val supplier = selectedStockHistory?.supplier ?: "NaN"

                CoroutineScope(Dispatchers.IO).launch {
                            insertStockHistory(
                                context,
                                order.id,
                                order.supplierOrderNumber,
                                productID,
                                order.recipient,
                                //supplier,
                                detail = detail,
                                type = type,
                                quantity = quantity,
                                cost = cost,
                                remark = remark,
                                weight = 0.0,
                                loss = 0.0,
                                process = process,
                                flow = flow,
                            )
                        }
                    }
            }
            clearForm(dialogView as ViewGroup)
            textViewResume.text = ""
            textViewCategory.text = ""
            textViewSubcategory.text = ""
            textViewType.text = ""
            textViewProductCode.text = ""
        }

        AlertDialog.Builder(context)
            .setTitle("Stock History variations")
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun toggleFieldVisibility(fields: List<View>) {
        val visibility = if (fields.first().visibility == View.VISIBLE) View.GONE else View.VISIBLE
        fields.forEach { it.visibility = visibility }
    }

    private fun validateInputs(quantity: Double, cost: Double): Boolean {
        return quantity >= 0.0 && cost >= 0.0
    }

    private suspend fun insertStockHistory(
        context: Context,
        supplierOrderMainID: Int,
        supplierOrderNumber: String,
        productID: Int,
        recipient: String,
        detail: String,
        type: Int,
        quantity: Double,
        cost: Double,
        remark: String,
        weight: Double,
        loss: Double,
        process: Int,
        flow: Int,
    ) {
        val query = """
    insert into StockHistory1 (
        SupplierOrderMainID, OrderNumber, HistoricDate, ProductID, Supplier, Detail, Type, Quantity, Cost, Remark, Weight, Loss, Process, Flow, SettlementStatus
    ) VALUES (
        $supplierOrderMainID, '$supplierOrderNumber', #$dateTime#, $productID, '$recipient', '$detail', $type, $quantity, $cost, '$remark', $weight, $loss, '$process', $flow, 'Unverified'
    )
""".trimIndent()
        try {
            val q = Query(query)
            withContext(Dispatchers.IO) {
                q.execute(Constants.url)
            }
        } catch (e: Exception) {
            Log.e("InStockHistoryDialog", "Error inserting into StockHistory", e)
        }
    }

    private fun clearForm(group: ViewGroup) {
        var i = 0
        val count = group.childCount
        while (i < count) {
            val view = group.getChildAt(i)
            if (view is EditText) {
                view.setText("")
            }
            if (view is ViewGroup && view.childCount > 0) clearForm(view)
            ++i
        }
    }

    private fun setupDynamicTotal(editTextQuantity: EditText, editTextCost: EditText, textViewTotal: TextView) {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val quantity = editTextQuantity.text.toString().toDoubleOrNull() ?: 0.0
                val cost = editTextCost.text.toString().toDoubleOrNull() ?: 0.0
                val totalCost = quantity * cost
                textViewTotal.text = totalCost.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        }
        editTextQuantity.addTextChangedListener(textWatcher)
        editTextCost.addTextChangedListener(textWatcher)
    }

    /**
     * Displays the list of StockHistory records in a table.
     * Only one record can be selected at a time.
     * When a checkbox is checked, update the “reverse” fields with that record’s data.
     * When a checkbox is unchecked and no other is selected, clear the fields.
     */
    private var currentCheckedCheckBox: CheckBox? = null

    private fun displayStockHistory(container: TableLayout, stockHistoryList: List<StockHistory>) {
        container.removeAllViews()
        // Create a table layout
        val tableLayout = TableLayout(container.context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        }
        // Create a header row
        val headerRow = TableRow(container.context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val headers = listOf("Date", "ID", "Supplier", "Type", "Qty", "$", "Process", "Flow")
        headers.forEach { headerText ->
            val headerTextView = TextView(container.context).apply {
                text = headerText
                textSize = 20f
                setTextColor(container.context.getColor(android.R.color.black))
                setPadding(8, 8, 8, 8)
                gravity = Gravity.CENTER
            }
            headerRow.addView(headerTextView)
        }
        // Optionally add an extra header cell for the checkbox column.
        val emptyHeader = TextView(container.context).apply {
            text = ""
            setPadding(8, 8, 8, 8)
        }
        headerRow.addView(emptyHeader)
        tableLayout.addView(headerRow)

        // For each StockHistory record, create a row with its data and a checkbox.
        stockHistoryList.forEach { stockHistory ->
            val row = TableRow(container.context).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            }
            val stockHistoryType = when (stockHistory.type) {
                1 -> "IN"
                2 -> "OUT"
                else -> "N/A"
            }
            val values = listOf(
                stockHistory.historicDate,
                stockHistory.productId.toString(),
                stockHistory.supplier,
                stockHistoryType,
                stockHistory.quantity.toString(),
                stockHistory.cost.toString(),
                stockHistory.process,
                stockHistory.flow,
            )
            values.forEach { value ->
                val cellTextView = TextView(container.context).apply {
                    text = value
                    textSize = 14f
                    setTextColor(container.context.getColor(android.R.color.black))
                    setPadding(8, 8, 8, 8)
                    gravity = Gravity.CENTER
                }
                row.addView(cellTextView)
            }
            val checkBox = CheckBox(container.context).apply {
                // Use the tag to keep a reference to the corresponding StockHistory record.
                tag = stockHistory
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        for (i in 0 until tableLayout.childCount) {
                            val child = tableLayout.getChildAt(i)
                            if (child is TableRow) {
                                for (j in 0 until child.childCount) {
                                    val cell = child.getChildAt(j)
                                    if (cell is CheckBox && cell != buttonView && cell.isChecked) {
                                        cell.isChecked = false
                                    }
                                }
                            }
                        }
                        // Update the selected record and update the reverse fields.
                        selectedStockHistory = stockHistory
                        Log.d("InStockHistoryDialog", "Selected stock history: $stockHistory")
                        updateReverseFields(stockHistory)
                    } else {
                        // If no checkbox is selected, clear the reverse fields.
                        var anyChecked = false
                        for (i in 0 until tableLayout.childCount) {
                            val child = tableLayout.getChildAt(i)
                            if (child is TableRow) {
                                for (j in 0 until child.childCount) {
                                    val cell = child.getChildAt(j)
                                    if (cell is CheckBox && cell.isChecked) {
                                        anyChecked = true
                                        break
                                    }
                                }
                                if (anyChecked) break
                            }
                        }
                        if (!anyChecked) {
                            selectedStockHistory = null
                            clearReverseFields()
                        }
                    }
                }
            }
            row.addView(checkBox)
            tableLayout.addView(row)
        }
        container.addView(tableLayout)
    }

    /**
     * Updates the reverse fields with the data from the provided StockHistory record.
     */
    private fun updateReverseFields(stockHistory: StockHistory) {
        editTextProductId.setText(stockHistory.productId.toString())
        typeSpinner.setSelection(if (stockHistory.type  == 1) 1 else 0)
        editTextQuantity.setText(stockHistory.quantity.toString())
        editTextCost.setText(stockHistory.cost.toString())
        val total = stockHistory.quantity * stockHistory.cost
        textViewTotal.text = total.toString()
        processSpinner.setSelection(stockHistory.process.toInt()-1)
        flowSpinner.setSelection(stockHistory.flow.toInt()-1)
    }

    /**
     * Clears the reverse fields.
     */
    private fun clearReverseFields() {
        editTextProductId.text.clear()
        typeSpinner.setSelection(0)
        editTextQuantity.text.clear()
        editTextCost.text.clear()
        textViewTotal.text = ""
        processSpinner.setSelection(0)
        flowSpinner.setSelection(0)
        editTextDetail1.text.clear()
        editTextRemark.text.clear()
    }

    /**
     * Reverses the type of the selected stock history record.
     */
    private fun reverse(context: Context, stockHistory: StockHistory) {
        stockHistory.type = if (stockHistory.type == 1) 2 else 1
        CoroutineScope(Dispatchers.IO).launch {
            insertIntoStockHistory(context, stockHistory)
        }
    }

    private suspend fun insertIntoStockHistory(context: Context, stockHistory: StockHistory) {
        val query = """
    INSERT INTO StockHistory1 (SupplierOrderMainID, OrderNumber, HistoricDate, ProductID, Supplier, Type, Quantity, Process, Flow, Detail, Remark, SettlementStatus) 
    VALUES ('${stockHistory.supplierOrderNumber}', '${stockHistory.orderNumber}', #$dateTime#, ${stockHistory.productId}, '${stockHistory.supplier}', ${stockHistory.type}, ${stockHistory.quantity}, ${stockHistory.process}, ${stockHistory.flow}, '${stockHistory.detail}', '${stockHistory.remark}', 'Unverified')
    """
        try {
            withContext(Dispatchers.IO) {
                Query(query).execute(Constants.url)
                Toast.makeText(context, "Data saved successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("InStockHistoryDialog", "Error inserting record: $stockHistory", e)
            Toast.makeText(context, "Error saving data", Toast.LENGTH_SHORT).show()
        }
    }
}


