package com.example.testapp.features.register

data class RegisterState(
    val firstName: String = "",
    val lastName: String = "",
    var username: String = "",
    var email: String = "",
    var password: String = "",
    val confirmPassword: String = "",
    val birthday: String = "",
    var gender: String = "Male",
    var country: String = "Tunisia",
    val isGenderDropdownExpanded: Boolean = false,
    val isCountryDropdownExpanded: Boolean = false
)