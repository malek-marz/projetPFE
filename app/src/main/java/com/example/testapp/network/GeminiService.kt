package com.example.testapp.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiService {

    @POST("models/gemini-2.0-flash:generateContent")
    fun getCountryDetails(
        @Body request: GeminiRequest,
        @Query("AIzaSyBt-LuJCBIFp4_2Xrl92SQIoi0VZq5Qklk") apiKey: String // Clé API passée comme paramètre de requête
    ): Call<GeminiResponse>
}

