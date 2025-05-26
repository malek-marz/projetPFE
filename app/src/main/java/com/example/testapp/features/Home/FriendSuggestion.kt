package com.example.testapp.models
data class FriendSuggestion(
    val userId: String,
    val firstName: String,
    val lastName: String = "",
    val email: String,
    val username: String = "",
    val profilePictureUrl: String,
    val criteria: List<String> = emptyList(), // utilisé à la place de interests
    val country: String = "",
    val matchPercentage: Double = 0.0  // Nouveau champ ajouté avec valeur par défaut

)
