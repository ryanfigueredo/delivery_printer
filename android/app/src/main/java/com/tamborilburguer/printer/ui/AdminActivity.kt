package com.tamborilburguer.printer.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.tamborilburguer.printer.R
import com.tamborilburguer.printer.data.api.ApiClient
import com.tamborilburguer.printer.data.model.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity de administração
 * Permite controlar horário de funcionamento, ver pedidos, marcar entregas e editar cardápio
 */
class AdminActivity : AppCompatActivity() {
    
    private val TAG = "AdminActivity"
    private val apiService = ApiClient.apiService
    
    // Views - Controle de Loja
    private lateinit var storeStatusSwitch: Switch
    private lateinit var storeStatusText: TextView
    private lateinit var nextOpenTimeButton: Button
    private lateinit var customMessageEdit: EditText
    private lateinit var saveStoreStatusButton: Button
    
    // Views - Pedidos
    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var ordersRefreshButton: Button
    
    // Views - Cardápio
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var menuRefreshButton: Button
    
    // Tabs
    private lateinit var tabLayout: TabLayout
    
    // Dados
    private var currentStoreStatus: StoreStatus? = null
    private var selectedNextOpenTime: String? = null
    private var ordersList: List<Order> = emptyList()
    private var menuItems: List<MenuItem> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        
        Log.d(TAG, "AdminActivity criada")
        
        initViews()
        setupTabs()
        loadStoreStatus()
        loadOrders()
        loadMenu()
    }
    
    private fun initViews() {
        // Controle de Loja
        storeStatusSwitch = findViewById(R.id.storeStatusSwitch)
        storeStatusText = findViewById(R.id.storeStatusText)
        nextOpenTimeButton = findViewById(R.id.nextOpenTimeButton)
        customMessageEdit = findViewById(R.id.customMessageEdit)
        saveStoreStatusButton = findViewById(R.id.saveStoreStatusButton)
        
        // Pedidos
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView)
        ordersAdapter = OrdersAdapter { order -> onOrderClick(order) }
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.adapter = ordersAdapter
        ordersRefreshButton = findViewById(R.id.ordersRefreshButton)
        
        // Cardápio
        menuRecyclerView = findViewById(R.id.menuRecyclerView)
        menuAdapter = MenuAdapter { item -> onMenuItemClick(item) }
        menuRecyclerView.layoutManager = LinearLayoutManager(this)
        menuRecyclerView.adapter = menuAdapter
        menuRefreshButton = findViewById(R.id.menuRefreshButton)
        
        // Tabs
        tabLayout = findViewById(R.id.tabLayout)
        
        // Listeners
        storeStatusSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateStoreStatusUI(isChecked)
        }
        
        nextOpenTimeButton.setOnClickListener {
            showTimePicker()
        }
        
        saveStoreStatusButton.setOnClickListener {
            saveStoreStatus()
        }
        
        ordersRefreshButton.setOnClickListener {
            loadOrders()
        }
        
        menuRefreshButton.setOnClickListener {
            loadMenu()
        }
    }
    
    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Loja"))
        tabLayout.addTab(tabLayout.newTab().setText("Pedidos"))
        tabLayout.addTab(tabLayout.newTab().setText("Cardápio"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showStoreTab()
                    1 -> showOrdersTab()
                    2 -> showMenuTab()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun showStoreTab() {
        findViewById<ScrollView>(R.id.storeScrollView).visibility = android.view.View.VISIBLE
        findViewById<ScrollView>(R.id.ordersScrollView).visibility = android.view.View.GONE
        findViewById<ScrollView>(R.id.menuScrollView).visibility = android.view.View.GONE
    }
    
    private fun showOrdersTab() {
        findViewById<ScrollView>(R.id.storeScrollView).visibility = android.view.View.GONE
        findViewById<ScrollView>(R.id.ordersScrollView).visibility = android.view.View.VISIBLE
        findViewById<ScrollView>(R.id.menuScrollView).visibility = android.view.View.GONE
    }
    
    private fun showMenuTab() {
        findViewById<ScrollView>(R.id.storeScrollView).visibility = android.view.View.GONE
        findViewById<ScrollView>(R.id.ordersScrollView).visibility = android.view.View.GONE
        findViewById<ScrollView>(R.id.menuScrollView).visibility = android.view.View.VISIBLE
    }
    
    private fun loadStoreStatus() {
        lifecycleScope.launch {
            try {
                val response = apiService.getStoreStatus()
                if (response.isSuccessful) {
                    currentStoreStatus = response.body()
                    currentStoreStatus?.let { status ->
                        storeStatusSwitch.isChecked = status.isOpen
                        updateStoreStatusUI(status.isOpen)
                        selectedNextOpenTime = status.nextOpenTime
                        nextOpenTimeButton.text = status.nextOpenTime ?: "Definir horário"
                        customMessageEdit.setText(status.message ?: "")
                    }
                } else {
                    Toast.makeText(this@AdminActivity, "Erro ao carregar status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar status da loja", e)
                Toast.makeText(this@AdminActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateStoreStatusUI(isOpen: Boolean) {
        if (isOpen) {
            storeStatusText.text = "🟢 LOJA ABERTA"
            storeStatusText.setTextColor(getColor(android.R.color.holo_green_dark))
            nextOpenTimeButton.isEnabled = false
        } else {
            storeStatusText.text = "🔴 LOJA FECHADA"
            storeStatusText.setTextColor(getColor(android.R.color.holo_red_dark))
            nextOpenTimeButton.isEnabled = true
        }
    }
    
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                selectedNextOpenTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                nextOpenTimeButton.text = selectedNextOpenTime
            },
            hour,
            minute,
            true
        ).show()
    }
    
    private fun saveStoreStatus() {
        lifecycleScope.launch {
            try {
                val isOpen = storeStatusSwitch.isChecked
                val nextOpenTime = if (!isOpen) selectedNextOpenTime else null
                val message = customMessageEdit.text.toString().takeIf { it.isNotBlank() }
                
                val request = StoreStatusUpdate(
                    isOpen = isOpen,
                    nextOpenTime = nextOpenTime,
                    message = customMessageEdit.text.toString().takeIf { it.isNotBlank() }
                )
                
                val response = apiService.updateStoreStatus(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminActivity, "Status atualizado!", Toast.LENGTH_SHORT).show()
                    loadStoreStatus()
                } else {
                    Toast.makeText(this@AdminActivity, "Erro ao atualizar status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar status", e)
                Toast.makeText(this@AdminActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadOrders() {
        lifecycleScope.launch {
            try {
                val response = apiService.getAllOrders()
                if (response.isSuccessful) {
                    ordersList = response.body() ?: emptyList()
                    ordersAdapter.updateOrders(ordersList)
                    Toast.makeText(this@AdminActivity, "${ordersList.size} pedidos carregados", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AdminActivity, "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar pedidos", e)
                Toast.makeText(this@AdminActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadMenu() {
        lifecycleScope.launch {
            try {
                val response = apiService.getMenu()
                if (response.isSuccessful) {
                    menuItems = response.body()?.items ?: emptyList()
                    menuAdapter.updateMenu(menuItems)
                    Toast.makeText(this@AdminActivity, "${menuItems.size} itens carregados", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AdminActivity, "Erro ao carregar cardápio", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar cardápio", e)
                Toast.makeText(this@AdminActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun onOrderClick(order: Order) {
        // Mostrar opções: marcar como saiu para entrega, etc
        if (order.status == OrderStatus.PRINTED && order.orderType == "delivery") {
            showMarkAsOutForDeliveryDialog(order)
        } else {
            Toast.makeText(this, "Pedido ${order.displayId ?: order.id}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showMarkAsOutForDeliveryDialog(order: Order) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Marcar como saiu para entrega?")
            .setMessage("Pedido ${order.displayId ?: order.id} - ${order.customerName}")
            .setPositiveButton("Sim") { _, _ ->
                markOrderOutForDelivery(order)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun markOrderOutForDelivery(order: Order) {
        lifecycleScope.launch {
            try {
                val response = apiService.markOrderOutForDelivery(order.id)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminActivity, "Pedido marcado como saiu para entrega!", Toast.LENGTH_SHORT).show()
                    // Aqui pode chamar a API de notificação também
                    loadOrders()
                } else {
                    Toast.makeText(this@AdminActivity, "Erro ao marcar pedido", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao marcar pedido como saiu", e)
                Toast.makeText(this@AdminActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun onMenuItemClick(item: MenuItem) {
        // Mostrar dialog para editar item
        showEditMenuItemDialog(item)
    }
    
    private fun showEditMenuItemDialog(item: MenuItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_menu_item, null)
        val nameEdit = dialogView.findViewById<EditText>(R.id.itemNameEdit)
        val priceEdit = dialogView.findViewById<EditText>(R.id.itemPriceEdit)
        val availableSwitch = dialogView.findViewById<Switch>(R.id.itemAvailableSwitch)
        
        nameEdit.setText(item.name)
        priceEdit.setText(item.price.toString())
        availableSwitch.isChecked = item.available
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Editar Item")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val newName = nameEdit.text.toString()
                val newPrice = priceEdit.text.toString().toDoubleOrNull() ?: item.price
                val newAvailable = availableSwitch.isChecked
                
                updateMenuItem(item.id, newName, newPrice, newAvailable)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun updateMenuItem(itemId: String, name: String, price: Double, available: Boolean) {
        lifecycleScope.launch {
            try {
                val request = MenuItemUpdate(
                    id = itemId,
                    name = name,
                    price = price,
                    available = available
                )
                
                val response = apiService.updateMenuItem(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminActivity, "Item atualizado!", Toast.LENGTH_SHORT).show()
                    loadMenu()
                } else {
                    Toast.makeText(this@AdminActivity, "Erro ao atualizar item", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar item", e)
                Toast.makeText(this@AdminActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
