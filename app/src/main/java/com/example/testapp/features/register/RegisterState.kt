package com.example.testapp.features.register

data class RegisterState(
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val birthday: String = "",
    val selectedGender: String = "Male",
    val selectedCountry: String = "Tunisia",
    val isGenderDropdownExpanded: Boolean = false,
    val isCountryDropdownExpanded: Boolean = false
)
