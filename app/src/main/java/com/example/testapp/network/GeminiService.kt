package com.example.testapp.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Service interface for Gemini API
interface GeminiService {

    @POST("v1beta2/generations:generateText")
    fun getCountryDetails(@Body request: GeminiRequest): Call<GeminiResponse>
}
