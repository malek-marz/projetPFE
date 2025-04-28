package com.example.testapp.network

import com.example.testapp.presentation.country.Country // Assurez-vous que ce modèle existe
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Définir l'interface GeminiService pour l'API
interface GeminiService {
    @POST("your-api-endpoint") // Remplacez par le bon endpoint
    suspend fun generateContent(@Body request: GeminiRequest): GeminiResponse
}

// Classe de la requête envoyée
data class GeminiRequest(
    val contents: List<Content> // Liste de contenu
) {
    data class Content(
        val parts: List<Part> // Liste de parties dans chaque contenu
    )

    data class Part(
        val text: String // Texte d'une partie
    )
}

// Classe de la réponse de l'API
data class GeminiResponse(
    val candidates: List<Candidate> // Liste de candidats
) {
    data class Candidate(
        val content: Content // Contenu du candidat
    )

    data class Content(
        val parts: List<Part> // Liste des parties du contenu
    )

    data class Part(
        val text: String // Texte d'une partie
    )
}

// Client pour l'appel API
object GeminiClient {

    // Configuration de Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/") // URL de base de l'API
        .addConverterFactory(GsonConverterFactory.create()) // Convertisseur JSON
        .build()

    // Création du service Retrofit
    private val service = retrofit.create(GeminiService::class.java)

    // Fonction suspendue pour obtenir les détails d'un pays à partir de l'API
    suspend fun getCountryDetails(prompt: String): Country {
        // Création de la requête
        val request = GeminiRequest(
            contents = listOf(
                GeminiRequest.Content(
                    parts = listOf(GeminiRequest.Part(text = prompt))
                )
            )
        )

        // Appel API
        val response = service.generateContent(request)

        // Récupération du texte depuis la première réponse du candidat
        val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()

        // Transformer le texte récupéré en un objet Country
        return parseCountryFromText(text)
    }

    // Fonction pour transformer le texte en un objet Country
    private fun parseCountryFromText(text: String): Country {
        // Adapter la logique de parsing selon la structure de la réponse
        return Country(
            name = text, // Exemple, vous pouvez ajuster selon les données
            capital = "",
            language = "",
            currency = "",
            population = "",
            timezone = "",
            flagUrl = "",
            mapUrl = "",
            description = "",
            landmarkUrl = ""
        )
    }
}
