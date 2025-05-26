package com.example.testapp.features.chat

data class Message(
    val gmail: String = "",
    val sendergmail: String = "",
    val messageText: String? = null,
    val createdAt: Long = 0L,
    val senderName: String = ""
)
