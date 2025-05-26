package com.example.testapp.features.country
import kotlinx.serialization.Serializable


@Serializable
data class CountryData(
    val name: String,
    val capital: String,
    val language: String,
    val currency: String,
    val population: String,
    val timezone: String,
    val flagUrl: String,
    val mapUrl: String,
    val description: String,
    val landmarks: List<String> = emptyList(),
    val image: String? = null


)


