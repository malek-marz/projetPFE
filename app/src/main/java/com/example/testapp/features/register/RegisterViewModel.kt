package com.example.yourapp.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.testapp.features.register.RegisterState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state

    // Étape unique : Création compte + enregistrement Firestore + envoi email
    fun register(
        onRegisterSuccess: () -> Unit,
        onRegisterFailed: (String) -> Unit
    ) {
        val currentState = state.value

        // Validation des données
        if (!validateRegisterModel(currentState)) {
            onRegisterFailed("Formulaire invalide : vérifiez les champs")
            return
        }

        val email = currentState.email
        val password = currentState.password

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: return@addOnCompleteListener

                    // Création des données Firestore immédiatement
                    val userData = mapOf(
                        "firstName" to currentState.firstName,
                        "lastName" to currentState.lastName,
                        "username" to currentState.username,
                        "email" to currentState.email,
                        "birthday" to currentState.birthday,
                        "gender" to currentState.gender,
                        "country" to currentState.country
                    )

                    db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            // Envoi de l'email de vérification
                            user.sendEmailVerification()
                                .addOnSuccessListener {
                                    Log.i("Register", "Email de vérification envoyé à $email")
                                    onRegisterSuccess()
                                }
                                .addOnFailureListener { e ->
                                    onRegisterFailed("Email non envoyé : ${e.localizedMessage}")
                                }
                        }
                        .addOnFailureListener { e ->
                            onRegisterFailed("Erreur Firestore : ${e.localizedMessage}")
                        }

                } else {
                    onRegisterFailed(task.exception?.message ?: "Inscription échouée")
                }
            }
    }

    // === Mise à jour de l'état (copy) ===
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

// === Validation ===
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
