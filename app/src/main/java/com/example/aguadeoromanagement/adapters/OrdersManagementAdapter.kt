package com.example.aguadeoromanagement.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.models.StockHistory
import com.example.aguadeoromanagement.models.SupplierOrder
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OrdersManagementAdapter(
    private val context: Context,
    private var dataSet: MutableList<SupplierOrder>,
    private val stockHistory: MutableList<StockHistory>,
) :
    RecyclerView.Adapter<ViewHolder>() {
    private lateinit var checkboxListener: CheckboxListener
    private var selectedItems = mutableListOf<SupplierOrder>()
    private var childrenCodes = mutableListOf<Int>()

    interface CheckboxListener {
        fun onCheckboxClicked(data: SupplierOrder, isChecked: Boolean)
    }

    fun setCheckboxListener(listener: CheckboxListener) {
        checkboxListener = listener
    }

    class GroupViewHolder(view: View) : ViewHolder(view) {
        val supplierOrder: TextView
        val invoiceDate: TextView
        val invoiceNumber: TextView
        val price: TextView
        val approved: CheckBox
        val inText: TextView
        val outText: TextView
        val approvedDate: TextView
        val expandImg: ImageView

        init {
            supplierOrder = view.findViewById(R.id.supplierOrder)
            invoiceDate = view.findViewById(R.id.invoiceDate)
            invoiceNumber = view.findViewById(R.id.invoiceNumber)
            price = view.findViewById(R.id.price)
            approved = view.findViewById(R.id.approved)
            inText = view.findViewById(R.id.`in`)
            outText = view.findViewById(R.id.out)
            approvedDate = view.findViewById(R.id.approvedDate)
            expandImg = view.findViewById(R.id.expandImg)
        }
    }

    class ChildViewHolder(view: View) : ViewHolder(view) {
        val productCode: TextView
        val outCount: TextView
        val returnedCount: TextView
        val finishedCount: TextView
        val deltaCount: TextView
        val varianceCount: TextView

        init {
            productCode = view.findViewById(R.id.productCode)
            outCount = view.findViewById(R.id.outCount)
            returnedCount = view.findViewById(R.id.returnedCount)
            finishedCount = view.findViewById(R.id.finishedCount)
            deltaCount = view.findViewById(R.id.deltaCount)
            varianceCount = view.findViewById(R.id.varianceCount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(context).inflate(R.layout.line_orders, parent, false)
            GroupViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(context).inflate(R.layout.line_orders_child, parent, false)
            ChildViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
        if (item.type == 0) {
            holder as GroupViewHolder
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val invoiceDateDate = LocalDate.parse(item.receivedDate, formatter)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val price =
                BigDecimal(item.amount).setScale(
                    2,
                    BigDecimal.ROUND_HALF_UP
                ).toDouble()
            val grams = BigDecimal(item.grams).setScale(
                2,
                BigDecimal.ROUND_HALF_UP
            ).toDouble()
            holder.inText.text = "${grams}g"
            holder.price.text = "$price CHF"
            holder.supplierOrder.text = item.supplierOrderId
            holder.invoiceDate.text = invoiceDateDate
            holder.invoiceNumber.text = item.invoiceNumber
            holder.approved.setOnCheckedChangeListener(null)
            if (item.approvedDate.isNotEmpty()) {
                val date = try {
                    LocalDate.parse(item.approvedDate, formatter)
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
                } catch (e: Exception) {
                    item.approvedDate
                }

                holder.approvedDate.visibility = View.VISIBLE
                holder.approvedDate.text = date
                holder.approved.visibility = View.INVISIBLE
            } else {
                holder.approvedDate.visibility = View.INVISIBLE
                holder.approved.visibility = View.VISIBLE
                holder.approved.isChecked = selectedItems.contains(item)
            }
            holder.approved.setOnCheckedChangeListener { compoundButton, isChecked ->
                checkboxListener.onCheckboxClicked(item, isChecked)
            }
            if (item.isExpanded) {
                holder.expandImg.setImageResource(R.drawable.ic_baseline_expand_less_24)
            } else {
                holder.expandImg.setImageResource(R.drawable.ic_baseline_expand_more_24)
            }
            if (item.children.isEmpty()) {
                holder.expandImg.visibility = View.GONE
            } else {
                holder.expandImg.visibility = View.VISIBLE
            }


            holder.expandImg.setOnClickListener {
                expandOrCollapseParentItem(item, position)
            }
            var outTotal = 0.0
            stockHistory.forEach { line ->
                if (line.orderNumber == item.supplierOrderId) {
                    if (line.type == 2) {
                        outTotal += line.quantity
                    }
                }
            }
            holder.outText.text = "${BigDecimal(outTotal).setScale(2, BigDecimal.ROUND_HALF_UP)}g"
        } else {
            holder as ChildViewHolder
            var outCount = 0.0
            var returnedCount = 0.0
            var finishedCount = 0.0
            var varianceCount = 0.0
            val productCode = item.productId
            var shortCode = ""
            stockHistory.forEach { line ->
                if (line.orderNumber == item.supplierOrderId && line.productId == productCode) {
                    shortCode = line.shortCode
                    if (line.type == 2) {
                        outCount += line.quantity
                    } else {
                        if (line.flow?.lowercase() == "returned") {
                            returnedCount += line.quantity
                        } else if (line.flow?.lowercase() == "finished") {
                            finishedCount += line.quantity
                        } else if (line.flow?.lowercase() == "variance") {
                            varianceCount += line.quantity
                        }
                    }
                }
            }
            val deltaCount: BigDecimal =
                BigDecimal(outCount).negate().add(BigDecimal(returnedCount))
                    .add(BigDecimal(finishedCount))
            holder.deltaCount.text = "${deltaCount.setScale(2, BigDecimal.ROUND_HALF_UP)}g"
            holder.outCount.text = "${outCount}g"
            holder.returnedCount.text = "${returnedCount}g"
            holder.finishedCount.text = "${finishedCount}g"
            holder.productCode.text = "$productCode($shortCode)"
            holder.varianceCount.text = "${varianceCount}g"
        }

    }

    override fun getItemViewType(position: Int): Int = dataSet[position].type

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setData(data: MutableList<SupplierOrder>, stockHistoryData: MutableList<StockHistory>) {
        dataSet.clear()
        dataSet.addAll(data)
        stockHistory.clear()
        stockHistory.addAll(stockHistoryData)
        notifyDataSetChanged()
    }

    fun updateCheck(data: SupplierOrder) {
        val index = dataSet.indexOfFirst { it.supplierOrderId == data.supplierOrderId }
        dataSet[index].approved = data.approved
        if (data.approved.isNotEmpty()) {
            selectedItems.add(dataSet[index])
        } else {
            selectedItems.remove(dataSet[index])
        }
        notifyItemChanged(index)
    }

    private fun expandOrCollapseParentItem(order: SupplierOrder, position: Int) {
        if (order.isExpanded) {
            collapseParentRow(position)
        } else {
            expandParentRow(position)
        }
    }

    private fun expandParentRow(position: Int) {
        val currentOrder = dataSet[position]
        val children = currentOrder.children
        childrenCodes.clear()
        currentOrder.isExpanded = true
        var nextPosition = position
        if (currentOrder.type == 0) {
            stockHistory.forEach { stock ->
                if (stock.orderNumber == currentOrder.supplierOrderId) {
                    if (!childrenCodes.contains(stock.productId)) {
                        childrenCodes.add(stock.productId)
                    }
                }
            }
            for (i in childrenCodes.indices) {
                val childToAdd = children[i]
                childToAdd.productId = childrenCodes[i]
                dataSet.add(++nextPosition, childToAdd)
            }
            notifyDataSetChanged()
        }
    }

    private fun collapseParentRow(position: Int) {
        val currentOrder = dataSet[position]
        val children = currentOrder.children
        currentOrder.isExpanded = false
        if (currentOrder.type == 0) {
            children.forEach { child ->
                dataSet.remove(child)
            }
            notifyDataSetChanged()
        }
    }

}