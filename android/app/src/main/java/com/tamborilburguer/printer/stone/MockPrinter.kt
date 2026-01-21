package com.tamborilburguer.printer.stone

import android.content.Context
import android.util.Log
import com.tamborilburguer.printer.data.model.Order
import kotlinx.coroutines.delay

/**
 * Mock da impressora Stone - Versão temporária para testar sem SDK
 * Quando tiver o token da Stone, substitua por PosPrintProvider real
 */
object MockPrinter {
    
    private const val TAG = "MockPrinter"
    
    /**
     * Simula impressão do pedido (apenas loga)
     * TODO: Substituir por PosPrintProvider quando tiver token da Stone
     */
    suspend fun printOrder(order: Order): Boolean {
        return try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "    TAMBORIL BURGUER - PEDIDO")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "Pedido: #${order.id}")
            Log.d(TAG, "Cliente: ${order.customerName}")
            Log.d(TAG, "Telefone: ${order.customerPhone}")
            Log.d(TAG, "Data: ${order.createdAt}")
            Log.d(TAG, "───────────────────────────────────────")
            Log.d(TAG, "ITENS:")
            
            order.items.forEach { item ->
                Log.d(TAG, "${item.quantity}x ${item.name} - R$ ${String.format("%.2f", item.price)}")
            }
            
            Log.d(TAG, "───────────────────────────────────────")
            Log.d(TAG, "TOTAL: R$ ${String.format("%.2f", order.totalPrice)}")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "⚠️ MODO TESTE - Não imprimiu de verdade")
            Log.d(TAG, "   Configure o token da Stone para imprimir")
            Log.d(TAG, "═══════════════════════════════════════")
            
            // Simula delay de impressão
            delay(1000)
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao simular impressão", e)
            false
        }
    }
}
