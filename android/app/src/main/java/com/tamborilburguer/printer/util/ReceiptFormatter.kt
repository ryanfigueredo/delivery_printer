package com.tamborilburguer.printer.util

import com.tamborilburguer.printer.data.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilitário para formatar pedidos em layout de cupom térmico 58mm
 * Formatação otimizada para impressora térmica de 58mm
 */
object ReceiptFormatter {
    
    private const val LINE_WIDTH = 32 // Caracteres por linha em 58mm
    private const val SEPARATOR = "--------------------------------"
    
    /**
     * Formata um pedido para impressão em cupom térmico 58mm
     */
    fun formatOrder(order: Order): List<String> {
        val lines = mutableListOf<String>()
        
        // Cabeçalho compacto
        lines.add(centerText("TAMBORIL BURGUER"))
        lines.add(centerText("dmtn.com.br"))
        lines.add(SEPARATOR)
        
        // Número do pedido destacado - prioriza display_id, depois daily_sequence formatado, por último UUID curto
        val orderId = when {
            !order.displayId.isNullOrBlank() -> order.displayId
            order.dailySequence != null -> "#${String.format("%03d", order.dailySequence)}"
            else -> "#${order.id.take(6).uppercase()}"
        }
        lines.add("")
        lines.add(centerText("PEDIDO $orderId"))
        
        // Posição na fila destacada
        if (order.dailySequence != null) {
            lines.add(centerText("${order.dailySequence}º NA FILA"))
        }
        lines.add(SEPARATOR)
        
        // Data e hora (compacto)
        val formattedDate = formatDateTime(order.createdAt)
        lines.add("Data: $formattedDate")
        
        // Cliente (compacto)
        lines.add("Cliente: ${order.customerName}")
        // Limpar telefone de caracteres estranhos (remover @, letras, etc)
        val telefoneLimpo = order.customerPhone.replace(Regex("[^0-9]"), "")
        lines.add("Tel: $telefoneLimpo")
        
        // Tipo de pedido
        if (!order.orderType.isNullOrBlank()) {
            val tipoTexto = if (order.orderType == "delivery") "Tipo: DELIVERY" else "Tipo: RESTAURANTE"
            lines.add(tipoTexto)
            
            // Se for delivery, mostrar endereço
            if (order.orderType == "delivery" && !order.deliveryAddress.isNullOrBlank()) {
                lines.add("Endereco: ${order.deliveryAddress}")
            }
        }
        lines.add(SEPARATOR)
        
        // Itens do pedido (compacto)
        order.items.forEach { item ->
            val itemTotal = item.price * item.quantity
            val priceLine = String.format(Locale("pt", "BR"), "R$ %.2f", itemTotal)
            // Formato: "2x Hambúrguer        R$ 36,00"
            val itemLine = "${item.quantity}x ${item.name}"
            val padding = LINE_WIDTH - itemLine.length - priceLine.length
            lines.add(itemLine + " ".repeat(if (padding > 0) padding else 1) + priceLine)
        }
        
        lines.add(SEPARATOR)
        
        // Total (destacado)
        val totalLine = "TOTAL: ${String.format(Locale("pt", "BR"), "R$ %.2f", order.totalPrice)}"
        lines.add(boldText(totalLine))
        lines.add("")
        
        // Método de pagamento e tempo
        if (!order.paymentMethod.isNullOrBlank()) {
            lines.add("Pagamento: ${order.paymentMethod}")
        }
        
        // Tempo estimado
        if (order.estimatedTime != null && order.estimatedTime > 0) {
            val tempoMin = order.estimatedTime
            val tempoMax = order.estimatedTime + 10
            lines.add("Tempo: ${tempoMin}-${tempoMax} min")
        }
        
        lines.add(SEPARATOR)
        
        // Rodapé compacto
        lines.add(centerText("Obrigado!"))
        lines.add("") // Espaço para cortar
        
        return lines
    }
    
    /**
     * Centraliza texto considerando largura de 58mm
     */
    private fun centerText(text: String): String {
        val padding = (LINE_WIDTH - text.length) / 2
        return if (padding > 0) {
            " ".repeat(padding) + text
        } else {
            text
        }
    }
    
    /**
     * Alinha texto à direita
     */
    private fun alignRight(text: String): String {
        val padding = LINE_WIDTH - text.length
        return if (padding > 0) {
            " ".repeat(padding) + text
        } else {
            text
        }
    }
    
    /**
     * Formata texto em negrito (algumas impressoras suportam)
     * Se não suportar, retorna o texto normal
     */
    private fun boldText(text: String): String {
        // Algumas impressoras térmicas suportam códigos de formatação
        // Ajuste conforme a documentação da sua impressora
        return text.toUpperCase(Locale("pt", "BR"))
    }
    
    /**
     * Formata data e hora
     */
    private fun formatDateTime(dateTimeString: String): String {
        return try {
            // Tenta parsear ISO 8601
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateTimeString) ?: Date()
            outputFormat.format(date)
        } catch (e: Exception) {
            // Se falhar, retorna a string original
            dateTimeString
        }
    }
}
