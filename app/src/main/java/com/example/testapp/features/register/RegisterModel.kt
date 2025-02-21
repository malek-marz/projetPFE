// RegisterModel.kt
package com.example.yourapp.model

data class RegisterModel(
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val birthday: String,
    val gender: String,
    val country: String
)

fun validateRegisterModel(model: RegisterModel): Boolean {
    // Validation des champs
    if (model.firstName.isBlank() || model.lastName.isBlank() || model.username.isBlank() ||
        model.email.isBlank() || model.password.isBlank() || model.confirmPassword.isBlank() ||
        model.birthday.isBlank()) {
        return false
    }

    // Vérification des mots de passe
    if (model.password != model.confirmPassword) {
        return false
    }


    // Vérification de l'email
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(model.email).matches()) {
        return false
    }

    // Vérification du format de la date (dd/mm/yyyy)
    val dateRegex = "^\\d{2}/\\d{2}/\\d{4}$".toRegex()
    if (!model.birthday.matches(dateRegex)) {
        return false
    }

    return true
}
