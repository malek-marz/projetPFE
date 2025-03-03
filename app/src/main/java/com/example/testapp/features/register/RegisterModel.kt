package com.example.yourapp.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class RegisterModel(
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val birthday: String = "",
    val gender: String = "",
    val country: String = ""
) {
    fun register(onRegisterSuccess: () -> Unit, onRegisterFailed: (String) -> Unit) {
        if (!validateRegisterModel(this)) {
            onRegisterFailed("Veuillez vérifier les informations fournies.")
            return
        }

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // Création de l'utilisateur avec email et mot de passe
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Création d'un objet utilisateur à stocker dans Firestore
                    val user = mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "username" to username,
                        "email" to email,
                        "birthday" to birthday,
                        "gender" to gender,
                        "country" to country
                    )

                    // Ajout des informations dans Firestore
                    db.collection("users").document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            onRegisterSuccess()
                        }
                        .addOnFailureListener { e ->
                            onRegisterFailed("Échec de l'inscription : ${e.localizedMessage}")
                        }
                } else {
                    onRegisterFailed("Échec : ${task.exception?.message}")
                }
            }
    }
}

fun validateRegisterModel(model: RegisterModel): Boolean {
    return when {
        model.firstName.isBlank() || model.lastName.isBlank() || model.username.isBlank() ||
                model.email.isBlank() || model.password.isBlank() || model.confirmPassword.isBlank() ||
                model.birthday.isBlank() -> false

        model.password != model.confirmPassword -> false

        !android.util.Patterns.EMAIL_ADDRESS.matcher(model.email).matches() -> false

        model.password.length < 6 -> false

        !model.birthday.matches("^\\d{2}/\\d{2}/\\d{4}$".toRegex()) -> false

        else -> true
    }
}
