package com.tamborilburguer.printer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tamborilburguer.printer.R
import com.tamborilburguer.printer.data.model.MenuItem

class MenuAdapter(
    private val onItemClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder>() {
    
    private var items: List<MenuItem> = emptyList()
    
    fun updateMenu(newItems: List<MenuItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuItemViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount() = items.size
    
    inner class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.menuItemName)
        private val priceText: TextView = itemView.findViewById(R.id.menuItemPrice)
        private val availableText: TextView = itemView.findViewById(R.id.menuItemAvailable)
        
        fun bind(item: MenuItem) {
            nameText.text = item.name
            priceText.text = "R$ ${String.format("%.2f", item.price)}"
            availableText.text = if (item.available) "✅ Disponível" else "❌ Indisponível"
            availableText.setTextColor(
                if (item.available) {
                    itemView.context.getColor(android.R.color.holo_green_dark)
                } else {
                    itemView.context.getColor(android.R.color.holo_red_dark)
                }
            )
            
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
