package com.example.aguadeoromanagement.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    var id: Int = 0,
    var name: String = "",
    var resp1: String = "",
    var remark: String = "",
    var type: String = "",
    var address: String = "",
    var phone: String = "",
    var email: String = "",
    var active: Boolean? = null,
    var resp2: String = "",
    var email2: String = "",
    var idToInsert: Int = 0
) : Parcelable