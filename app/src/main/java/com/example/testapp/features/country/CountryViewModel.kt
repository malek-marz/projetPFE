package com.example.testapp.presentation.country

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.testapp.features.country.CountryData
import com.example.testapp.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.serialization.json.Json

class CountryViewModel : ViewModel() {

    private val apiKey = "AIzaSyBt-LuJCBIFp4_2Xrl92SQIoi0VZq5Qklk"

    private val _generatedText = MutableStateFlow<String?>(null)
    val generatedText: StateFlow<String?> = _generatedText

    // Pour infos pays
    private val _countryData = mutableStateOf<CountryData?>(null)
    val countryData: State<CountryData?> = _countryData

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun fetchCountryInfoBasedOnInterests(interests: List<String>) {
        _isLoading.value = true
        _errorMessage.value = null

        val prompt = buildPromptBasedOnInterests(interests)
        Log.d("CountryViewModel", "Prompt généré : $prompt")

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            )
        )

        RetrofitClient.geminiService.getCountryDetails(request, apiKey)
            .enqueue(object : Callback<GeminiResponse> {
                override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val contentText = response.body()
                            ?.candidates?.firstOrNull()
                            ?.content?.parts?.firstOrNull()?.text
                            ?.substringAfter("```json")
                            ?.substringBefore("```")
                            ?: ""

                        Log.d("CountryViewModel", "Réponse API: $contentText")

                        val country = parseCountryJson(contentText)

                        _countryData.value = country
                    } else {
                        _errorMessage.value = "Erreur lors de la récupération des données : ${response.message()}"
                        Log.e("CountryViewModel", "Erreur API : ${response.message()}, ${response.code()}, ${response.errorBody()}")
                    }
                }

                override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "Échec de la requête : ${t.message}"
                    Log.e("CountryViewModel", "Échec de la requête : ${t.message}")
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
              "flagUrl": "[Lien direct vers le drapeau use this site https://www.countryflags.com]",
              "mapUrl": "[https://fr.mappy.com/plan/pays/<code-pays>]
]
]",
              "description": "",
              "landmarkUrl": "[Lien direct vers un monument célèbre]"
            }
        """.trimIndent()
    }

    private fun parseCountryJson(jsonText: String): CountryData? {
        return try {
            val json = Json.parseToJsonElement(jsonText).jsonObject
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
            Log.e("CountryViewModel", "Erreur lors du parsing JSON: ${e.message}")
            null
        }
    }
}
val request = GeminiRequest(
    contents = listOf(
        Content(
            parts = listOf(Part(text = "Donne-moi des détails sur la France"))
        )
    )
)

