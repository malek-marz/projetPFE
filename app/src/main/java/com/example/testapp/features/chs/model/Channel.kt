package com.example.testapp.features.chs.model

data class Channel(
    val email: String = "",
    var username: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
