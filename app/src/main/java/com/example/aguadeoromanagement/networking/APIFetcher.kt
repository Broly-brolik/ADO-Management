package com.example.aguadeoromanagement.networking

import android.app.Activity
import android.util.Log
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class APIFetcher(context: Activity? = null) {
    private val url = "http://195.15.223.234/aguadeoro/connect.php"

    fun getSupplierRecipients(completion: (List<Supplier>, Boolean) -> Unit) {

        val q = Query("select * from Contacts where Type = 'Supplier' order by Contacts ASC")
        val s = q.execute(url)
        val res = mutableListOf<Supplier>()
        q.res.forEach { supplier ->
            res.add(Supplier(supplier["ID"]!!.toInt(), supplier["Contacts"]!!))
        }
        completion(res, s)
    }

    fun getSuppliers(completion: (List<Contact>, Boolean) -> Unit) {
        val q =
            Query("select * from Supplier order by SupplierName ASC")
        val s = q.execute(url)
        val res = mutableListOf<Contact>()
        q.res.forEach { contact ->
            val c = Contact()
            c.id = contact["ID"]!!.toInt()
            c.name = contact["SupplierName"] ?: ""
            c.email = contact["SupplierEmail"] ?: ""
            c.resp1 = contact["Responsable"] ?: ""
            c.remark = contact["Remark"] ?: ""
            c.address = contact["SupplierAddress"] ?: ""
            c.phone = contact["SupplierTel"] ?: ""
            c.email2 = contact["SupplierEmail2"] ?: ""
            c.type = contact["SupplierType"] ?: ""
            c.active = contact["Active"]?.toBooleanStrictOrNull() ?: false
            res.add(c)
        }
        completion(res, s)
    }

    fun getSupplier(id: String, isContact: Boolean, isName: Boolean, completion: (Contact, Boolean) -> Unit) {
        //TODO fix ID logic
        var query = if (isContact) {
            "select Supplier.*, Contacts.ID as contactId from Contacts inner join Supplier on Contacts.SupplierID = Supplier.ID WHERE Contacts.ID = $id"
        } else {
            "select Supplier.*, Contacts.ID as contactId from Supplier inner join Contacts on Contacts.Contacts = Supplier.SupplierName WHERE Supplier.ID = $id"
        }

        if (isName){
            query = "select Supplier.*, Contacts.ID as contactId from Supplier inner join Contacts on Contacts.Contacts = Supplier.SupplierName WHERE Supplier.SupplierName = '$id'"
        }


        val q = Query(query)
        val s = q.execute(url)
        Log.e("RES", q.res.toString())
        val res = mutableListOf<Contact>()
        q.res.forEach { contact ->
            val c = Contact()
            c.id = contact["ID"]!!.toInt()
            c.name = contact["SupplierName"] ?: ""
            c.email = contact["SupplierEmail"] ?: ""
            c.resp1 = contact["Responsable"] ?: ""
            c.remark = contact["Remark"] ?: ""
            c.address = contact["SupplierAddress"] ?: ""
            c.phone = contact["SupplierTel"] ?: ""
            c.email2 = contact["SupplierEmail2"] ?: ""
            c.type = contact["SupplierType"] ?: ""
            c.active = contact["Active"]?.toBooleanStrictOrNull() ?: false
            c.idToInsert = contact["contactId"]?.toIntOrNull() ?: 0
            res.add(c)
        }
        completion(res[0], s)
    }


    fun getSupplierHistory(contact: Contact, completion: (List<ContactHistory>, Boolean) -> Unit) {
        val q =
            Query("select * from ContactsHistory WHERE Contact = ${contact.id} order by HistoryDate DESC")
        val s = q.execute(url)
        val res = mutableListOf<ContactHistory>()
        q.res.forEach { contactHistory ->
            res.add(
                ContactHistory(
                    contactHistory["ID"]!!.toInt(),
                    contactHistory["Contact"]!!.toInt(),
                    contactHistory["HistoryDate"]!!,
                    contactHistory["Remark"] ?: "",
                )
            )
        }
        completion(res, s)
    }

    fun getSupplierInvoices(contact: Contact, completion: (List<Invoice>, Boolean) -> Unit) {
        val q =
            Query("select * from Invoice WHERE Supplier = ${contact.idToInsert} and (Status = 'Open' or Status = 'Investigate') order by InvoiceDate ASC")
        val s = q.execute(url)
        val res = mutableListOf<Invoice>()
        q.res.forEach { invoice ->
            res.add(
                Invoice(
                    id = invoice["ID"]!!.toInt(),
                    supplierId = contact.idToInsert,
                    invoiceDate = invoice["InvoiceDate"]!!,
                    invoiceNumber = invoice["InvNumber"]!!,
                    amount = invoice["Amount"]?.toDoubleOrNull() ?: 0.0,
                    receivedDate = invoice["ReceivedDate"]!!,
                    remark = invoice["Remark"]!!,
                    items = mutableListOf(),
                    dueOn = invoice["DueOn"] ?: "",
                    paid = invoice["Paid"]?.toDoubleOrNull() ?: 0.0,
                    remain = invoice["Remain"]?.toDoubleOrNull() ?: 0.0,
                    status = invoice["Status"] ?: "",
                    discountPercentage = invoice["Discount"]?.toDoubleOrNull() ?: 0.0,
                    registeredBy = invoice["RegisteredBy"] ?: "",
                    createdDate = invoice["CreatedDate"] ?: "",
                    currency = invoice["Curr"] ?: "",
                )
            )
        }
        completion(res, s)
    }

    fun getPaymentsForSupplier(contact: Contact, completion: (List<Payment>, Boolean) -> Unit) {
        val q = Query(
            "select InvoiceHistory.* " +
                    "FROM InvoiceHistory INNER JOIN Invoice ON InvoiceHistory.InvoiceID = Invoice.ID " +
                    "WHERE (((Invoice.Supplier)=${contact.id}))"
        )
        val s = q.execute(url)
        val res = mutableListOf<Payment>()
        q.res.forEach { payment ->
            res.add(
                Payment(
                    payment["ID"]!!.toInt(),
                    payment["InvoiceID"]!!.toInt(),
                    payment["AmountPaid"]?.toDoubleOrNull() ?: 0.0,
                    payment["ScheduledDate"]!!,
                    payment["PaymentBy"] ?: "",
                    payment["Remarks"] ?: "",
                    payment["Executed"]?.toBooleanStrictOrNull() ?: false
                )
            )
        }
        completion(res, s)
    }


    fun getSupplierOrderMainHistory(
        contactName: String,
        completion: (List<SupplierOrderMain>, Boolean) -> Unit
    ) {
        val q =
            Query("select * from SupplierOrderMain WHERE Recipient = '${contactName}' order by Status ASC, Deadline")
        val s = q.execute(url)
        val res = mutableListOf<SupplierOrderMain>()
        q.res.forEach { supplierOrderMain ->
            res.add(
                SupplierOrderMain(
                    supplierOrderMain["ID"]!!.toInt(),
                    supplierOrderMain["OrderComponentID"]!!.toIntOrNull(),
                    supplierOrderMain["Instruction"] ?: "",
                    supplierOrderMain["Deadline"] ?: "",
                    supplierOrderMain["Recipient"] ?: "",
                    supplierOrderMain["Status"] ?: "",
                    supplierOrderMain["CreatedDate"] ?: "",
                    supplierOrderMain["Step"] ?: "",
                    supplierOrderMain["InstructionCode"] ?: "",
                    supplierOrderMain["Remark"] ?: "",
                    supplierOrderMain["SupplierOrderNumber"] ?: "",
                    supplierOrderMain["Approval"] ?: "",
                    false
                )
            )
        }
        completion(res, s)
    }

    fun getSupplierOrders(
        supplier: String,
        date: String,
        dateFrom: String,
        completion: (MutableList<SupplierOrder>, Boolean) -> Unit
    ) {
        var whereClause =
            "((Contacts.Contacts)='$supplier') and (Invoice.ReceivedDate between #$dateFrom# and #$date#)"
        if (supplier == "All") {
            whereClause = "(Invoice.ReceivedDate between #$dateFrom# and #$date#)"
        }
        val q = Query(
            "select Contacts.Contacts, InvoiceItems.ID, InvoiceItems.SupplierOrderMain_ID," +
                    " InvoiceItems.UnitPrice,InvoiceItems.CalculatedAmount, InvoiceItems.Quantity,InvoiceItems.ApprovedDate, InvoiceItems.Approved, Invoice.Amount," +
                    " Customer.CustomerName, Invoice.ReceivedDate, SupplierOrderMain.Deadline," +
                    " InvoiceItems.InvoiceID, Invoice.InvNumber, InvoiceItems.Unit FROM (MainOrder INNER JOIN" +
                    " (OrderComponent INNER JOIN ((InvoiceItems INNER JOIN (Contacts INNER JOIN" +
                    " Invoice ON Contacts.ID = Invoice.Supplier) ON InvoiceItems.InvoiceID = Invoice.ID)" +
                    " INNER JOIN SupplierOrderMain ON InvoiceItems.SupplierOrderMain_ID =" +
                    " SupplierOrderMain.SupplierOrderNumber) ON OrderComponent.ID =" +
                    " SupplierOrderMain.OrderComponentID) ON MainOrder.OrderNumber = OrderComponent.OrderNumber)" +
                    " INNER JOIN Customer ON MainOrder.CustomerNumber = Customer.CustomerNumber" +
                    " WHERE $whereClause ORDER BY Invoice.ReceivedDate DESC, InvoiceItems.SupplierOrderMain_ID ASC;"
        )
        val success = q.execute(url)
        val res = mutableListOf<SupplierOrder>()
        q.res.forEach { mapRes ->
            val key = mapRes["SupplierOrderMain_ID"]!!
            if (res.none { it.supplierOrderId == key }) {
                var grams = 0.0
                var pieces = 0.0
                var price = 0.0
                if (mapRes["Unit"] == "Gramms") {
                    grams = mapRes["Quantity"]?.toDoubleOrNull() ?: 0.0
                } else {
                    pieces = mapRes["Quantity"]?.toDoubleOrNull() ?: 0.0
                    price = mapRes["UnitPrice"]?.toDoubleOrNull() ?: 0.0

                }
                res.add(
                    SupplierOrder(
                        mapRes["ID"]!!.toInt(),
                        key,
                        price,
                        pieces,
                        grams,
                        mapRes["Unit"]!!,
                        mapRes["ReceivedDate"]!!,
                        mapRes["InvNumber"]!!,
                        mapRes["Approved"]!!,
                        mapRes["InvoiceID"]!!,
                        mapRes["ApprovedDate"]!!,
                        mapRes["Approved"]!!.isNotEmpty(),
                        isExpanded = false,
                        type = 0,
                        amount = mapRes["CalculatedAmount"]?.toDoubleOrNull() ?: 0.0
                    )
                )
            } else {
                res.find { it.supplierOrderId == key }?.let { order ->
                    order.children.add(
                        SupplierOrder(
                            mapRes["ID"]!!.toInt(),
                            key,
                            mapRes["UnitPrice"]?.toDoubleOrNull() ?: 0.0,
                            mapRes["Quantity"]?.toDoubleOrNull() ?: 0.0,
                            0.0,
                            mapRes["Unit"]!!,
                            mapRes["ReceivedDate"]!!,
                            mapRes["InvNumber"]!!,
                            mapRes["Approved"]!!,
                            mapRes["InvoiceID"]!!,
                            mapRes["ApprovedDate"]!!,
                            mapRes["Approved"]!!.isNotEmpty(),
                            isExpanded = false,
                            type = 1
                        )
                    )
                    if (mapRes["Unit"] == "Gramms") {
                        order.grams += mapRes["Quantity"]?.toDoubleOrNull() ?: 0.0
                    } else {
                        order.quantity += mapRes["Quantity"]?.toDoubleOrNull() ?: 0.0
                        order.unitPrice += mapRes["UnitPrice"]?.toDoubleOrNull() ?: 0.0
                        order.amount += mapRes["CalculatedAmount"]?.toDoubleOrNull() ?: 0.0
                    }
                }
            }
        }
        completion(res, success)
    }

    fun getStockHistory(
        supplier: String,
        onlyHistory: Boolean = false,
        completion: (List<StockHistory>, Boolean) -> Unit
    ) {
        var whereClause = "where StockHistory1.Supplier = '$supplier'"
        if (supplier == "All") {
            whereClause = ""
        }
//        val start_date = LocalDate.now().minusDays(30)
//        date.minusDays(60)

        val q =
            if (onlyHistory) Query("select * FROM StockHistory1 WHERE Supplier = '$supplier' AND (InvoiceID IS NULL OR InvoiceID = 0)")
            else Query(
                "select StockHistory1.*, Products.ProductCode " +
                        " FROM StockHistory1 INNER JOIN Products ON StockHistory1.ProductID = Products.ID $whereClause "
            )
        val success = q.execute(url)
        val res = mutableListOf<StockHistory>()
        q.res.forEach { map ->
            res.add(
                StockHistory(
                    id = map["ID"]!!.toInt(),
                    orderNumber = map["OrderNumber"]!!,
                    supplierOrderNumber = map["SupplierOrderMainID"]!!,
                    historicDate = map["HistoricDate"]!!,
                    productId = map["ProductID"]!!.toIntOrNull() ?: 0,
                    supplier = map["Supplier"]!!,
                    detail = map["Detail"]!!,
                    type = map["Type"]!!.toIntOrNull() ?: -1,
                    quantity = map["Quantity"]?.toDoubleOrNull() ?: 0.0,
                    cost = map["Cost"]?.toDoubleOrNull() ?: 0.0,
                    remark = map["Remark"]!!,
                    weight = map["Weight"]?.toDoubleOrNull() ?: 0.0,
                    loss = map["Loss"]!!,
                    process = map["Process"]!!,
                    flow = map["Flow"]!!,
                    shortCode = map.getOrDefault("ProductCode", ""),
                    )
            )
        }
        res.sortBy { it.orderNumber }
        completion(res, success)
    }

    fun getPayments(completion: (MutableList<Payment>, Boolean) -> Unit) {
        val q = Query("select * from InvoiceHistory")
        val success = q.execute(url)
        val res = mutableListOf<Payment>()
        q.res.forEach { map ->
            res.add(
                Payment(
                    map["ID"]!!.toInt(),
                    map["InvoiceID"]!!.toInt(),
                    map["AmountPaid"]?.toDoubleOrNull() ?: 0.0,
                    map["ScheduledDate"]!!,
                    map["PaymentBy"]!!,
                    map["Remarks"]!!,
                    (map["Executed"]?.toIntOrNull() ?: 0) == 1,
                )
            )
        }
        completion(res, success)
    }

    fun verifyItem(
        items: MutableMap<String, String>,
        completion: (Boolean) -> Unit
    ) {
        for (item in items) {
            val q =
                Query("update InvoiceItems set Approved = '${item.value}', ApprovedDate=NOW() where SupplierOrderMain_ID = '${item.key}'")
            val success = q.execute(url)
            if (!success) {
                completion(false)
                return
            }
        }
        completion(true)
    }

    fun getPeopleThatCanApprove(completion: (List<Map<String, String>>, Boolean) -> Unit) {
        val q = Query("select * from OptionValues where Type = 'Approval'")
        val success = q.execute(url)
        completion(q.res, success)
    }

    fun getInvoices(
        supplier: Int,
        from: Long,
        to: Long,
        completion: (List<Invoice>, Boolean) -> Unit
    ) {
        val dateFrom = Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        val dateTo = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        var query =
            Query("select top 100 * from Invoice where Supplier = $supplier and (InvoiceDate between #$dateFrom# and #$dateTo#) order by ID desc")
        var success = query.execute(url)
        if (!success) {
            completion(mutableListOf(), false)
            return
        }
        val invoicesRes = query.res
        val q = "select InvoiceItems.*" +
                " from InvoiceItems INNER JOIN Invoice ON InvoiceItems.InvoiceID = Invoice.ID" +
                " where Invoice.Supplier = $supplier and (Invoice.InvoiceDate between #$dateFrom# and #$dateTo#)"
        query = Query(q)
        success = query.execute(url)
        val invoiceItemsRes = query.res
        val invoices = mutableListOf<Invoice>()
        val items = mutableListOf<InvoiceItem>()
        invoicesRes.forEach { map ->
            invoiceItemsRes.forEach { item ->
                if (item["InvoiceID"] == map["ID"]) {
                    items.add(
                        InvoiceItem(
                            id = item["ID"]!!.toInt(),
                            invoiceId = item["InvoiceID"]!!.toInt(),
                            supplierOrderID = item["SupplierOrderMain_ID"]!!,
                            quantity = item["Quantity"]?.toDoubleOrNull() ?: 0.0,
                            unitPrice = item["UnitPrice"]?.toDoubleOrNull() ?: 0.0,
                            unit = item["Unit"]!!,
                            approved = item["Approved"]!!,
                            approvedDate = item["ApprovedDate"]!!,
                            product = item["Product"]?.toIntOrNull() ?: 0,
                            accountCode = item["AccountCode"]!!,
                            process = item["Process"]!!,
                            flow = item["Flow"]!!,
                            vat = item["VAT"]?.toDoubleOrNull() ?: 0.0,
                            detail = item["Detail"]!!,
                        )
                    )
                }
            }
            invoices.add(
                Invoice(
                    map["ID"]!!.toInt(),
                    supplierId = supplier,
                    invoiceDate = map["InvoiceDate"]!!,
                    invoiceNumber = map["InvNumber"]!!,
                    amount = map["Amount"]?.toDoubleOrNull() ?: 0.0,
                    receivedDate = map["ReceivedDate"]!!,
                    remark = map["Remark"]!!,
                    items = items,
                    dueOn = map["DueOn"] ?: "",
                    paid = map["Paid"]?.toDoubleOrNull() ?: 0.0,
                    remain = map["Remain"]?.toDoubleOrNull() ?: 0.0,
                    status = map["Status"] ?: "",
                    discountPercentage = map["Discount"]?.toDoubleOrNull() ?: 0.0,
                    currency = map["Curr"] ?: "",
                    createdDate = map["CreatedDate"] ?: "",
                    registeredBy = map["RegisteredBy"] ?: "",
                )
            )
        }
        completion(invoices, success)
    }

    fun getOptionValues(completion: (List<OptionValue>, Boolean) -> Unit) {
        val q = Query("select * from OptionValues")
        val s = q.execute(url)
        val res = mutableListOf<OptionValue>()
        q.res.forEach { map ->
            res.add(
                OptionValue(
                    map["ID"]!!.toInt(),
                    map["Type"]!!,
                    map["OptionKey"]!!,
                    map["OptionValue"]!!
                )
            )
        }
        completion(res, s)
    }

    fun updateInvoice(invoice: Invoice, completion: (Boolean) -> Unit) {

        var createdDateToInsert = ""
        if (invoice.createdDate.isNotEmpty()) {
            createdDateToInsert = try {
                LocalDate.parse(invoice.createdDate, Constants.forUsFormatter)
                    .format(Constants.accessFormater)
            } catch (e: java.lang.Exception) {
                invoice.createdDate
            }
        }

        val dueOnToInsert = invoice.dueOn

        val q =
            Query(
                "update Invoice set " +
                        "Supplier = ${invoice.supplierId}, InvoiceDate = #${invoice.invoiceDate}#," +
                        " InvNumber = '${invoice.invoiceNumber}', Amount = ${invoice.amount}," +
                        " ReceivedDate = #${invoice.receivedDate}#, Remark = '${invoice.remark}'," +
                        " DueOn = #${dueOnToInsert}#, Paid = ${invoice.paid}, Remain = ${invoice.remain}," +
                        " Status = '${invoice.status}', Discount = ${invoice.discountAmount}," +
                        " Curr = '${invoice.currency}', CreatedDate = #$createdDateToInsert#," +
                        " RegisteredBy = '${invoice.registeredBy}' WHERE ID = ${invoice.id} "
            )
        val success = q.execute(url)
        completion(success)
    }


    fun createInvoice(invoice: Invoice, completion: (Int) -> Unit) {

        var createdDateToInsert = ""
        if (invoice.createdDate.isNotEmpty()) {
            createdDateToInsert = LocalDate.parse(invoice.createdDate, Constants.forUsFormatter)
                .format(Constants.accessFormater)
        }

//        val dueOnToInsert = LocalDate.parse(invoice.dueOn, DateTimeFormatter.ISO_DATE)
//            .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        val dueOnToInsert = invoice.dueOn

        val q =
            Query(
                "insert into Invoice " +
                        "(Supplier, InvoiceDate, InvNumber, Amount, ReceivedDate, Remark, DueOn, Paid, Remain, Status, Discount, Curr, CreatedDate, RegisteredBy)" +
                        " values (${invoice.supplierId}, #${invoice.invoiceDate}#, '${invoice.invoiceNumber}', ${invoice.amount}, #${invoice.receivedDate}#, '${invoice.remark}'," +
                        " #${dueOnToInsert}#, ${invoice.paid}, ${invoice.remain}, '${invoice.status}', ${invoice.discountAmount}, '${invoice.currency}', #$createdDateToInsert#," +
                        " '${invoice.registeredBy}')"
            )
        val newId = q.execute(url, true)
        completion(newId)
    }

    fun createContact(contact: Contact, completion: (Boolean) -> Unit) {
        var q =
            Query(
                "insert into Supplier" +
                        "(SupplierName, SupplierEmail, Responsable, Remark, SupplierAddress, SupplierTel, Responsable2, SupplierEmail2, SupplierType, Active)" +
                        " values ('${contact.name}', '${contact.email}', '${contact.resp1}', '${contact.remark}', '${contact.address}', '${contact.phone}', '${contact.resp2}', '${contact.email2}', '${contact.type}', ${contact.active})"

            )
        val id = q.execute(url, true)
        if (id == 0) {
            completion(false)
            return
        }
        if (contact.type != "Supplier") {
            completion(true)
            return
        }


        q = Query(
            "insert into Contacts " +
                    "(Contacts, Type, SupplierID) values ('${contact.name}', '${contact.type}', ${id})"
        )
        var success2 = q.execute(url)

        if (!success2) {
            completion(false)
            return
        }
        q = Query(
            "insert into OptionValues " +
                    "(Type, OptionKey, OptionValue) values ('${contact.type}', ${id}, '${contact.name}')"
        )
        success2 = q.execute(url)


        completion(success2)
    }

    fun updateContact(contact: Contact, completion: (Boolean) -> Unit) {
        var q =
            Query(
                "update Supplier" +
                        "SET SupplierName = '${contact.name}', " +
                        "SupplierEmail = '${contact.email}', " +
                        "Responsable = '${contact.resp1}', " +
                        "Remark = '${contact.remark}', " +
                        "SupplierAddress = '${contact.address}', " +
                        "SupplierTel = '${contact.phone}', " +
                        "Responsable2 = '${contact.resp2}', " +
                        "SupplierEmail2 = '${contact.email2}', " +
                        "SupplierType = '${contact.type}', " +
                        "Active ${contact.active} " +
                        "WHERE id = ${contact.id}"

            )
    }

    fun getStockHistoryForOrder(
        supplierOrderId: Int,
        supplierOrderNumber: String,
//        supplierName: String,
        completion: (List<StockHistory>, Boolean) -> Unit
    ) {
        val query = if (supplierOrderId == 0) {
            "select * from StockHistory1  where OrderNumber = '${supplierOrderNumber}'"
        } else {
            "select StockHistory1.*, Products.ProductCode from StockHistory1  inner join Products on StockHistory1.ProductID = Products.ID where OrderNumber = '${supplierOrderNumber}'"
        }
        val q =
            Query(query)
        val success = q.execute(url)
        val res = mutableListOf<StockHistory>()
        q.res.forEach { map ->
            res.add(
                StockHistory(
                    id = map["ID"]!!.toInt(),
                    orderNumber = map["OrderNumber"]!!,
                    supplierOrderNumber = map["SupplierOrderMainID"]!!,
                    historicDate = map["HistoricDate"]!!,
                    productId = map["ProductID"]?.toIntOrNull() ?: 0,
                    supplier = map["Supplier"]!!,
                    detail = map["Detail"]!!,
                    type = map["Type"]!!.toInt(),
                    quantity = map["Quantity"]?.toDoubleOrNull() ?: 0.0,
                    cost = map["Cost"]?.toDoubleOrNull() ?: 0.0,
                    remark = map["Remark"]!!,
                    weight = map["Weight"]?.toDoubleOrNull() ?: 0.0,
                    loss = map["Loss"]!!,
                    process = map["Process"]!!,
                    flow = map["Flow"]!!,
                    shortCode = map["ProductCode"] ?: "no product",
                )
            )
        }
        completion(res, success)
    }



    fun deleteItem(id: Int, stock: Boolean, completion: (Boolean) -> Unit) {
        Log.e("ID", id.toString())
        var query = "delete from InvoiceItems WHERE ID = $id"
        var q = Query(query)
        var success = q.execute(url)
        if (!success) {
            completion(false)
            return
        }
        if (stock) {
            query = "delete from StockHistory1 WHERE ID = $id"
            q = Query(query)
            success = q.execute(url)
            completion(success)
            return
        }
        completion(true)

    }


    fun getFlows(completion: (List<String>, Boolean) -> Unit) {
        val q = Query("select * from Flow")
        val success = q.execute(url)
        val res = mutableListOf<String>()
        q.res.forEach { map ->
            res.add(
                map["Flow"]!!
            )
        }
        completion(res, success)
    }

    fun getProcesses(completion: (List<String>, Boolean) -> Unit) {
        val q = Query("select * from Process")
        val success = q.execute(url)
        val res = mutableListOf<String>()
        q.res.forEach { map ->
            res.add(
                map["Process"]!!
            )
        }
        completion(res, success)
    }

    fun getProduct(id: Int, completion: (Product?, Boolean) -> Unit) {
        val q = Query("select * from Products WHERE ID = $id ")
        val success = q.execute(url)
        val res = mutableListOf<Product>()
        q.res.forEach { map ->
            res.add(
                Product(
                    id,
                    description = map["Description"]!!,
                    productCode = map["ProductCode"]!!,
                    unit = map["Unit"] ?: ""
                )
            )
        }
        if (res.size > 0) {
            completion(res[0], success)
        } else {
            completion(null, false)
        }
    }

    fun updateInvoiceRemaining(
        amount: Double,
        remain: Double,
        id: Int,
        completion: (Boolean) -> Unit
    ) {
        val q =
            Query("update Invoice set Remain = ${remain}, Amount = $amount where ID = $id")
        val success = q.execute(url)
        completion(success)
    }

    fun getSupplierOrdersOfIds(
        ids: List<String>,
        completion: (List<SupplierOrderMain>, Boolean) -> Unit
    ) {
        var ids_string = ""
        if (ids.isNotEmpty()) {
            ids.forEach { ids_string += ("'$it', ") }
            ids_string = ids_string.removeRange(ids_string.length - 2, ids_string.length)
            Log.e("ids", ids_string)
        }
        val query = Query(
            "select * FROM SupplierOrderMain WHERE SupplierOrderNumber in ($ids_string)"
        )
        val success = query.execute(url)
        val res = mutableListOf<SupplierOrderMain>()
        query.res.forEach { supplierOrderMain ->
            res.add(
                SupplierOrderMain(
                    supplierOrderMain["ID"]!!.toInt(),
                    supplierOrderMain["OrderComponentID"]!!.toIntOrNull(),
                    supplierOrderMain["Instruction"] ?: "",
                    supplierOrderMain["Deadline"] ?: "",
                    supplierOrderMain["Recipient"] ?: "",
                    supplierOrderMain["Status"] ?: "",
                    supplierOrderMain["CreatedDate"] ?: "",
                    supplierOrderMain["Step"] ?: "",
                    supplierOrderMain["InstructionCode"] ?: "",
                    supplierOrderMain["Remark"] ?: "",
                    supplierOrderMain["SupplierOrderNumber"] ?: "",
                    supplierOrderMain["Approval"] ?: "",
                    false
                )
            )
        }
        completion(res, success)
    }

    fun getSupplierOrdersOfInvoice(
        invoice: Invoice,
        completion: (List<SupplierOrderMain>, Boolean) -> Unit
    ) {
        val query = Query(
            "select SupplierOrderMain.*" +
                    " FROM (Invoice INNER JOIN InvoiceItems ON Invoice.ID = InvoiceItems.InvoiceID) INNER JOIN SupplierOrderMain ON InvoiceItems.SupplierOrderMain_ID = SupplierOrderMain.SupplierOrderNumber " +
                    " WHERE Invoice.ID=${invoice.id}"
        )
        val success = query.execute(url)
        val res = mutableListOf<SupplierOrderMain>()
        query.res.forEach { supplierOrderMain ->
            res.add(
                SupplierOrderMain(
                    supplierOrderMain["ID"]!!.toInt(),
                    supplierOrderMain["OrderComponentID"]!!.toIntOrNull(),
                    supplierOrderMain["Instruction"] ?: "",
                    supplierOrderMain["Deadline"] ?: "",
                    supplierOrderMain["Recipient"] ?: "",
                    supplierOrderMain["Status"] ?: "",
                    supplierOrderMain["CreatedDate"] ?: "",
                    supplierOrderMain["Step"] ?: "",
                    supplierOrderMain["InstructionCode"] ?: "",
                    supplierOrderMain["Remark"] ?: "",
                    supplierOrderMain["SupplierOrderNumber"] ?: "",
                    supplierOrderMain["Approval"] ?: "",
                    false
                )
            )
        }
        completion(res, success)
    }

    fun getInvoiceItems(invoice: Invoice, completion: (List<InvoiceItem>, Boolean) -> Unit) {
        val q = Query(
            "select * from InvoiceItems where InvoiceID = ${invoice.id}"
        )
        val success = q.execute(url, "Supplier invoices")
        val res = mutableListOf<InvoiceItem>()
        q.res.forEach { item ->
            res.add(
                InvoiceItem(
                    id = item["ID"]!!.toInt(),
                    invoiceId = item["InvoiceID"]!!.toInt(),
                    supplierOrderID = item["SupplierOrderMain_ID"]!!,
                    quantity = item["Quantity"]?.toDoubleOrNull() ?: 0.0,
                    unitPrice = item["UnitPrice"]?.toDoubleOrNull() ?: 0.0,
                    unit = item["Unit"]!!,
                    approved = item["Approved"]!!,
                    approvedDate = item["ApprovedDate"]!!,
                    product = item["Product"]?.toIntOrNull() ?: 0,
                    accountCode = item["AccountCode"]!!,
                    process = item["Process"]!!,
                    flow = item["Flow"]!!,
                    vat = item["VAT"]?.toDoubleOrNull() ?: 0.0,
                    detail = item["Detail"]!!,
                )
            )
        }
        completion(res, success)
    }


    fun updateSupplierOrderStatus(
        supplierOrderNumber: String,
        status: String,
        completion: (Boolean) -> Unit
    ) {
        val q =
            Query("update SupplierOrderMain set Status = '${status}' where SupplierOrderNumber = '$supplierOrderNumber'")
        val success = q.execute(url)
        completion(success)
    }

}