package com.tamborilburguer.printer.util

import android.content.Context
import android.content.Intent
import com.tamborilburguer.printer.service.OrderPrinterService

/**
 * Utilitário para gerenciar o serviço de impressão
 */
object ServiceManager {
    
    /**
     * Inicia o serviço de impressão
     */
    fun startService(context: Context) {
        val intent = Intent(context, OrderPrinterService::class.java).apply {
            action = OrderPrinterService.ACTION_START_SERVICE
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
    
    /**
     * Para o serviço de impressão
     */
    fun stopService(context: Context) {
        val intent = Intent(context, OrderPrinterService::class.java).apply {
            action = OrderPrinterService.ACTION_STOP_SERVICE
        }
        context.startService(intent)
    }
    
    /**
     * Verifica se o serviço está rodando
     * Nota: Esta é uma verificação básica. Para verificação mais robusta,
     * considere usar um BroadcastReceiver ou LiveData
     */
    fun isServiceRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val services = activityManager.getRunningServices(Integer.MAX_VALUE)
        
        return services.any { service ->
            OrderPrinterService::class.java.name == service.service.className
        }
    }
}
