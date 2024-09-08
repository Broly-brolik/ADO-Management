package com.example.aguadeoromanagement.utils

import android.content.Context
import android.util.Log
import com.example.aguadeoromanagement.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import java.lang.Math.pow
import java.lang.Math.round
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

fun roundWithNDecimal(num: Double, n: Int): Double {
    return (10.0.pow(n.toDouble()) * num).roundToInt() / 10.0.pow(n.toDouble())
}

fun Double.toPrice(): Double {
    return roundWithNDecimal(this, 2)
}

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()

    return GoogleSignIn.getClient(context, signInOptions)
}

fun getDate(fromAccess: Boolean = false): String {
    val date = LocalDate.now()
    if (fromAccess) {
        return date.format(Constants.forUsFormatter)
    }
    return date.format(Constants.accessFormater)
}

fun getTime(): String {
    val date = LocalDateTime.now()
    return date.format(Constants.accessFormaterDateTime)
}

fun filterLocationData(
    res: ArrayList<Map<String, String>>,
    withVerified: Boolean = false
): ArrayList<Map<String, String>> {
    val notes = ArrayList<Map<String, String>>()
    val codes = ArrayList<String>()
    val data = ArrayList<Map<String, String>>()
    for (i in res.indices) {
        if (res[i]["InventoryCode"] == "O-356") {
            Log.e("comparing 0-356", res[i].toString())
        }
        if (res[i]["Action"] == null || res[i]["Action"]?.isEmpty() == true) {
            continue
        }

        var transferred = false
        var timestamp1 = Timestamp(1)
        var timestamp2 = Timestamp(0)
        if (res[i]["HistoryDate"] != "") {
            if (res[i]["Action"] != null) {
                if (res[i]["Action"] == "Transferred") {
                    transferred = true
                }
            }
//            if (res[i]["InventoryCode"] == "O-356") {
//                Log.e("comparing 0-356", res[i].toString())
//            }
            if (!transferred) {
                for (j in res.indices) {
                    if (res[i]["InventoryCode"] == res[j]["InventoryCode"]) {
                        if (res[j]["HistoryDate"] != "") {
                            if (res[j]["Action"] != null) {
                                if (res[j]["Action"] == "Transferred") {
                                    try {
                                        val dateFormat =
                                            SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                        val parsedDate =
                                            dateFormat.parse(res[i]["HistoryDate"])
                                        timestamp1 =
                                            Timestamp(parsedDate.time) // date of the current element
                                    } catch (e: Exception) {
                                        Log.d("error 1", "" + e)
                                    }
                                    try {
                                        val dateFormat =
                                            SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                        val parsedDate =
                                            dateFormat.parse(res[j]["HistoryDate"])
                                        timestamp2 =
                                            Timestamp(parsedDate.time) // date of the elements we are checking
                                    } catch (e: Exception) {
                                        Log.d("error 2", "" + e)
                                    }
                                    if (timestamp2.time > timestamp1.time) { // if the current element is not the oldest we discard it
//                                        Log.d(
//                                            "t1t2",
//                                            "code " + res[j]["InventoryCode"] + " t1 " + timestamp1.time + " t2 " + timestamp2.time
//                                        )
                                        if (res[i]["InventoryCode"] == "O-356") {
                                            Log.e(
                                                "not selecting",
                                                "${res[j]} is older than ${res[i]}"
                                            )
                                        }
                                        transferred = true
                                    } else {
                                        if (res[i]["InventoryCode"] == "O-356") {
                                            Log.e("selecting", "${res[i]}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Log.d("OK", "" + res[i]["InventoryCode"] + " " + transferred)
            if (!codes.contains(res[i]["InventoryCode"]) && !transferred) {
                val map = HashMap<String, String>()
                map[res[i]["InventoryCode"]!!] = res[i]["Notes"]!!
                codes.add(res[i]["InventoryCode"]!!)
                data.add(res[i])
                notes.add(map)
            }
        }
    }
    return data
}
