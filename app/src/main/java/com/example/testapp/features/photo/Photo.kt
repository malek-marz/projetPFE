package com.example.testapp.domain.model

// Représente une image dans l'API de Pexels
data class Photo(
    val id: Int,
    val width: Int,
    val height: Int,
    val url: String,  // L'URL de l'image
    val photographer: String, // Le nom du photographe
    val photographer_url: String, // L'URL du photographe
    val src: ImageSrc  // Les différentes résolutions de l'image
)

// Contient les différentes résolutions de l'image
data class ImageSrc(
    val original: String,  // URL de l'image originale
    val large: String,     // URL de l'image grande
    val medium: String     // URL de l'image moyenne
)

// Représente la réponse de l'API de Pexels
data class PhotoResponse(
    val photos: List<Photo>  // Liste de photos récupérées
)
