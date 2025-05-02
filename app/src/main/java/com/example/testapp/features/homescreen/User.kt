package com.example.testapp.features.homescreen

data class User(
    val email: String = "",
    val name: String = "",
    val profilePicture: String = "",
    val mutualFriends: List<String> = emptyList()
)
