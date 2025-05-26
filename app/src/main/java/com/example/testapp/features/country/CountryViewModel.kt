package com.example.testapp.presentation.country

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.testapp.features.country.CountryData
import com.example.testapp.network.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.serialization.json.Json

class CountryViewModel : ViewModel() {

    private val apiKey = "AIzaSyBt-LuJCBIFp4_2Xrl92SQIoi0VZq5Qklk"
    private val db = FirebaseFirestore.getInstance()

    private val _generatedText = MutableStateFlow<String?>(null)
    val generatedText: StateFlow<String?> = _generatedText

    private val _countryData = mutableStateOf<CountryData?>(null)
    val countryData: State<CountryData?> = _countryData

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _recommendedPlus = mutableStateOf<String?>(null)
    val recommendedPlus: State<String?> = _recommendedPlus

    fun fetchCountryInfoBasedOnInterests(Interests: List<String>) {
        Log.d("CountryViewModel", "Intérêts reçus dans fetchCountryInfoBasedOnInterests: $Interests")
        _isLoading.value = true
        _errorMessage.value = null

        val prompt = buildPromptBasedOnInterests(Interests)
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
                            ?.substringAfter("{")?.substringBeforeLast("}")?.let { "{$it}" }
                            ?: ""



                        val parsed = parseCountryJson(contentText)
                        _countryData.value = parsed

                        _recommendedPlus.value = response.body()
                            ?.candidates?.firstOrNull()
                            ?.content?.parts?.firstOrNull()?.text
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
              Merci de fournir :
- Une URL directe vers une image du drapeau officiel du pays au format **.png uniquement**
    - Une description courte du pays
    - Une URL vers une photo ou une page d’un monument ou lieu touristique célèbre du pays
            - Les images doivent être directement affichables dans une application Android avec Coil.
             **Ne retourne aucun lien en .svg ou .webp**.

    
    ⚠️ Ne choisis pas le même pays qu'auparavant, propose un **autre pays** différent avec les mêmes intérêts.


    Voici le format attendu :
            {
            
              "name": "",
              "capital": "",
              "language": "",
              "currency": "",
              "population": "",
              "timezone": "",
               "image": "Lien direct vers l’image connu de pays (ex. : ex. : https://images.unsplash.com/photo-1576485375217-d6a95e34d043?q=80&w=1974&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D)",
              "flagUrl": "Lien direct vers l’image du drapeau (ex. : https://flagcdn.com/fr.svg)",
              "mapUrl": "https://fr.mappy.com/plan/pays/[code-pays]",
              "description": "Une courte description touristique du pays",
              "landmarks": ["Eiffel Tower", "Louvre", "Notre-Dame"]
            }
        """.trimIndent()
    }

    private fun parseCountryJson(jsonText: String): CountryData? {
        return try {
            val json = Json.parseToJsonElement(jsonText).jsonObject

            val landmarksList = when (val landmarksElement = json["landmarks"]) {
                null -> emptyList()
                else -> {
                    if (landmarksElement is kotlinx.serialization.json.JsonArray) {
                        landmarksElement.map { it.jsonPrimitive.content }
                    } else {
                        // Si c’est une chaîne (JsonLiteral), on la split par virgule
                        landmarksElement.jsonPrimitive.content.split(",").map { it.trim() }
                    }
                }
            }

            CountryData(
                name = json["name"]?.jsonPrimitive?.content ?: "Inconnu",
                capital = json["capital"]?.jsonPrimitive?.content ?: "Inconnu",
                language = json["language"]?.jsonPrimitive?.content ?: "Inconnu",
                currency = json["currency"]?.jsonPrimitive?.content ?: "Inconnu",
                population = json["population"]?.jsonPrimitive?.content ?: "Inconnu",
                timezone = json["timezone"]?.jsonPrimitive?.content ?: "Inconnu",
                flagUrl = json["flagUrl"]?.jsonPrimitive?.content ?: "",
                mapUrl = json["mapUrl"]?.jsonPrimitive?.content ?: "",
                description = json["description"]?.jsonPrimitive?.content ?: "",
                image = json["image"]?.jsonPrimitive?.content ?: "",
                landmarks = landmarksList
            )
        } catch (e: Exception) {
            Log.e("CountryViewModel", "Erreur lors du parsing JSON: ${e.message}")
            null
        }
    }


    fun saveSelectedCountry(
        countryNameParam: String? = null,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = Firebase.firestore
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onFailure(Exception("Utilisateur non connecté"))
            return
        }

        val countryName = countryNameParam ?: _countryData.value?.name

        if (countryName.isNullOrBlank()) {
            onFailure(Exception("Le nom du pays est vide ou non défini."))
            return
        }

        val userSelectedCountryDocRef = db.collection("users")
            .document(user.uid)
            .collection("savedCountries")
            .document("selected_country")

        userSelectedCountryDocRef.get()
            .addOnSuccessListener { document ->
                val existingCountries = document.get("countries") as? List<Map<String, Any>> ?: emptyList()
                // Ajoute le nouveau pays avec timestamp à la liste existante
                val updatedCountries = existingCountries.toMutableList().apply {
                    add(
                        mapOf(
                            "name" to countryName,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                    )
                }
                // Met à jour le document avec la nouvelle liste
                userSelectedCountryDocRef.set(mapOf("countries" to updatedCountries))
                    .addOnSuccessListener {
                        Log.d("CountryViewModel", "Pays ajouté avec succès dans la liste : $countryName")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("CountryViewModel", "Erreur lors de la mise à jour : ${e.message}")
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CountryViewModel", "Erreur lors de la récupération du document : ${e.message}")
                onFailure(e)
            }
    }


    fun saveUserReview(
        reviewText: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onFailure(Exception("Utilisateur non connecté"))
            return
        }

        val reviewData = hashMapOf(
            "review" to reviewText,
            "timestamp" to System.currentTimeMillis(),
            "ownerName" to (user.displayName ?: user.email ?: "Utilisateur inconnu") // ✅ Ajouté ici
        )

        db.collection("users")
            .document(user.uid)
            .collection("user_reviews")
            .add(reviewData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }



    fun exploreMore(onComplete: () -> Unit = {}) {
        val currentInterests = extractInterestsFromLastPrompt()
        if (currentInterests.isEmpty()) {
            _errorMessage.value = "Aucun intérêt trouvé pour explorer plus."
            return
        }
        // Relancer la requête avec les mêmes intérêts
        fetchCountryInfoBasedOnInterests(currentInterests)
        onComplete()
    }

    private fun extractInterestsFromLastPrompt(): List<String> {
        // Extraction simple des intérêts depuis le dernier prompt généré
        val lastPrompt = _recommendedPlus.value ?: return emptyList()
        // On suppose que la chaîne contient quelque part "(interests1, interests2, ...)"
        val regex = """\((.*?)\)""".toRegex()
        val match = regex.find(lastPrompt)
        return match?.groups?.get(1)?.value
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()
    }

}


