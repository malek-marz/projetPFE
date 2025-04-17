package com.example.testapp.domain.model

data class CountryRequest(
    val model: String = "lmstudio",
    val messages: List<Message>,
    val temperature: Double = 0.1
) {
    data class Message(
        val role: String = "user",
        val content: String
    )

    companion object {
        fun create(intrests: String): CountryRequest {
            return CountryRequest(
                messages = listOf(Message(content ="Donne-moi un pays qui soutient le mieux ces intérêts : $intrests. Fournis son nom, sa langue, sa monnaie, sa population, son fuseau horaire, ainsi qu’une URL d’image du pays, au format JSON."))
            )
        }
    }
}
