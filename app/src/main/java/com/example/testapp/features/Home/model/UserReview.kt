package com.example.testapp.features.Home.model


data class UserReview(
    val ownerName: String = "",
    val review: String = "",
    val rating: Int = 0,
    val timestamp: Long = 0L
)


