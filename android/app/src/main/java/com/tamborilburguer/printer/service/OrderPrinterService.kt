package com.tamborilburguer.printer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
// import br.com.stone.posandroid.providers.PosPrintProvider  // TODO: Descomente quando tiver token da Stone
import com.tamborilburguer.printer.MainActivity
import com.tamborilburguer.printer.stone.MockPrinter
import com.tamborilburguer.printer.R
import com.tamborilburguer.printer.data.api.ApiClient
import com.tamborilburguer.printer.data.model.Order
import com.tamborilburguer.printer.data.model.OrderStatus
import com.tamborilburguer.printer.data.model.UpdateStatusRequest
import com.tamborilburguer.printer.util.ReceiptFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// import stone.application.interfaces.StoneCallbackInterface  // TODO: Descomente quando tiver token da Stone

/**
 * Serviço Foreground que faz polling na API a cada 5 segundos
 * e imprime pedidos automaticamente usando o SDK da Stone
 */
class OrderPrinterService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var pollingJob: Job? = null
    
    companion object {
        private const val TAG = "OrderPrinterService"
        private const val CHANNEL_ID = "OrderPrinterServiceChannel"
        private const val NOTIFICATION_ID = 1
        private const val POLLING_INTERVAL_MS = 5000L // 5 segundos
        
        const val ACTION_START_SERVICE = "com.tamborilburguer.printer.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.tamborilburguer.printer.STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d(TAG, "═══════════════════════════════════════")
        android.util.Log.d(TAG, "OrderPrinterService criado")
        android.util.Log.d(TAG, "Versão do Android: ${Build.VERSION.SDK_INT}")
        android.util.Log.d(TAG, "Modelo: ${Build.MODEL}")
        android.util.Log.d(TAG, "Fabricante: ${Build.MANUFACTURER}")
        android.util.Log.d(TAG, "═══════════════════════════════════════")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d(TAG, "onStartCommand chamado - Action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                android.util.Log.d(TAG, "Iniciando serviço foreground...")
                startForeground(NOTIFICATION_ID, createNotification())
                android.util.Log.d(TAG, "Serviço foreground iniciado. Iniciando polling...")
                startPolling()
                android.util.Log.d(TAG, "Polling iniciado com sucesso")
            }
            ACTION_STOP_SERVICE -> {
                android.util.Log.d(TAG, "Parando serviço...")
                stopPolling()
                stopForeground(true)
                stopSelf()
            }
            else -> {
                android.util.Log.w(TAG, "Ação desconhecida: ${intent?.action}")
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d(TAG, "OrderPrinterService sendo destruído")
        stopPolling()
        serviceScope.cancel()
        android.util.Log.d(TAG, "OrderPrinterService destruído")
    }

    /**
     * Inicia o polling da API
     */
    private fun startPolling() {
        pollingJob?.cancel()
        android.util.Log.d(TAG, "Iniciando polling com intervalo de ${POLLING_INTERVAL_MS}ms")
        pollingJob = serviceScope.launch {
            var iteration = 0
            while (isActive) {
                iteration++
                try {
                    android.util.Log.d(TAG, "--- Iteração #$iteration ---")
                    checkForOrders()
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "❌ Erro no polling (iteração #$iteration)", e)
                }
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    /**
     * Para o polling
     */
    private fun stopPolling() {
        android.util.Log.d(TAG, "Parando polling...")
        pollingJob?.cancel()
        pollingJob = null
        android.util.Log.d(TAG, "Polling parado")
    }

    /**
     * Verifica se há pedidos para imprimir
     */
    private suspend fun checkForOrders() = withContext(Dispatchers.IO) {
        try {
            // Verifica conectividade antes de fazer requisição
            if (!isNetworkAvailable()) {
                android.util.Log.w(TAG, "⚠️ Sem conexão com internet - pulando verificação")
                updateNotification("Sem conexão - aguardando...")
                return@withContext
            }
            
            android.util.Log.d(TAG, "Verificando pedidos na API...")
            val response = ApiClient.apiService.getNextOrderToPrint()
            
            android.util.Log.d(TAG, "Resposta da API - Código: ${response.code()}, Sucesso: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val order = response.body()!!
                android.util.Log.d(TAG, "✅ Pedido encontrado: ${order.id}")
                updateNotification("Processando pedido #${order.id}...")
                processOrder(order)
            } else if (response.code() == 404) {
                // Não há pedidos pendentes - comportamento normal
                android.util.Log.d(TAG, "Nenhum pedido pendente (404)")
                updateNotification("Aguardando pedidos...")
            } else {
                android.util.Log.e(TAG, "❌ Erro ao buscar pedido: código ${response.code()}, mensagem: ${response.message()}")
                if (response.errorBody() != null) {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e(TAG, "Corpo do erro: $errorBody")
                }
                updateNotification("Erro na API (${response.code()})")
            }
        } catch (e: java.net.UnknownHostException) {
            android.util.Log.e(TAG, "❌ Erro de DNS - não foi possível resolver o host", e)
            updateNotification("Erro de conexão - verifique a internet")
        } catch (e: java.net.SocketTimeoutException) {
            android.util.Log.e(TAG, "❌ Timeout na requisição", e)
            updateNotification("Timeout - tentando novamente...")
        } catch (e: java.io.IOException) {
            android.util.Log.e(TAG, "❌ Erro de I/O na requisição", e)
            updateNotification("Erro de conexão")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Erro inesperado ao verificar pedidos", e)
            android.util.Log.e(TAG, "Tipo: ${e.javaClass.simpleName}, Mensagem: ${e.message}")
            e.printStackTrace()
            updateNotification("Erro inesperado")
        }
    }
    
    /**
     * Verifica se há conexão com internet disponível
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            if (network == null) {
                false
            } else {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities == null) {
                    false
                } else {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Atualiza a notificação com nova mensagem
     */
    private fun updateNotification(message: String) {
        try {
            val notification = createNotification(message)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Erro ao atualizar notificação", e)
        }
    }

    /**
     * Processa um pedido: imprime e confirma na API
     * 
     * ⚠️ MODO TESTE: Usando MockPrinter temporariamente
     * Quando tiver o token da Stone, descomente o código abaixo e use PosPrintProvider
     */
    private suspend fun processOrder(order: Order) = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d(TAG, "═══════════════════════════════════════")
            android.util.Log.d(TAG, "🖨️ Processando pedido ${order.id}")
            android.util.Log.d(TAG, "Cliente: ${order.customerName}")
            android.util.Log.d(TAG, "Total: R$ ${String.format("%.2f", order.totalPrice)}")
            android.util.Log.d(TAG, "Itens: ${order.items.size}")
            
            // ⚠️ VERSÃO TEMPORÁRIA - Mock sem SDK Stone
            // TODO: Substituir por PosPrintProvider quando tiver token da Stone
            android.util.Log.d(TAG, "⚠️ Usando MockPrinter (modo teste)")
            val printSuccess = MockPrinter.printOrder(order)
            
            if (printSuccess) {
                android.util.Log.d(TAG, "✅ Pedido ${order.id} processado (modo teste)")
                updateNotification("Pedido #${order.id} processado")
                
                // Confirma a impressão na API mesmo em modo teste
                confirmOrderPrinted(order.id)
            } else {
                android.util.Log.e(TAG, "❌ Falha ao processar pedido ${order.id}")
                updateNotification("Erro ao processar pedido #${order.id}")
            }
            
            /* 
            // CÓDIGO REAL COM SDK STONE (descomente quando tiver token):
            withContext(Dispatchers.Main) {
                val receiptLines = ReceiptFormatter.formatOrder(order)
                val printProvider = PosPrintProvider(applicationContext)
                
                receiptLines.forEach { line ->
                    printProvider.addLine(line)
                }
                
                printProvider.connectionCallback = object : StoneCallbackInterface {
                    override fun onSuccess() {
                        android.util.Log.d("OrderPrinterService", "Pedido ${order.id} impresso com sucesso")
                        serviceScope.launch(Dispatchers.IO) {
                            confirmOrderPrinted(order.id)
                        }
                    }

                    override fun onError() {
                        val errors = printProvider.listOfErrors?.joinToString(", ") ?: "Erro desconhecido"
                        android.util.Log.e("OrderPrinterService", "Erro ao imprimir pedido ${order.id}: $errors")
                    }
                }
                
                printProvider.execute()
            }
            */
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Erro ao processar pedido ${order.id}", e)
            android.util.Log.e(TAG, "Tipo: ${e.javaClass.simpleName}, Mensagem: ${e.message}")
            e.printStackTrace()
            updateNotification("Erro ao processar pedido")
        }
    }

    /**
     * Confirma na API que o pedido foi impresso
     */
    private suspend fun confirmOrderPrinted(orderId: String) = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d(TAG, "Confirmando impressão do pedido $orderId na API...")
            val request = UpdateStatusRequest(OrderStatus.PRINTED)
            val response = ApiClient.apiService.confirmOrderPrinted(orderId, request)
            
            if (response.isSuccessful) {
                android.util.Log.d(TAG, "✅ Status do pedido $orderId confirmado como PRINTED")
            } else {
                android.util.Log.e(TAG, "❌ Erro ao confirmar impressão: código ${response.code()}, mensagem: ${response.message()}")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Erro ao confirmar impressão do pedido $orderId", e)
            android.util.Log.e(TAG, "Tipo: ${e.javaClass.simpleName}, Mensagem: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Cria o canal de notificação (necessário para Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Serviço de Impressão de Pedidos",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificação do serviço de impressão automática de pedidos"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Cria a notificação para o Foreground Service
     */
    private fun createNotification(message: String = "Serviço rodando - aguardando pedidos..."): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Impressão de Pedidos")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
