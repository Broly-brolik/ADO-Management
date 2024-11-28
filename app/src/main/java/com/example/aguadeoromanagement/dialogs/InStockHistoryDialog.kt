package com.example.aguadeoromanagement.dialogs

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.models.*
import com.example.aguadeoromanagement.networking.Query
import com.example.aguadeoromanagement.networking.api.searchProductById
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class InStockHistoryDialog (context: Activity? = null) {
    private val url = "http://195.15.223.234/aguadeoro/connect.php"

    fun showDialog(context: Context, selectedItems: List<SupplierOrderMain>) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_add_item_stockhistory, null)
        val container: LinearLayout = dialogView.findViewById(R.id.containerSelectedItems)
        val selectedItemsTextView: TextView = dialogView.findViewById(R.id.textViewSelectedItems)
        var editTextProductId: EditText = dialogView.findViewById(R.id.editTextProductId)
        var textViewCategory: TextView = dialogView.findViewById(R.id.TextViewCategory)
        var textViewSubcategory: TextView = dialogView.findViewById(R.id.TextViewSubcategory)
        var textViewType: TextView = dialogView.findViewById(R.id.TextViewType)
        var textViewProductCode: TextView = dialogView.findViewById(R.id.TextViewProductCode)
        val imageViewSearch: ImageView = dialogView.findViewById(R.id.search)
        val editTextDetail1: EditText = dialogView.findViewById(R.id.editTextDetail1)
        val typeSpinner: Spinner = dialogView.findViewById(R.id.spinnerTextType)
        val adapterType = ArrayAdapter.createFromResource(
            context,
            R.array.type_array,
            android.R.layout.simple_spinner_item
        )
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapterType
        val editTextQuantity: EditText = dialogView.findViewById(R.id.editTextQuantity)
        val editTextCost: EditText = dialogView.findViewById(R.id.editTextCost)
        val textViewTotal: TextView = dialogView.findViewById(R.id.TextViewTotalCost)
        val editTextRemark: EditText = dialogView.findViewById(R.id.editTextRemark)
        val processSpinner: Spinner = dialogView.findViewById(R.id.spinnerTextProcess)
        val adapterProcess = ArrayAdapter.createFromResource(
            context,
            R.array.process_array,
            android.R.layout.simple_spinner_item
        )
        adapterProcess.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        processSpinner.adapter = adapterProcess
        val flowSpinner: Spinner = dialogView.findViewById(R.id.spinnerFlow)
        val adapterFlow = ArrayAdapter.createFromResource(
            context,
            R.array.flow_array,
            android.R.layout.simple_spinner_item
        )
        adapterFlow.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        flowSpinner.adapter = adapterFlow
        val textViewResume: TextView = dialogView.findViewById(R.id.textViewResume)
        val buttonVerify: Button = dialogView.findViewById(R.id.buttonVerify)
        val buttonSave: Button = dialogView.findViewById(R.id.buttonSave)
        var selectedOrder: String? = null

        setupDynamicTotal(editTextQuantity, editTextCost, textViewTotal)

        selectedItems.forEach { order ->
            val textView = TextView(context).apply {
                text = order.supplierOrderNumber
                textSize = 16f
                setPadding(8, 8, 8, 8)
                setTextColor(context.getColor(android.R.color.black))
                setOnClickListener {
                    toggleFieldVisibility(listOf(editTextProductId, imageViewSearch, textViewCategory, textViewSubcategory,
                        textViewType, textViewProductCode, typeSpinner, editTextQuantity, editTextCost, textViewTotal, editTextDetail1, editTextRemark, processSpinner, flowSpinner))
                    selectedOrder = order.supplierOrderNumber
                    selectedItemsTextView.text = selectedOrder
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
                    textViewCategory.text = product2?.category
                    textViewSubcategory.text = product2?.subCategory
                    textViewType.text = product2?.type
                    textViewProductCode.text = product2?.productCode
                }
            } else {
                Toast.makeText(context, "Invalid Product ID", Toast.LENGTH_SHORT).show()
            }
        }

        buttonVerify.setOnClickListener{
            val details = StringBuilder()
            if (textViewProductCode.visibility == View.VISIBLE) {
                val productId = editTextProductId.text.toString().trim()
                details.append("Product ID: $productId; ")
                val category = textViewCategory.text.toString()
                details.append("Category: $category; ")
                val subCategory = textViewSubcategory.text.toString()
                details.append("Subcategory: $subCategory; ")
                val type = textViewType.text.toString()
                details.append("Type: $type; ")
                val productCode = textViewProductCode.text.toString()
                details.append("Product Code: $productCode; ")
                val typeSpinner = typeSpinner.selectedItem.toString()
                details.append("Type Spinner: $typeSpinner; ")
                val process = processSpinner.selectedItem.toString()
                details.append("Process: $process; ")
                val flow = flowSpinner.selectedItem.toString()
                details.append("Flow: $flow; ")
                val quantity = editTextQuantity.text.toString().trim()
                details.append("Quantity: $quantity; ")
                val cost = editTextCost.text.toString().trim()
                details.append("Cost: $cost; ")
                val totalCost = textViewTotal.text.toString().trim()
                details.append("Total Cost: $totalCost; ")
                val detail = editTextDetail1.text.toString().trim()
                details.append("Detail: $detail; ")
            }
            textViewResume.text = details.toString()
            textViewResume.visibility = View.VISIBLE
        }

        buttonSave.setOnClickListener {
            if (editTextProductId.visibility == View.VISIBLE) {
                val productID = editTextProductId.text.toString().toIntOrNull() ?: 0
                val detail = editTextDetail1.text.toString()
                val type = typeSpinner.selectedItemPosition+1
                val quantity = editTextQuantity.text.toString().toDoubleOrNull() ?: 0.0
                val cost = editTextCost.text.toString().toDoubleOrNull() ?: 0.0
                val totalCost = quantity * cost
                textViewTotal.text = totalCost.toString()
                val remark = editTextRemark.text.toString()
                val process = processSpinner.selectedItemPosition+1
                val flow = flowSpinner.selectedItemPosition+1

                if (validateInputs(quantity, cost)) {
                    selectedItems.find { it.supplierOrderNumber == selectedOrder }?.let { order ->
                        CoroutineScope(Dispatchers.IO).launch {
                            insertStockHistory(
                                context,
                                order.id,
                                order.supplierOrderNumber,
                                productID,
                                order.recipient,
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
                } else {
                    Log.e("InStockHistoryDialog", "Invalid input data.")
                }
            }
            clearForm(dialogView as ViewGroup)
            toggleFieldVisibility(listOf(editTextProductId, editTextDetail1, typeSpinner, editTextQuantity, editTextCost, editTextRemark, processSpinner, flowSpinner))
        }

        AlertDialog.Builder(context)
            .setTitle("Stock Historic IN/OUT")
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
        val dateTime = DateTimeFormatter
            .ofPattern("dd/MM/yyyy h:mm:ss a")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())

        val query = """
    INSERT INTO StockHistory1 (
        SupplierOrderMainID, OrderNumber, HistoricDate, ProductID, Supplier, Detail, Type, Quantity, Cost, Remark, Weight, Loss, Process, Flow, SettlementStatus
    ) VALUES (
        $supplierOrderMainID, '$supplierOrderNumber', #$dateTime#, $productID, '$recipient', '$detail', $type, $quantity, $cost, '$remark', $weight, $loss, '$process', $flow, 'Unverified'
    )
""".trimIndent()
        try {
            val q = Query(query)
            withContext(Dispatchers.IO) {
                q.execute(url)
                Log.e("InStockHistoryDialog", "Record successfully inserted into StockHistory.")
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
            if (view is ViewGroup && (view.childCount > 0)) clearForm(view)
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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used
            }
        }
        editTextQuantity.addTextChangedListener(textWatcher)
        editTextCost.addTextChangedListener(textWatcher)
    }


}
