package com.example.yourapp.model

import androidx.lifecycle.ViewModel
import com.example.testapp.features.register.RegisterState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RegisterViewModel (): ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state

    fun register(onRegisterSuccess: () -> Unit, onRegisterFailed: (String) -> Unit) {
        if (!validateRegisterModel(state.value)) {
            onRegisterFailed("Veuillez vérifier les informations fournies.")
            return
        }

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // Création de l'utilisateur avec email et mot de passe
        auth.createUserWithEmailAndPassword(state.value.email, state.value.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Création d'un objet utilisateur à stocker dans Firestore
                    val user = mapOf(
                        "firstName" to state.value.firstName,
                        "lastName" to state.value.lastName,
                        "username" to state.value.username,
                        "email" to state.value.email,
                        "birthday" to state.value.birthday,
                        "gender" to state.value.gender,
                        "country" to state.value.country
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

    fun onFirstNameChanged(firstName: String) {
        _state.update { it.copy(firstName = firstName) }
    }

    fun onLastNameChanged(lastName: String) {
        _state.update { it.copy(lastName = lastName) }
    }

    fun onUsernameChanged(username: String) {
        _state.update { it.copy(username = username) }
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun onBirthdayChanged(birthday: String) {
        _state.update { it.copy(birthday = birthday) }
    }

    fun onGenderChanged(gender: String) {
        _state.update { it.copy(gender = gender) }
    }

    fun onCountryChanged(country: String) {
        _state.update { it.copy(country = country) }
    }
}

fun validateRegisterModel(model: RegisterState): Boolean {
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
