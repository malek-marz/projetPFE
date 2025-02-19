package com.example.testapp.features.login.model

import androidx.compose.ui.graphics.Color

data class LoginState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val errorMessage: String = "",


    )

