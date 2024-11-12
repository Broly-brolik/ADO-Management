package com.example.aguadeoromanagement.dialogs

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.models.SupplierOrderMain
import com.example.aguadeoromanagement.networking.Query
import com.example.aguadeoromanagement.networking.api.getSupplier
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

        val selectedItemsTextView: TextView = dialogView.findViewById(R.id.textViewSelectedItems)
        val editTextProductType: EditText = dialogView.findViewById(R.id.editTextProductType)
        val editTextType: EditText = dialogView.findViewById(R.id.editTextType)
        val editTextQuantity: EditText = dialogView.findViewById(R.id.editTextQuantity)
        val editTextCost: EditText = dialogView.findViewById(R.id.editTextCost)
        val editTextProcess: EditText = dialogView.findViewById(R.id.editTextProcess)
        val editTextFlow: EditText = dialogView.findViewById(R.id.editTextFlow)
        val editTextDetail1: EditText = dialogView.findViewById(R.id.editTextDetail1)
        val editTextRemark: EditText = dialogView.findViewById(R.id.editTextRemark)
        val buttonSave: Button = dialogView.findViewById(R.id.buttonSave)

        selectedItemsTextView.text = selectedItems.joinToString(prefix = "Selected Items:\n", separator = "\n") { it.supplierOrderNumber }

        selectedItemsTextView.setOnClickListener {
            toggleFieldVisibility(listOf(editTextProductType, editTextType, editTextQuantity, editTextCost, editTextProcess, editTextFlow, editTextDetail1, editTextRemark))
        }

        buttonSave.setOnClickListener {
            if (editTextProductType.visibility == View.VISIBLE) {
                val productID = editTextProductType.text.toString().toIntOrNull() ?: 0
                val quantity = editTextQuantity.text.toString().toIntOrNull() ?: 0
                val cost = editTextCost.text.toString().toDoubleOrNull() ?: 0.0
                val process = editTextProcess.text.toString()
                val flow = editTextFlow.text.toString().toIntOrNull() ?: 0
                val detail = editTextDetail1.text.toString()
                val remark = editTextRemark.text.toString()

                if (validateInputs(quantity, cost)) {
                    selectedItems.forEach { order ->
                        CoroutineScope(Dispatchers.IO).launch {
                            //val (contact, success) = getSupplier(order.supplierID.toString(), isContact = true)
                            //val supplierName = contact.name
                            insertStockHistory(
                                context,
                                order.id,
                                order.supplierOrderNumber,
                                productID,
                                //supplierName,
                                quantity,
                                cost,
                                weight = 0.0,
                                loss = 0,
                                process,
                                flow,
                                detail,
                                remark
                            )
                        }
                    }
                } else {
                    Log.e("InStockHistoryDialog", "Invalid input data.")
                }
            }
        }

        AlertDialog.Builder(context)
            .setTitle("'In' items for stock history")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun toggleFieldVisibility(fields: List<View>) {
        val visibility = if (fields.first().visibility == View.VISIBLE) View.GONE else View.VISIBLE
        fields.forEach { it.visibility = visibility }
    }

    private fun validateInputs(quantity: Int, cost: Double): Boolean {
        return quantity >= 0 && cost >= 0.0
    }

    private suspend fun insertStockHistory(
        context: Context,
        supplierOrderMainID: Int,
        supplierOrderNumber: String,
        productID: Int,
        //SupplierName: String,
        quantity: Int,
        cost: Double,
        weight: Double,
        loss: Int,
        process: String,
        flow: Int,
        detail: String,
        remark: String
    ) {
        val dateTime = DateTimeFormatter
            .ofPattern("dd/MM/yyyy h:mm:ss a")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
        val query = """
            INSERT INTO StockHistory1 (
                SupplierOrderMainID, OrderNumber, HistoricDate, ProductID, Quantity, Cost, Weight, Loss, Process, Flow, Detail, Remark
            ) VALUES (
                $supplierOrderMainID, '$supplierOrderNumber', #$dateTime#, $productID, $quantity, $cost, $weight, $loss, '$process', $flow, '$detail', '$remark'
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
}
