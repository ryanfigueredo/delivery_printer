package com.tamborilburguer.printer.data.api

import com.google.gson.GsonBuilder
import com.tamborilburguer.printer.data.model.DoubleTypeAdapter
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP configurado com Retrofit
 * Inclui autenticação via API_KEY no header
 */
object ApiClient {
    
    // URL base da API (Vercel)
    private const val BASE_URL = "https://tamboril-burguer.vercel.app/"
    
    // API_KEY para autenticação (mesma do backend)
    private const val API_KEY = "7e229ceb049fcfa2d3c6ff29b4e50d202bd3855804e66fb02487419e79124b26"
    
    /**
     * Interceptor para adicionar X-API-Key header em todas as requisições
     */
    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .header("X-API-Key", API_KEY)
            .build()
        chain.proceed(newRequest)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor) // Adiciona X-API-Key header em todas as requisições
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Gson customizado para lidar com total_price como string ou number
    private val gson = GsonBuilder()
        .registerTypeAdapter(Double::class.java, DoubleTypeAdapter())
        .registerTypeAdapter(java.lang.Double::class.java, DoubleTypeAdapter())
        .create()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
