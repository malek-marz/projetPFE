package com.example.testapp.features.login.model

data class LoginState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false // Ajout du champ isLoading
)