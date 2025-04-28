package com.example.yourapp.model

import androidx.lifecycle.ViewModel
import com.example.testapp.features.register.RegisterState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RegisterViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun register(onRegisterSuccess: () -> Unit, onRegisterFailed: (String) -> Unit) {
        val currentState = state.value

        if (!validateRegisterModel(currentState)) {
            onRegisterFailed("Veuillez vérifier les informations fournies.")
            return
        }

        auth.createUserWithEmailAndPassword(currentState.email, currentState.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId == null) {
                        onRegisterFailed("Erreur : Impossible de récupérer l'ID utilisateur.")
                        return@addOnCompleteListener
                    }

                    val user = mapOf(
                        "uid" to userId,
                        "firstName" to currentState.firstName,
                        "lastName" to currentState.lastName,
                        "username" to currentState.username,
                        "email" to currentState.email,
                        "birthday" to currentState.birthday,
                        "gender" to currentState.gender,
                        "country" to currentState.country,
                        "createdAt" to System.currentTimeMillis()
                    )

                    db.collection("users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            onRegisterSuccess()
                        }
                        .addOnFailureListener { e ->
                            onRegisterFailed("Erreur lors de l'enregistrement Firestore: ${e.localizedMessage}")
                        }
                } else {
                    onRegisterFailed("Erreur d'inscription: ${task.exception?.localizedMessage}")
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
                model.birthday.isBlank() || model.gender.isBlank() || model.country.isBlank() -> false

        model.password != model.confirmPassword -> false

        !android.util.Patterns.EMAIL_ADDRESS.matcher(model.email).matches() -> false

        model.password.length < 6 -> false

        !model.birthday.matches("^\\d{2}/\\d{2}/\\d{4}$".toRegex()) -> false

        else -> true
    }
}
