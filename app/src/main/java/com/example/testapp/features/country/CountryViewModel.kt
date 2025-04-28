package com.example.testapp.presentation.country

import androidx.lifecycle.ViewModel
import kotlinx.serialization.json.*
import com.example.testapp.model.CountryData
import com.example.testapp.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class CountryViewModel : ViewModel() {

    private val _countryData = mutableStateOf<CountryData?>(null)
    val countryData: State<CountryData?> = _countryData

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun fetchCountryInfoBasedOnInterests(interests: List<String>) {
        _isLoading.value = true
        _errorMessage.value = null  // Clear any previous error message

        val apiKey = "AIzaSyDlQiusEchlTg8jq6_SrW1nWG2-epihOj8"

        val prompt = buildPromptBasedOnInterests(interests)

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            )
        )

        // Utilisation de geminiService pour appeler l'API
        RetrofitClient.geminiService.getCountryDetails(request)
            .enqueue(object : Callback<GeminiResponse> {
                override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                    _isLoading.value = false

                    if (response.isSuccessful) {
                        val contentText = response.body()
                            ?.candidates
                            ?.firstOrNull()
                            ?.content
                            ?.parts
                            ?.firstOrNull()
                            ?.text ?: ""

                        val country = parseCountryJson(contentText)
                        _countryData.value = country
                    } else {
                        _errorMessage.value = "Erreur lors de la récupération des données"
                    }
                }

                override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "Échec de la requête : ${t.message}"
                }
            })
    }

    private fun buildPromptBasedOnInterests(interests: List<String>): String {
        val interestsStr = interests.joinToString(", ")
        return """
            Donne-moi les informations suivantes sur un pays avec les intérêts suivants ($interestsStr) au format JSON :
            {
              "name": "",
              "capital": "",
              "language": "",
              "currency": "",
              "population": "",
              "timezone": "",
              "flagUrl": "[Lien direct vers le drapeau]",
              "mapUrl": "[Lien direct vers la carte]",
              "description": "",
              "landmarkUrl": "[Lien direct vers un monument célèbre]"
            }
        """.trimIndent()
    }

    private fun parseCountryJson(jsonText: String): CountryData? {
        return try {
            val json = kotlinx.serialization.json.Json.parseToJsonElement(jsonText).jsonObject
            CountryData(
                name = json["name"]?.jsonPrimitive?.content ?: "",
                capital = json["capital"]?.jsonPrimitive?.content ?: "",
                language = json["language"]?.jsonPrimitive?.content ?: "",
                currency = json["currency"]?.jsonPrimitive?.content ?: "",
                population = json["population"]?.jsonPrimitive?.content ?: "",
                timezone = json["timezone"]?.jsonPrimitive?.content ?: "",
                flagUrl = json["flagUrl"]?.jsonPrimitive?.content ?: "",
                mapUrl = json["mapUrl"]?.jsonPrimitive?.content ?: "",
                description = json["description"]?.jsonPrimitive?.content ?: "",
                landmarkUrl = json["landmarkUrl"]?.jsonPrimitive?.content ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}
