package com.example.testapp.features.chat

data class Message(
    val gmail: String = "",
    val sendergmail: String = "",
    val message: String? = "",
    val createdAt: Long = System.currentTimeMillis(),
    val senderName: String = "",

    val messageText: String? = null,
    )
