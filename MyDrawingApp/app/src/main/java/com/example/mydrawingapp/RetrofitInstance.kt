package com.example.mydrawingapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://0.0.0.0:8081/static/" // Your server's base URL

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: DrawingApiService by lazy {
        retrofit.create(DrawingApiService::class.java)
    }
}