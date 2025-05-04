package com.example.testapp.features.chs

data class Channel(
    val id:String="",
    val email: String = "",
    var username: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
