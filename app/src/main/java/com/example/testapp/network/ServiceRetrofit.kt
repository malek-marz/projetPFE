package com.example.testapp.network

import com.example.testapp.features.country.CountryData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CountryApi {

    @GET("country/details")
    suspend fun getCountryDetails(@Query("interests") interests: String): Response<CountryData>
}


