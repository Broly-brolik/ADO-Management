package com.example.aguadeoromanagement.networking.api

import android.util.Log
import com.example.aguadeoromanagement.Constants
import com.example.aguadeoromanagement.models.Contact
import com.example.aguadeoromanagement.networking.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getSupplier(id: String, isContact: Boolean): Pair<Contact, Boolean> {
    return withContext(Dispatchers.IO) {

        //TODO fix ID logic
        val query = if (isContact) {
            "select Supplier.*, Contacts.ID as contactId from Contacts inner join Supplier on Contacts.SupplierID = Supplier.ID WHERE Contacts.ID = $id"
        } else {
            "select Supplier.*, Contacts.ID as contactId from Supplier inner join Contacts on Contacts.Contacts = Supplier.SupplierName WHERE Supplier.ID = $id"
        }
        val q = Query(query)
        val s = q.execute(Constants.url)
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
        try {
            return@withContext Pair(res[0], s)
        } catch (e: Exception) {
            return@withContext Pair(Contact(), false)
        }
    }

}
