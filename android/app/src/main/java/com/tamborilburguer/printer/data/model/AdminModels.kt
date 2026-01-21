package com.tamborilburguer.printer.data.model

import com.google.gson.annotations.SerializedName

/**
 * Resposta ao marcar pedido como "saiu para entrega"
 */
data class DeliveryResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("order")
    val order: Order,
    
    @SerializedName("customer_phone")
    val customerPhone: String,
    
    @SerializedName("display_id")
    val displayId: String
)

/**
 * Request para notificar entrega
 */
data class DeliveryNotificationRequest(
    @SerializedName("message")
    val message: String? = null
)

/**
 * Response da notificação de entrega
 */
data class DeliveryNotificationResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("order_id")
    val orderId: String,
    
    @SerializedName("customer_phone")
    val customerPhone: String,
    
    @SerializedName("display_id")
    val displayId: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("whatsapp_phone")
    val whatsappPhone: String
)

/**
 * Item do cardápio
 */
data class MenuItem(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("available")
    val available: Boolean
)

/**
 * Response do cardápio
 */
data class MenuResponse(
    @SerializedName("items")
    val items: List<MenuItem>
)

/**
 * Request para atualizar item do cardápio
 */
data class MenuItemUpdate(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("price")
    val price: Double? = null,
    
    @SerializedName("available")
    val available: Boolean? = null
)

/**
 * Status da loja
 */
data class StoreStatus(
    @SerializedName("isOpen")
    val isOpen: Boolean,
    
    @SerializedName("nextOpenTime")
    val nextOpenTime: String? = null, // Formato: "HH:mm"
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("lastUpdated")
    val lastUpdated: String? = null
)

/**
 * Request para atualizar status da loja
 */
data class StoreStatusUpdate(
    @SerializedName("isOpen")
    val isOpen: Boolean,
    
    @SerializedName("nextOpenTime")
    val nextOpenTime: String? = null, // Formato: "HH:mm"
    
    @SerializedName("message")
    val message: String? = null
)

/**
 * Response ao atualizar status da loja
 */
data class StoreStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("status")
    val status: StoreStatus
)
