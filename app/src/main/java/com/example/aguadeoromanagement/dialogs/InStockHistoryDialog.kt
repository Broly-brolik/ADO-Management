package com.example.aguadeoromanagement.dialogs

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.models.SupplierOrderMain
import com.example.aguadeoromanagement.networking.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class InStockHistoryDialog {

    fun showDialog(context: Context, selectedItems: List<SupplierOrderMain>) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_add_item_stockhistory, null)

        val container: LinearLayout = dialogView.findViewById(R.id.containerSelectedItems)
        //val selectedItemsTextView: TextView = dialogView.findViewById(R.id.textViewSelectedItems)
        val editTextProductId: EditText = dialogView.findViewById(R.id.editTextProductId)
        val editTextDetail1: EditText = dialogView.findViewById(R.id.editTextDetail1)
        val editTextType: EditText = dialogView.findViewById(R.id.editTextType)
        val editTextQuantity: EditText = dialogView.findViewById(R.id.editTextQuantity)
        val editTextCost: EditText = dialogView.findViewById(R.id.editTextCost)
        val editTextRemark: EditText = dialogView.findViewById(R.id.editTextRemark)
        val editTextProcess: EditText = dialogView.findViewById(R.id.editTextProcess)
        val editTextFlow: EditText = dialogView.findViewById(R.id.editTextFlow)
        val buttonSave: Button = dialogView.findViewById(R.id.buttonSave)
        var selectedOrder: String? = null

        Log.e("zzz", "too muuch: $selectedItems")

        selectedItems.forEach { order ->
            val textView = TextView(context).apply {
                text = order.supplierOrderNumber
                textSize = 16f
                setPadding(8, 8, 8, 8)
                setTextColor(context.getColor(android.R.color.black))
                setOnClickListener {
                    toggleFieldVisibility(listOf(editTextProductId, editTextDetail1, editTextType, editTextQuantity, editTextCost, editTextRemark, editTextProcess, editTextFlow))
                    selectedOrder = order.supplierOrderNumber
                    Log.e("InStockHistoryDialog", "Clicked on: $selectedOrder")
                }
            }
            container.addView(textView)
        }

        buttonSave.setOnClickListener {
            if (editTextProductId.visibility == View.VISIBLE) {
                val productID = editTextProductId.text.toString().toIntOrNull() ?: 0
                val detail = editTextDetail1.text.toString()
                val type = editTextType.text.toString().toIntOrNull() ?: 0
                val quantity = editTextQuantity.text.toString().toDoubleOrNull() ?: 0.0
                val cost = editTextCost.text.toString().toDoubleOrNull() ?: 0.0
                val remark = editTextRemark.text.toString()
                val process = editTextProcess.text.toString().toIntOrNull() ?: 0
                val flow = editTextFlow.text.toString().toIntOrNull() ?: 0

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
            toggleFieldVisibility(listOf(editTextProductId, editTextDetail1, editTextType, editTextQuantity, editTextCost, editTextRemark, editTextProcess, editTextFlow))
        }

        AlertDialog.Builder(context)
            .setTitle("'Ins' items for stock history")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
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
                val result = q.execute(Constants.url)
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

}
