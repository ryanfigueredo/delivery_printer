package com.tamborilburguer.printer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tamborilburguer.printer.ui.AdminActivity
import com.tamborilburguer.printer.util.ServiceManager

/**
 * Activity principal da aplicação
 * Permite iniciar e parar o serviço de impressão
 */
class MainActivity : AppCompatActivity() {
    
    private val TAG = "MainActivity"
    private lateinit var statusText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "MainActivity criada")
        Log.d(TAG, "═══════════════════════════════════════")
        
        statusText = findViewById(R.id.statusText)
        
        // Botão para acessar Admin
        val adminButton = findViewById<Button>(R.id.adminButton)
        adminButton?.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }
        
        // Verifica se o serviço já está rodando
        val isRunning = ServiceManager.isServiceRunning(this)
        updateStatus(if (isRunning) "Serviço já está rodando" else "Iniciando serviço...")
        
        // Inicia o serviço automaticamente quando a activity é criada
        try {
        ServiceManager.startService(this)
            Log.d(TAG, "✅ Serviço iniciado com sucesso")
            updateStatus("Serviço iniciado - aguardando pedidos...")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao iniciar serviço", e)
            updateStatus("Erro ao iniciar serviço: ${e.message}")
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Atualiza status quando a activity volta ao foco
        val isRunning = ServiceManager.isServiceRunning(this)
        updateStatus(if (isRunning) "Serviço rodando - aguardando pedidos..." else "Serviço não está rodando")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity sendo destruída")
        // Opcional: parar o serviço quando a activity é destruída
        // ServiceManager.stopService(this)
    }
    
    private fun updateStatus(message: String) {
        runOnUiThread {
            statusText.text = message
            Log.d(TAG, "Status atualizado: $message")
        }
    }
}
