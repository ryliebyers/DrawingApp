package com.example.mydrawingapp

import retrofit2.http.GET

interface DrawingApiService {
    @GET("images.html") // Ensure correct endpoint
    suspend fun getDrawings(): List<Drawing> // Ensure 'suspend' keyword is properly placed
}