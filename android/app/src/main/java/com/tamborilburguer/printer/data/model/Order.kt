package com.tamborilburguer.printer.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

/**
 * Modelo de dados representando um pedido
 */
data class Order(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("customer_name")
    val customerName: String,
    
    @SerializedName("customer_phone")
    val customerPhone: String,
    
    @SerializedName("items")
    val items: List<OrderItem>,
    
    @SerializedName("total_price")
    val totalPrice: Double,
    
    @SerializedName("status")
    val status: OrderStatus,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("order_number")
    val orderNumber: Int? = null,
    
    @SerializedName("daily_sequence")
    val dailySequence: Int? = null,
    
    @SerializedName("display_id")
    val displayId: String? = null,
    
    @SerializedName("payment_method")
    val paymentMethod: String? = null,
    
    @SerializedName("customer_total_orders")
    val customerTotalOrders: Int? = null,
    
    @SerializedName("order_type")
    val orderType: String? = null,
    
    @SerializedName("estimated_time")
    val estimatedTime: Int? = null,
    
    @SerializedName("delivery_address")
    val deliveryAddress: String? = null
)

/**
 * Item do pedido
 */
data class OrderItem(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("price")
    val price: Double
)

/**
 * Status do pedido
 */
enum class OrderStatus {
    @SerializedName("pending")
    PENDING,
    
    @SerializedName("printed")
    PRINTED,
    
    @SerializedName("finished")
    FINISHED,
    
    @SerializedName("out_for_delivery")
    OUT_FOR_DELIVERY
}

/**
 * Resposta da API para próximo pedido a imprimir
 */
data class NextOrderResponse(
    @SerializedName("order")
    val order: Order?,
    
    @SerializedName("has_order")
    val hasOrder: Boolean
)

/**
 * Request para atualizar status do pedido
 */
data class UpdateStatusRequest(
    @SerializedName("status")
    val status: OrderStatus
)

/**
 * TypeAdapter customizado para converter total_price de string para Double
 * A API retorna total_price como string, mas precisamos de Double
 */
class DoubleTypeAdapter : JsonDeserializer<Double> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Double {
        return when {
            json == null -> 0.0
            json.isJsonPrimitive -> {
                val primitive = json.asJsonPrimitive
                when {
                    primitive.isNumber -> primitive.asDouble
                    primitive.isString -> {
                        try {
                            primitive.asString.toDouble()
                        } catch (e: NumberFormatException) {
                            0.0
                        }
                    }
                    else -> 0.0
                }
            }
            else -> 0.0
        }
    }
}
