package com.example.aguadeoromanagement.adapters.suppliersFragment

import android.content.ComponentName
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.aguadeoromanagement.MainActivity
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.databinding.LineSupplierOrdersHistoryBinding
import com.example.aguadeoromanagement.models.SupplierOrderMain
import com.example.aguadeoromanagement.networking.APIFetcher
import com.example.aguadeoromanagement.networking.getOrderIdFromSupplierOrderNumber
import com.example.aguadeoromanagement.networking.getStockHistoryForOrders
import kotlinx.coroutines.*

class SupplierOrdersHistoryAdapter(
    private val supplierOrders: List<SupplierOrderMain>,
    private val activity: FragmentActivity
) :
    RecyclerView.Adapter<SupplierOrdersHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LineSupplierOrdersHistoryBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.line_supplier_orders_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return supplierOrders.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val soNumber = supplierOrders[position].supplierOrderNumber
        holder.binding.orderNumber.text = soNumber
        holder.binding.orderDeadline.text = supplierOrders[position].deadline
        holder.binding.orderStatus.text = supplierOrders[position].status

        if (supplierOrders[position].isSelected) {
            holder.binding.root.setBackgroundColor(
                ContextCompat.getColor(activity, R.color.highlightColor)
            )
        } else {
            holder.binding.root.setBackgroundColor(
                ContextCompat.getColor(activity, R.color.white)
            )
        }

        holder.binding.root.setOnClickListener {
            //Log.e("bien", supplierOrders[position].supplierOrderNumber)
            CoroutineScope(Dispatchers.Default).launch {
                val id = getOrderIdFromSupplierOrderNumber(soNumber)
                withContext(Dispatchers.Main){
                    val intent = Intent()
                    intent.component =
                        ComponentName("com.aguadeoro", "com.aguadeoro.activity.OrderDetailActivity")
                    intent.putExtra("OrderNumber", id.toString())
                    intent.putExtra("Caller", activity.localClassName)
                    activity.startActivity(intent)
                }
            }
        }
        holder.binding.root.setOnLongClickListener{
            // Toggle selection state
            supplierOrders[position].isSelected = !supplierOrders[position].isSelected
            // Notify adapter to refresh this item
            notifyItemChanged(position)
            // Return true to indicate the long click was handled
            true
        }
    }

    fun getSelectedItems(): List<SupplierOrderMain> {
        return supplierOrders.filter { it.isSelected }
    }
}