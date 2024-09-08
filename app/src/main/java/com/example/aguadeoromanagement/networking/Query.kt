package com.example.aguadeoromanagement.networking

import android.util.Log
import android.widget.Toast
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.utils.JSONParser
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.json.JSONObject

class Query(mQuery: String) {
    private val parser: JSONParser
    private val params: MutableList<NameValuePair>
    private var json: JSONObject? = null
    private var mQuery: String
    private val method: String
    val res: ArrayList<Map<String, String>>

    init {
        parser = JSONParser()
        params = ArrayList()
        res = ArrayList()
        this.mQuery = mQuery
        method = "POST"
    }

    fun getmQuery(): String {
        return mQuery
    }

    fun setmQuery(mQuery: String) {
        this.mQuery = mQuery
    }

    fun execute(url: String?, target_db: String = ""): Boolean {
//        Log.e("TARGETDB", target_db)
        res.clear()
        params.add(BasicNameValuePair("query", mQuery))
        params.add(BasicNameValuePair("destination", "aguadeoro19"))
        if (target_db.isNotEmpty()){
            params.add(BasicNameValuePair("target_db", target_db))
        }

        try {
            Log.d("server adress ", url!!)
            json = parser.makeHttpRequest(url, method, params)
            Log.d("returned json is", "" + json)
            val success = json?.getInt("success")
            if (success == 1) {
                if (mQuery.startsWith("select") || mQuery.startsWith("SELECT")) {
                    Log.d("aaa", json.toString())
                    val users = json?.getJSONArray("result")
                    // looping through All Objects
                    if (users != null) {
                        for (i in 0 until users.length()) {
                            val obj = users.getJSONObject(i)
                            val id = obj.length()
                            val v = obj.names()
                            val strRes: MutableMap<String, String> = HashMap()
                            for (j in 0 until id) {
                                strRes[v.getString(j)] = obj.getString(v.getString(j))
                            }
                            res.add(strRes)
                        }
                    }
                } else {
                    Log.d("bbb", "not a select $mQuery")
                }
            } else {
                Log.e("OOPS", "Error " + json?.getString("message"))
                Log.e("OOPS", "Error " + json?.getString("query"))
                return false
            }
        } catch (e: Exception) {
            Log.d("ERROR", "could not connect")
            e.printStackTrace()
            return false
        }
        return true
    }

    fun execute(url: String?, insert: Boolean?): Int {
        res.clear()
        params.add(BasicNameValuePair("query", mQuery))
        params.add(BasicNameValuePair("destination", "aguadeoro19"))
        return try {
            Log.d("server adress ", url!!)
            json = parser.makeHttpRequest(url, method, params)
            Log.d("returned json is", "" + json)
            val success = json?.getInt("success")
            if (success == 1) {
                if (mQuery.startsWith("insert") || mQuery.startsWith("INSERT")) {
                    val id = json?.getString("return_id")
                    //                    Log.e("ID", id);
                    id!!.toInt()
                } else {
                    Log.d("bbb", "not a select $mQuery")
                    0
                }
            } else {
                Log.e("OOPS", "Error " + json?.getString("message"))
                Log.e("OOPS", "Error " + json?.getString("query"))
                Toast.makeText(ManagementApplication.getAppContext(), "error query", Toast.LENGTH_LONG).show()
                0
            }
        } catch (e: Exception) {
            Log.d("ERROR", "could not connect")
            e.printStackTrace()
            0
        }
    }
}