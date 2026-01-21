package com.tamborilburguer.printer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tamborilburguer.printer.R
import com.tamborilburguer.printer.data.model.Order
import com.tamborilburguer.printer.data.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.*

class OrdersAdapter(
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {
    
    private var orders: List<Order> = emptyList()
    
    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }
    
    override fun getItemCount() = orders.size
    
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val displayIdText: TextView = itemView.findViewById(R.id.orderDisplayId)
        private val customerNameText: TextView = itemView.findViewById(R.id.orderCustomerName)
        private val statusText: TextView = itemView.findViewById(R.id.orderStatus)
        private val totalText: TextView = itemView.findViewById(R.id.orderTotal)
        private val dateText: TextView = itemView.findViewById(R.id.orderDate)
        
        fun bind(order: Order) {
            displayIdText.text = order.displayId ?: "#${order.dailySequence?.toString()?.padStart(3, '0') ?: "000"}"
            customerNameText.text = order.customerName
            statusText.text = getStatusText(order.status)
            totalText.text = "R$ ${String.format("%.2f", order.totalPrice)}"
            
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse(order.createdAt)
                val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                dateText.text = displayFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateText.text = order.createdAt
            }
            
            itemView.setOnClickListener {
                onOrderClick(order)
            }
        }
        
        private fun getStatusText(status: OrderStatus): String {
            return when (status) {
                OrderStatus.PENDING -> "⏳ Pendente"
                OrderStatus.PRINTED -> "✅ Impresso"
                OrderStatus.FINISHED -> "✅ Finalizado"
                OrderStatus.OUT_FOR_DELIVERY -> "🚚 Saiu para entrega"
            }
        }
    }
}
