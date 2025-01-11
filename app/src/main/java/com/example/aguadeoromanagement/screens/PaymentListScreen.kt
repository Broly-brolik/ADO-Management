package com.example.aguadeoromanagement.screens

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.components.Spinner
import com.example.aguadeoromanagement.models.Contact
import com.example.aguadeoromanagement.models.OptionValue
import com.example.aguadeoromanagement.models.Payment
import com.example.aguadeoromanagement.networking.api.closeInvoices
import com.example.aguadeoromanagement.networking.api.updateSinglePayment
import com.example.aguadeoromanagement.ui.theme.GrayBorder
import com.example.aguadeoromanagement.ui.theme.MainGreen
import com.example.aguadeoromanagement.ui.theme.Shapes
import com.example.aguadeoromanagement.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

@Composable
fun PaymentsListScreen(
    payments: List<Payment>,
    updatePayment: suspend (Int, Boolean) -> Unit,
    optionValues: List<OptionValue>,
    suppliers: List<Contact>,
    showPopup: Boolean,
    dismissPopup: () -> Unit,
    navigateToInvoice: (String, String) -> Unit,
    updateInvoiceItems: (Int, Double, Double)-> Unit,
    updateDate: (Int, LocalDate) -> Unit,
    selectedDate: LocalDate,
) {

    val toExecute = remember { mutableStateListOf<Int>() }
    val scope = rememberCoroutineScope()

    var daysBeforeNow by remember {
        mutableStateOf(6)
    }

    var updating by remember {
        mutableStateOf(false)
    }

    var openingInvoice by remember {
        mutableStateOf(false)
    }


    var includeFuture by remember {
        mutableStateOf(false)
    }

    var onlyExecuted by remember {
        mutableStateOf(false)
    }

    var specificSupplierIndex by remember {
        mutableStateOf(0)
    }

    var suppliersWithAll = mutableListOf<String>()
    suppliersWithAll.add("All")
    suppliersWithAll.addAll(suppliers.map { it.name })


    val columnFields =
        listOf(
            "Name",
            "Inv n°",
            "Amount",
            "Remain.",
            "Sched.",
            "Payment",
            "Checked by",
            "Executed ?"
        )
    val context = LocalContext.current
    val datePicker = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
        }, selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth
    )
    datePicker.setMessage("Select Payment Date")
    datePicker.setOnDateSetListener { datePicker, i, i2, i3 ->
        val newDate = LocalDate.of(i, i2 + 1, i3)
        updateDate(daysBeforeNow, newDate)
    }

    fun update() {
        scope.launch {
            updating = true
            updatePayment(daysBeforeNow, includeFuture)
            updating = false
        }
    }

    fun showPaymentFromCondition(payment: Payment): Boolean {
//        Log.e("payment", payment.contactName)
        if (onlyExecuted && !payment.executed) {
//            Log.e("payment non executé", "")
            return false
        }
        if (specificSupplierIndex > 0) {
            val name = suppliersWithAll[specificSupplierIndex]
            return name == payment.contactName
        }
        return true
    }


    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = if (daysBeforeNow > 0) daysBeforeNow.toString() else "",
                onValueChange = { newVal ->
                    daysBeforeNow = try {
                        newVal.toInt()
                    } catch (e: Exception) {
                        0
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(75.dp)
            )
            //Text("days until ${LocalDate.now().format(Constants.forUsFormatter)}",
            Text("days until ${selectedDate.format(Constants.forUsFormatter)}",
                Modifier.clickable { datePicker.show() })
            Button(onClick = {
                update()
            }) {
                Text("update")
            }
            Text("future ?")
            Checkbox(checked = includeFuture,
                colors = CheckboxDefaults.colors(checkedColor = MainGreen),
                onCheckedChange = {
                    includeFuture = !includeFuture
                    update()
                })
            Text("executed ?")
            Checkbox(checked = onlyExecuted,
                colors = CheckboxDefaults.colors(checkedColor = MainGreen),
                onCheckedChange = {
                    onlyExecuted = it
                })

            Spinner(
                entries = suppliersWithAll,
                index = specificSupplierIndex,
                label = "Choose supplier"
            ) { newIndex ->
                specificSupplierIndex = newIndex
            }
        }
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            if (updating || payments.isEmpty()) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2 * columnFields.size - 1),
                        Modifier.heightIn(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(columnFields, span = { index, item ->
                            if (index == columnFields.lastIndex - 1) {
                                GridItemSpan(1)
                            } else {
                                GridItemSpan(2)
                            }
                        }) { index, item ->
                            Text(item, fontSize = 12.sp)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            payments,
                            key = { item: Payment -> item.id }) { payment: Payment ->
                            var color = if (payment.executed) Color.Green else GrayBorder
                            val borderWidth = if (payment.id in toExecute) 4.dp else 2.dp
                            if (payment.id in toExecute) {
                                color = MainGreen
                            }

                            if (showPaymentFromCondition(payment)) {
                                val paymentInfo = mutableListOf<String>()
                                paymentInfo.add(payment.contactName)
                                paymentInfo.add(payment.invoiceNumber)
                                paymentInfo.add("${payment.amountPaid} ${payment.currency}/${payment.totalOfInvoice} ${payment.currency}")
                                paymentInfo.add("${payment.remainingOfInvoice} ${payment.currency}")
                                paymentInfo.add(payment.scheduledDate)
                                paymentInfo.add(payment.paymentMode)
                                paymentInfo.add(payment.checkedBy)

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2 * columnFields.size - 1),
                                    modifier = Modifier
                                        .heightIn(10.dp, 100.dp)
                                        .padding(vertical = 8.dp),
                                    userScrollEnabled = false,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.Bottom

                                ) {
                                    itemsIndexed(paymentInfo, span = { index, item ->
                                        if (index == paymentInfo.lastIndex) {
                                            GridItemSpan(1)
                                        } else {
                                            GridItemSpan(2)
                                        }
                                    }) { index, item ->
                                        var mod: Modifier = Modifier
                                        if (index == 1) {
                                            mod = Modifier.clickable {
                                                openingInvoice = true

                                                navigateToInvoice(
                                                    payment.invoiceID.toString(),
                                                    payment.contactName
                                                )
                                            }
                                        }
                                        Text(item, fontSize = 12.sp, modifier = mod)
                                    }
                                    item(span = {
                                        GridItemSpan(2)
                                    }
                                    ) {
                                        if (!payment.executed) {
                                            Checkbox(
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = MainGreen
                                                ),
                                                checked = payment.id in toExecute,
                                                onCheckedChange = {
                                                    if (payment.executed) {
                                                        return@Checkbox
                                                    }
                                                    if (payment.id !in toExecute) {
                                                        toExecute.add(payment.id)
                                                    } else {
                                                        toExecute.remove(payment.id)
                                                    }
                                                }

                                            )
                                        } else {
                                            Text(
                                                "Executed on ${payment.executedDate}",
                                                color = color,
                                                fontSize = 12.sp,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                                Divider(thickness = 2.dp, color = MainGreen)
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 8.dp)
//                                        .border(
//                                            borderWidth,
//                                            color = color,
//                                            shape = Shapes.medium
//                                        )
//                                        .padding(12.dp)
//                                        .clickable {
//                                            openingInvoice = true
//
//                                            navigateToInvoice(
//                                                payment.invoiceId.toString(),
//                                                payment.contactName
//                                            )
////                                    if (payment.executed) {
////                                        return@clickable
////                                    }
////                                    if (payment.id !in toExecute) {
////                                        toExecute.add(payment.id)
////                                    } else {
////                                        toExecute.remove(payment.id)
////                                    }
//                                        },
//                                    horizontalArrangement = Arrangement.SpaceBetween
//                                ) {
//                                    Column(
//                                        horizontalAlignment = Alignment.Start,
//                                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                                    ) {
//                                        Text(payment.id.toString())
//                                        Text("Contact: ${payment.contactName}")
//                                        Text("Invoice number: ${payment.invoiceNumber}")
//                                        Text("Amount: ${payment.amountPaid} ${payment.currency} of ${payment.totalOfInvoice} ${payment.currency}")
//                                        Text("Remaining: ${payment.remainingOfInvoice} ${payment.currency}")
//                                        if (payment.remarks.isNotEmpty()) {
//                                            Text("Remark: ${payment.remarks}")
//                                        }
//                                    }
//                                    Column(
//                                        horizontalAlignment = Alignment.End,
//
//                                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                                    ) {
//                                        Text("scheduled for ${payment.scheduledDate}")
//                                        if (payment.paymentMode.isNotEmpty()) {
//                                            Text("payment mode: ${payment.paymentMode}")
//                                        }
//                                        if (payment.checkedBy.isNotEmpty()) {
//                                            Text("checked by ${payment.checkedBy}")
//                                        }
//                                        if (!payment.executed) {
//                                            Row(
//                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                                verticalAlignment = Alignment.CenterVertically,
//                                            ) {
//                                                Text("Execute")
//                                                Checkbox(
//                                                    colors = CheckboxDefaults.colors(
//                                                        checkedColor = MainGreen
//                                                    ),
//                                                    checked = payment.id in toExecute,
//                                                    onCheckedChange = {
//                                                        if (payment.executed) {
//                                                            return@Checkbox
//                                                        }
//                                                        if (payment.id !in toExecute) {
//                                                            toExecute.add(payment.id)
//                                                        } else {
//                                                            toExecute.remove(payment.id)
//                                                        }
//                                                    }
//
//                                                )
//                                            }
//                                        } else {
//                                            Text(
//                                                "payment executed on ${payment.executedDate}",
//                                                color = color
//                                            )
//                                        }
//                                    }
//                                }
                            }


                        }
                    }
                }
            }
        }
    }
    if (showPopup) {
        Popup() {

            var closeInvoice by remember {
                mutableStateOf(false)
            }

            var invoiceToClose by remember {
                mutableStateOf(mutableListOf<Payment>())
            }

            var updatingDatabase by remember {
                mutableStateOf(false)
            }

            fun onUpdateSuccess(date: String, checkedBy: String, paymentMode: String) {
                scope.launch {
                    updatingDatabase = true
                    toExecute.forEach { id ->
                        val payment = payments.find { payment: Payment -> payment.id == id }!!
                        Log.e("payment", payment.toString())

                        val success = updateSinglePayment(
                            payment,
                            LocalDate.parse(date, Constants.forUsFormatter)
                                .format(Constants.accessFormater),
                            checkedBy = checkedBy,
                            paymentMode = paymentMode
                        )
                        Log.e("success", success.toString())
                        if (success) {
//                            toExecute.remove(id)
                            val newRemaining = payment.remainingOfInvoice - payment.amountPaid
                            val sameInvoicePayments =
                                payments.filter { p: Payment -> p.invoiceID == payment.invoiceID }
                            sameInvoicePayments.forEach { p: Payment ->
                                p.remainingOfInvoice = newRemaining
                            }
//                            updatePayment(daysBeforeNow)
                        }
                    }
                    val distinctPayments =
                        payments.filter { payment: Payment -> payment.id in toExecute }
                            .distinctBy { payment: Payment -> payment.invoiceNumber }
                    Log.e("distinct", distinctPayments.toString())
                    distinctPayments.forEach { payment ->
                        Log.e("payment", payment.invoiceNumber)
                        if (payment.remainingOfInvoice <= 0) {
                            Log.e("close", payment.invoiceNumber)
                            invoiceToClose.add(payment)
                        }
                        updateInvoiceItems(payment.invoiceID, payment.totalOfInvoice, payment.amountPaid)

                    }
                    updatingDatabase = false

                    if (invoiceToClose.isNotEmpty()) {
                        closeInvoice = true
                    } else {
                        //pour rafraichir le ui
                        updatePayment(daysBeforeNow, includeFuture)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .background(color = Color.Black.copy(alpha = 0.8f), shape = Shapes.medium)
                    .fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                // TODO modifier checked by et payment date
                var checkedBy by remember {
                    mutableStateOf("")
                }
                var selectedDateText by remember {
                    mutableStateOf("Select Date")
                }



                if (!closeInvoice) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.5F)
                            .background(Color.White)
                            .padding(64.dp), verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        if (updatingDatabase) {
                            CircularProgressIndicator()
                            return@Column
                        }

                        val context = LocalContext.current
                        val calendar = Calendar.getInstance()

//                var selectedDateText by remember { mutableStateOf("") }

                        val year = calendar[Calendar.YEAR]
                        val month = calendar[Calendar.MONTH]
                        val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
                        val datePicker = DatePickerDialog(
                            context,
                            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                                selectedDateText =
                                    "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                            }, year, month, dayOfMonth
                        )
                        datePicker.setMessage("Select Payment Date")
                        datePicker.setOnDateSetListener { datePicker, i, i2, i3 ->
                            val payDate = LocalDate.of(i, i2 + 1, i3)
                            selectedDateText = payDate.format(Constants.forUsFormatter)
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Payment Date:")
                            Text(selectedDateText,
                                modifier = Modifier
                                    .background(color = White, shape = Shapes.medium)
                                    .border(2.dp, color = MainGreen, shape = Shapes.medium)
                                    .padding(16.dp)
                                    .clickable { datePicker.show() })
                        }

                        //approval part
                        val approvals =
                            optionValues.filter { optionValue -> optionValue.type == "Approval" }
                                .map { optionValue -> optionValue.optionValue }
                        var approvalIndex by remember {
                            mutableStateOf(-1)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Checked by:")
                            Spinner(
                                entries = approvals,
                                index = approvalIndex,
                                label = "Select",
                                modifier = Modifier.heightIn(0.dp, 1000.dp)
                            ) {
                                approvalIndex = it
                            }
                        }

                        //payment mode part
                        val paymentModes =
                            optionValues.filter { optionValue -> optionValue.type == "SupplierPaymentMethod" }
                                .map { optionValue -> optionValue.optionValue }
                        var paymentModeIndex by remember {
                            mutableStateOf(-1)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Payment mode:")
                            Spinner(
                                entries = paymentModes,
                                index = paymentModeIndex,
                                label = "Select",
                                modifier = Modifier.heightIn(0.dp, 1000.dp)
                            ) {
                                paymentModeIndex = it
                            }
                        }

                        //Buttons part
                        Buttons(
                            onValidate = {
                                if (selectedDateText != "Select Date" && approvalIndex > -1 && paymentModeIndex > -1) {
                                    onUpdateSuccess(
                                        date = selectedDateText,
                                        checkedBy = approvals[approvalIndex],
                                        paymentMode = paymentModes[paymentModeIndex]
                                    )
                                } else {
                                    Toast.makeText(
                                        ManagementApplication.getAppContext(),
                                        "Enter all information",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onCancel = { dismissPopup() })

//                datePicker.show()

                    }
                }
                //update invoice part
                else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier
                            .fillMaxWidth(0.5F)
                            .background(Color.White)
                            .padding(64.dp)
                    ) {
                        var toCloseId by remember {
                            mutableStateOf(listOf<Int>())
                        }
                        Text(
                            "There is no more to pay for the following invoices. Select which one would you like to close.",
                            modifier = Modifier.height(IntrinsicSize.Min)
                        )
                        LazyColumn {
                            items(invoiceToClose) { invoice ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val invoiceId = invoice.invoiceID
                                    Text(invoice.invoiceNumber)
                                    Checkbox(
                                        colors = CheckboxDefaults.colors(checkedColor = MainGreen),
                                        checked = invoiceId in toCloseId,
                                        onCheckedChange = { checked ->
                                            if (checked) toCloseId = toCloseId + invoiceId
                                            else toCloseId = toCloseId.toMutableList()
                                                .also { it.remove(invoiceId) }
                                        })
                                }
                            }
                        }
                        Buttons(onValidate = {
                            updatingDatabase = true
                            scope.launch {
                                val success = closeInvoices(toCloseId)

                                Toast.makeText(
                                    ManagementApplication.getAppContext(),
                                    if (success) "Update successful" else "Update failed",
                                    Toast.LENGTH_LONG
                                ).show()

                                toExecute.clear()
                                updatePayment(daysBeforeNow, includeFuture)
                                delay(1000)

                                dismissPopup()
                            }
                        }, onCancel = {
                            toExecute.clear()
                            scope.launch {
                                updatePayment(daysBeforeNow, includeFuture)
                            }
                            dismissPopup()
                        })

                    }
                }

            }
        }
    }
    if (openingInvoice) {
        Popup() {
            Box(
                modifier = Modifier
                    .background(color = Color.Black.copy(alpha = 0.8f), shape = Shapes.medium)
                    .fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(White)
                        .size(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

    }
}


@Composable
fun Buttons(onValidate: () -> Unit = {}, onCancel: () -> Unit = {}) {
    //Buttons part
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { onCancel() },
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }
        Button(
            onClick = {
                onValidate()
            },
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("Validate")
        }
    }
}

