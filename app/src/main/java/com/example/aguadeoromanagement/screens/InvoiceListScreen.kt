package com.example.aguadeoromanagement.screens

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.components.Spinner
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.InvoiceItem
import com.example.aguadeoromanagement.models.Payment
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.networking.api.getStockForSimilarOrders
import com.example.aguadeoromanagement.ui.theme.MainGreen
import com.example.aguadeoromanagement.utils.toPrice
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Calendar

enum class Details { NONE, ITEMS, IN_OUT, PAYMENTS }


@Composable
fun InvoiceListScreen(
    invoices: List<Invoice>,
    invoiceItems: List<InvoiceItem> = listOf(),
    paymentList: List<Payment>,
    inOutList: List<StockHistory>,
    loading: Boolean,
    loadingItems: Boolean,
    navigateToDetails: (Invoice) -> Unit,
    fetchInvoiceItems: (Invoice) -> Unit,
    fetchPaymentList: (Invoice) -> Unit,
    fetchInOuts: (Invoice) -> Unit,
    invoicesToCheck: List<Invoice>,
    addRemoveInvoiceToCheck: (Invoice, Boolean) -> Unit,
    validateInvoices: (List<Invoice>) -> Unit,
    daysBeforeNow: Int,
    updateDate: (Int, LocalDate) -> Unit,
    selectedDate: LocalDate

) {

    var selectedInvoice: Invoice? by remember {
        mutableStateOf(null)
    }

    var details by remember {
        mutableStateOf(Details.NONE)
    }
    val context = LocalContext.current
//    val calendar = Calendar.getInstance()
//    val year = calendar[Calendar.YEAR]
//    val month = calendar[Calendar.MONTH]
//    val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
    val datePicker = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            {}
        }, selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth
    )
    datePicker.setMessage("Select Payment Date")

    datePicker.setOnDateSetListener { datePicker, i, i2, i3 ->

        val newDate = LocalDate.of(i, i2 + 1, i3)
        updateDate(daysBeforeNow, newDate)
//                    selectedDateText = payDate.format(Constants.forUsFormatter)

    }


    var daysList = (0..30).toList()


    fun onInvoiceClick(invoice: Invoice) {
        selectedInvoice = invoice
        if (details == Details.NONE) details = Details.ITEMS
        if (details == Details.ITEMS) fetchInvoiceItems(invoice)
        if (details == Details.PAYMENTS) fetchPaymentList(invoice)
        if (details == Details.IN_OUT) fetchInOuts(invoice)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Days before today (${
                        selectedDate.format(Constants.forUsFormatter)
                    })", Modifier.clickable { datePicker.show() })


//                var selectedDateText by remember { mutableStateOf("") }


                Spinner(
                    daysList.map { it.toString() },
                    daysList.indexOf(daysBeforeNow),
                    label = "",
                    onValueChange = { newVal -> updateDate(newVal, selectedDate) })
            }

            if (invoicesToCheck.isNotEmpty()) {

                Button(onClick = { validateInvoices(invoicesToCheck) }) {
                    Text("Validate invoices")
                }

            }
        }
        invoicesContainer(
            title = "Open invoices",
            invoices = invoices,
            onInvoiceClick = { invoice ->
                onInvoiceClick(invoice)
            },
            selectedInvoice = selectedInvoice,
            addRemoveInvoiceToCheck = addRemoveInvoiceToCheck,
            invoicesToCheck = invoicesToCheck,
            navigateToDetails = navigateToDetails,
            modifier = Modifier.weight(1f)
        )

        val defaultButton = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent)


        if (details != Details.NONE) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (details == Details.PAYMENTS) return@Button
                        fetchPaymentList(selectedInvoice!!)
                        details = Details.PAYMENTS
                    },
                    colors = if (details == Details.PAYMENTS) ButtonDefaults.buttonColors() else defaultButton,
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Text(
                        "Payment List",
                        color = if (details == Details.PAYMENTS) Color.White else MainGreen
                    )
                }
                Button(
                    onClick = {
                        if (details == Details.ITEMS) return@Button
                        details = Details.ITEMS
                        fetchInvoiceItems(selectedInvoice!!)
                    },
                    colors = if (details == Details.ITEMS) ButtonDefaults.buttonColors() else defaultButton,
                    elevation = ButtonDefaults.elevation(0.dp)

                ) {
                    Text(
                        "Invoice Items",
                        color = if (details == Details.ITEMS) Color.White else MainGreen
                    )
                }
                Button(
                    onClick = {
                        if (details == Details.IN_OUT) return@Button
                        details = Details.IN_OUT
                        fetchInOuts(selectedInvoice!!)
                    },
                    colors = if (details == Details.IN_OUT) ButtonDefaults.buttonColors() else defaultButton,
                    elevation = ButtonDefaults.elevation(0.dp)

                ) {
                    Text(
                        "In/Out",
                        color = if (details == Details.IN_OUT) Color.White else MainGreen
                    )
                }
            }
            if (details == Details.ITEMS) {
                itemsContainer(
                    invoiceItems = invoiceItems,
                    loadingItems = loadingItems,
                    modifier = Modifier.weight(1f)
                )
            }
            if (details == Details.PAYMENTS) {
                PaymentsContainer(
                    paymentList = paymentList,
                    loadingItems = loadingItems,
                    modifier = Modifier.weight(1f)
                )
            }
            if (details == Details.IN_OUT) {
                InOutContainerV2(
                    inOutList = inOutList,
                    loadingItems = loadingItems,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun invoicesContainer(
    title: String,
    invoices: List<Invoice>,
    onInvoiceClick: (Invoice) -> Unit,
    selectedInvoice: Invoice?,
    addRemoveInvoiceToCheck: (Invoice, Boolean) -> Unit,
    invoicesToCheck: List<Invoice>,
    navigateToDetails: (Invoice) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                style = TextStyle.Default.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
            )
        }

        val categories = listOf(
            "Received date", "Supplier", "Invoice number", "Amount", "Days to pay", "Checked"
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(categories.size), modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(categories) {
                Text(it)
            }
        }
        LazyColumn(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(invoices) { invoice ->
                var receivedDateString = ""
                var invoiceDateString = ""
                var receivedDate: LocalDate? = null;
                var invoiceDate: LocalDate? = null;
                var dueDate: LocalDate? = null;
                var dueDays = "N/A";
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                if (invoice.receivedDate.isNotEmpty()) {
                    receivedDate = LocalDate.parse(invoice.receivedDate, formatter)
                    receivedDateString =
                        receivedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
                }
                if (invoice.invoiceDate.isNotEmpty()) {
                    invoiceDate = LocalDate.parse(invoice.invoiceDate, formatter)
                    invoiceDateString =
                        invoiceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
                }

                if (invoice.dueOn.isNotEmpty()) {
                    dueDate = LocalDate.parse(invoice.dueOn, formatter)
                }

                if (dueDate != null) {
                    val now = LocalDate.now()
                    val period = Period.between(now, dueDate);
                    dueDays =
                        "${period.days} (${dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))})"
                }
                var modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onInvoiceClick(invoice) },
                        onLongClick = { navigateToDetails(invoice) })
                    .padding(vertical = 4.dp)

                if (invoice == selectedInvoice) {
                    modifier = modifier
                        .border(2.dp, MainGreen)
                        .padding(8.dp)
                }
                Row(
                    modifier.background(color = if (invoice.validated) Color.Green else Color.Transparent)
                ) {
                    Text(receivedDateString, modifier = Modifier.weight(1f))
                    Text(
                        ManagementApplication.getSuppliersInfo()
                            .getOrDefault(invoice.supplierId.toString(), "N/A"), Modifier.weight(1f)
                    )
                    Text(invoice.invoiceNumber, modifier = Modifier.weight(1f))
                    Text(invoice.amount.toPrice().toString(), modifier = Modifier.weight(1f))
                    Text(dueDays, modifier = Modifier.weight(1f))
                    if (invoice.validated) {
                        Text(
                            "OK",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Checkbox(
                            checked = invoice in invoicesToCheck,
                            onCheckedChange = {
                                addRemoveInvoiceToCheck(invoice, invoice !in invoicesToCheck)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MainGreen),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

            }
        }

    }
}


@Composable
fun itemsContainer(
    invoiceItems: List<InvoiceItem>, loadingItems: Boolean, modifier: Modifier
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        if (loadingItems) {
            CircularProgressIndicator()
            return@Box
        }
        if (invoiceItems.isEmpty()) {
            Text("No invoice items found !")
            return@Box
        }
        Column(modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Invoice items",
                    style = TextStyle.Default.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            }
            val itemFields =
                listOf(
                    "Sup order n°",
                    "Acc code",
                    "Quantity",
                    "Unit Price",
                    "Total Price",
                    "TVA",
                    "Detail"
                )
            LazyVerticalGrid(
                columns = GridCells.Fixed(itemFields.size),
                Modifier.padding(bottom = 16.dp)
            ) {
                items(itemFields) {
                    Text(it)
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(invoiceItems) { item ->
                    val itemInfo = mutableListOf<String>()
                    itemInfo.add(item.supplierOrderID)
                    itemInfo.add(item.accountCode)
                    itemInfo.add("${item.quantity} ${item.unit}")
                    itemInfo.add(item.unitPrice.toPrice().toString())
                    itemInfo.add((item.quantity * item.unitPrice).toPrice().toString())
                    itemInfo.add(item.vat.toString())
                    itemInfo.add(item.detail)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(itemInfo.size),
                        Modifier.heightIn(0.dp, 100.dp)
                    ) {
                        items(itemInfo) {
                            Text(it)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PaymentsContainer(paymentList: List<Payment>, loadingItems: Boolean, modifier: Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        if (loadingItems) {
            CircularProgressIndicator()
            return@Box
        }
        Column(modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Payment List",
                    style = TextStyle.Default.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            }
            val itemFields =
                listOf(
                    "Amount",
                    "Remain.",
                    "Sched",
                    "Method",
                    "Checked by",
                    "Executed",
                )
            LazyVerticalGrid(
                columns = GridCells.Fixed(itemFields.size),
                Modifier.padding(bottom = 16.dp)
            ) {
                items(itemFields) {
                    Text(it)
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(paymentList) { item ->
                    val paymentInfo = mutableListOf<String>()
                    paymentInfo.add(item.amountPaid.toPrice().toString())
                    paymentInfo.add(item.remainingOfInvoice.toPrice().toString())
                    paymentInfo.add(item.scheduledDate)
                    paymentInfo.add(item.paymentMode)
                    paymentInfo.add(item.checkedBy)
                    paymentInfo.add(item.executed.toString())
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(paymentInfo.size),
                        Modifier.heightIn(0.dp, 100.dp)
                    ) {
                        items(paymentInfo) {
                            Text(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockForFlow(
    modifier: Modifier = Modifier,
    flows: List<Int>,
    is_in: Boolean,
    stock: Map<Int, List<StockHistory>>
) {
    Column(Modifier.heightIn(50.dp, 5000.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        stock.keys.toList().forEach {
            val stocks = stock[it]!!
            val stockForFlow =
                stocks.filter { s -> s.flow.isNotEmpty() }
                    .filter { s -> s.flow.toInt() in flows && ((s.type == 1 && is_in) || (s.type != 1 && !is_in)) }
            val sum = stockForFlow.fold(0.0) { acc, stock -> acc + stock.quantity }
            val amount = stockForFlow.fold(0.0) { acc, stock -> acc + stock.cost * stock.quantity }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (sum == 0.0) "" else sum.toPrice().toString(), fontWeight = FontWeight.Bold)
                Text(if (amount == 0.0) "" else "(${amount.toPrice()} CHF)")
            }
//            Divider()

        }
    }
}


@Composable
fun InOutContainerV2(inOutList: List<StockHistory>, loadingItems: Boolean, modifier: Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        if (loadingItems) {
            CircularProgressIndicator()
            return@Box
        }
        if (inOutList.isEmpty()) {
            Text("No in/out found !")
            return@Box
        }


        val orderStock = inOutList.sortedBy { it.orderNumber }.groupBy { it.orderNumber }



        Column(Modifier.fillMaxSize()) {


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Stock history",
                    style = TextStyle.Default.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
            val scope = rememberCoroutineScope()

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                orderStock.keys.forEach { key ->
                    val groupedStock =
                        orderStock[key]!!.sortedBy { it.productId }.groupBy { it.productId }
                    item {
                        Column {


                            Text(
                                text = key,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        getStockForSimilarOrders(key)
                                    }
                                })
                            Column() {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {

                                    VerticalDivider(thickness = 2.dp, color = Color.Black)

                                    Column(
                                        Modifier.heightIn(50.dp, 5000.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {


                                        Text("ID")
                                        Text("")

                                        groupedStock.keys.toList().forEach {
                                            Text(it.toString())
                                        }
                                    }
                                    VerticalDivider(thickness = 2.dp, color = Color.Black)

                                    Column(
                                        Modifier
                                            .weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Stock/Return",
                                            Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(IntrinsicSize.Min),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Column {
                                                Text(
                                                    "In",
                                                    Modifier.weight(1f),
                                                    textAlign = TextAlign.Center
                                                )
                                                StockForFlow(
                                                    flows = listOf(1, 2),
                                                    is_in = true,
                                                    stock = groupedStock
                                                )
                                            }
                                            VerticalDivider(
                                                thickness = 2.dp,
                                                color = Color.Black
                                            )
                                            Column {
                                                Text(
                                                    "Out",
                                                    Modifier.weight(1f),
                                                    textAlign = TextAlign.Center
                                                )
                                                StockForFlow(
                                                    flows = listOf(1, 2),
                                                    is_in = false,
                                                    stock = groupedStock
                                                )
                                            }
                                        }
                                    }
                                    VerticalDivider(thickness = 2.dp, color = Color.Black)
                                    Column(
                                        Modifier

                                            .weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Process/Finish",
                                            Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(IntrinsicSize.Min),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Column {
                                                Text(
                                                    "In",
                                                    Modifier.weight(1f),
                                                    textAlign = TextAlign.Center
                                                )
                                                StockForFlow(
                                                    flows = listOf(3, 4),
                                                    is_in = true,
                                                    stock = groupedStock
                                                )

                                            }
                                            VerticalDivider(
                                                thickness = 2.dp,
                                                color = Color.Black
                                            )
                                            Column {
                                                Text(
                                                    "Out",
                                                    Modifier.weight(1f),
                                                    textAlign = TextAlign.Center
                                                )
                                                StockForFlow(
                                                    flows = listOf(3, 4),
                                                    is_in = false,
                                                    stock = groupedStock
                                                )
                                            }
                                        }
                                    }
                                    VerticalDivider(thickness = 2.dp, color = Color.Black)
                                    Column(
                                        Modifier

                                            .weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Variance",
                                            Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(IntrinsicSize.Min),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Column {
                                                Text(
                                                    "In",
                                                    Modifier.weight(1f),
                                                    textAlign = TextAlign.Center
                                                )
                                                StockForFlow(
                                                    flows = listOf(5),
                                                    is_in = true,
                                                    stock = groupedStock
                                                )
                                            }
                                            VerticalDivider(
                                                thickness = 2.dp,
                                                color = Color.Black
                                            )
                                            Column {
                                                Text(
                                                    "Out",
                                                    Modifier.weight(1f),
                                                    textAlign = TextAlign.Center
                                                )
                                                StockForFlow(
                                                    flows = listOf(5),
                                                    is_in = false,
                                                    stock = groupedStock
                                                )
                                            }
                                        }
                                    }
                                    VerticalDivider(thickness = 2.dp, color = Color.Black)
                                }
                            }
                        }
                    }


                }
            }
        }

    }
}


@Composable
fun InOutContainer(inOutList: List<StockHistory>, loadingItems: Boolean, modifier: Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        if (loadingItems) {
            CircularProgressIndicator()
            return@Box
        }
        if (inOutList.isEmpty()) {
            Text("No in/out found !")
        }
        Column(modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "In/Out",
                    style = TextStyle.Default.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            }
            val itemFields =
                listOf(
                    "Order N°",
                    "Date",
                    "Product ID",
                    "Quantity",
                    "Type",
                    "Flow",
                    "Process"
                )
            LazyVerticalGrid(
                columns = GridCells.Fixed(itemFields.size),
                Modifier.padding(bottom = 16.dp)
            ) {
                items(itemFields) {
                    Text(it)
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(inOutList) { item ->
                    val type = when (item.type) {
                        1 -> "IN"
                        2 -> "OUT"
                        else -> "N/A"
                    }
                    val inOutInfo = mutableListOf<String>()
                    inOutInfo.add(item.orderNumber)
                    inOutInfo.add(item.historicDate)
                    inOutInfo.add(item.productId.toString())
//                    paymentInfo.add(item.supplier)
                    inOutInfo.add(item.quantity.toString())
                    inOutInfo.add(type)
                    inOutInfo.add(item.flow)
                    inOutInfo.add(item.process)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(inOutInfo.size),
                        Modifier.heightIn(0.dp, 100.dp)
                    ) {
                        items(inOutInfo) {
                            Text(it)
                        }
                    }
                }
            }
        }
    }
}










