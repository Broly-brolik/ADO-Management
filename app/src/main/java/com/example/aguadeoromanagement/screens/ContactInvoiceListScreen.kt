package com.example.aguadeoromanagement.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aguadeoromanagement.models.Invoice
import com.example.aguadeoromanagement.models.InvoiceItem
import com.example.aguadeoromanagement.ui.theme.MainGreen
import com.example.aguadeoromanagement.utils.toPrice
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Composable
fun ContactInvoiceListScreen(
    openInvoices: List<Invoice>,
    closedInvoices: List<Invoice>,
    invoiceItems: List<InvoiceItem> = listOf(),
    loading: Boolean,
    loadingItems: Boolean,
    navigateToDetails: (Invoice) -> Unit,
    fetchInvoiceItems: (Invoice) -> Unit
) {

    var showOpen by remember {
        mutableStateOf(true)
    }

    var selectedInvoice: Invoice? by remember {
        mutableStateOf(null)
    }

    fun onInvoiceClick(invoice: Invoice) {
        //selectedInvoice = invoice
        //fetchInvoiceItems(invoice)
        navigateToDetails(invoice)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
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
                .padding(bottom = 16.dp)
        ) {
            val modifier = Modifier
                .weight(1f)

            val selectedMod = Modifier.border(
                2.dp,
                MainGreen
            )

            Text(
                "Open invoices", modifier = modifier
                    .then(
                        if (showOpen) selectedMod else Modifier
                    )
                    .clickable { showOpen = true }
                    .padding(16.dp)

            )
            Text(
                "Closed invoices", modifier = modifier
                    .then(
                        if (!showOpen) selectedMod else Modifier
                    )
                    .clickable { showOpen = false }
                    .padding(16.dp)

            )

        }
        if (showOpen) {
            invoicesContainer(
                title = "Open invoices",
                invoices = openInvoices,
                onInvoiceClick = { invoice -> onInvoiceClick(invoice) },
                selectedInvoice = selectedInvoice,
                invoicesToCheck = listOf(),
                addRemoveInvoiceToCheck ={invoice, b ->  },

                modifier = Modifier.weight(1f)
            )
        } else {
            invoicesContainer(
                title = "Closed invoices",
                invoices = closedInvoices,
                onInvoiceClick = { invoice -> onInvoiceClick(invoice) },
                selectedInvoice = selectedInvoice,
                addRemoveInvoiceToCheck ={invoice, b ->  },
                invoicesToCheck = listOf(),
                modifier = Modifier.weight(1f)
            )
        }
        if (invoiceItems.isNotEmpty()) {
            itemsContainer(
                invoiceItems = invoiceItems,
                loadingItems = loadingItems,
                modifier = Modifier.weight(1f)
            )
        }


    }
}


@Composable
fun contactInvoicesContainer(
    title: String,
    invoices: List<Invoice>,
    onInvoiceClick: (Invoice) -> Unit,
    selectedInvoice: Invoice?,
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
            "Invoice number",
            "Received date",
            "Invoice date",
            "Remain",
            "Days to pay"
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
                    .clickable { onInvoiceClick(invoice) }
                    .padding(vertical = 4.dp)

                if (invoice == selectedInvoice) {
                    modifier = modifier
                        .border(2.dp, MainGreen)
                        .padding(8.dp)
                }
                Row(
                    modifier
                ) {
                    Text(invoice.invoiceNumber, modifier = Modifier.weight(1f))
                    Text(invoiceDateString, modifier = Modifier.weight(1f))
                    Text(receivedDateString, modifier = Modifier.weight(1f))
                    Text(invoice.remain.toPrice().toString(), modifier = Modifier.weight(1f))
                    Text(dueDays, modifier = Modifier.weight(1f))
                }

            }
        }

    }
}
