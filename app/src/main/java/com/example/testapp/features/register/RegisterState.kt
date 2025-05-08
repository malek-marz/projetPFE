package com.example.testapp.features.register

data class RegisterState(
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val birthday: String = "",
    var gender: String = "",
    var country: String = "",
    val emailVerified: Boolean = false
)
