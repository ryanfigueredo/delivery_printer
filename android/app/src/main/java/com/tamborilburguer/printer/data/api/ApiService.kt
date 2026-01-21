package com.tamborilburguer.printer.data.api

import com.tamborilburguer.printer.data.model.OrderStatus
import com.tamborilburguer.printer.data.model.UpdateStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST

/**
 * Interface da API para comunicação com o backend
 */
interface ApiService {
    
    /**
     * Busca o próximo pedido a ser impresso
     * Retorna diretamente o Order ou 404 se não houver pedidos pendentes
     */
    @GET("api/orders/next-to-print")
    suspend fun getNextOrderToPrint(): Response<com.tamborilburguer.printer.data.model.Order>
    
    /**
     * Atualiza o status de um pedido (confirma impressão)
     */
    @PATCH("api/orders/{orderId}/status")
    suspend fun confirmOrderPrinted(
        @Path("orderId") orderId: String,
        @Body request: UpdateStatusRequest
    ): Response<Unit>
    
    /**
     * Marca pedido como "saiu para entrega"
     */
    @PATCH("api/orders/{orderId}/mark-out-for-delivery")
    suspend fun markOrderOutForDelivery(
        @Path("orderId") orderId: String
    ): Response<com.tamborilburguer.printer.data.model.DeliveryResponse>
    
    /**
     * Notifica cliente via WhatsApp quando pedido sair para entrega
     */
    @POST("api/orders/{orderId}/notify-delivery")
    suspend fun notifyDelivery(
        @Path("orderId") orderId: String,
        @Body request: com.tamborilburguer.printer.data.model.DeliveryNotificationRequest
    ): Response<com.tamborilburguer.printer.data.model.DeliveryNotificationResponse>
    
    /**
     * Lista todos os pedidos (para admin)
     */
    @GET("api/orders")
    suspend fun getAllOrders(): Response<List<com.tamborilburguer.printer.data.model.Order>>
    
    /**
     * Busca cardápio (para admin)
     */
    @GET("api/admin/menu")
    suspend fun getMenu(): Response<com.tamborilburguer.printer.data.model.MenuResponse>
    
    /**
     * Atualiza item do cardápio (para admin)
     */
    @PATCH("api/admin/menu")
    suspend fun updateMenuItem(
        @Body request: com.tamborilburguer.printer.data.model.MenuItemUpdate
    ): Response<com.tamborilburguer.printer.data.model.MenuItem>
    
    /**
     * Obter status da loja (aberta/fechada)
     */
    @GET("api/admin/store-hours")
    suspend fun getStoreStatus(): Response<com.tamborilburguer.printer.data.model.StoreStatus>
    
    /**
     * Atualizar status da loja (abrir/fechar)
     */
    @POST("api/admin/store-hours")
    suspend fun updateStoreStatus(
        @Body request: com.tamborilburguer.printer.data.model.StoreStatusUpdate
    ): Response<com.tamborilburguer.printer.data.model.StoreStatusResponse>
}
