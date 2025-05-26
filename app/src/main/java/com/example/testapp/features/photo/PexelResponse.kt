package com.example.journeybuddy.domain.model

data class PexelsResponse(
    val photos: List<PexelsPhoto>
)

data class PexelsPhoto(
    val id: Int,
    val src: PhotoSource
)

data class PhotoSource(
    val original: String // URL de l'image
)
