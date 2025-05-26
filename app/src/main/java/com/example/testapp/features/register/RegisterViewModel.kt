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

    // Étape 1 : Création compte + envoi email de vérification
    fun register(
        onRegisterSuccess: () -> Unit,
        onRegisterFailed: (String) -> Unit
    ) {
        val currentState = state.value

        // Validation des données avant inscription
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
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { emailTask ->
                            if (emailTask.isSuccessful) {
                                Log.i("Register", "Email de vérification envoyé à $email")
                                // Ne pas enregistrer dans Firestore avant vérification email
                                onRegisterSuccess()
                            } else {
                                onRegisterFailed("Échec de l'envoi de l'email de vérification : ${emailTask.exception?.message}")
                            }
                        }
                } else {
                    onRegisterFailed(task.exception?.message ?: "Inscription échouée")
                }
            }
    }

    // Étape 2 : Après clic "J'ai vérifié mon email" — création document utilisateur Firestore
    fun createUserDocumentIfEmailVerified(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("Utilisateur non connecté")
            return
        }

        user.reload().addOnSuccessListener {
            if (user.isEmailVerified) {
                val userId = user.uid
                val userData = mapOf(
                    "firstName" to state.value.firstName,
                    "lastName" to state.value.lastName,
                    "username" to state.value.username,
                    "email" to state.value.email,
                    "birthday" to state.value.birthday,
                    "gender" to state.value.gender,
                    "country" to state.value.country
                )

                db.collection("users").document(userId)
                    .set(userData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e ->
                        onFailure("Erreur lors de l'enregistrement Firestore : ${e.localizedMessage}")
                    }
            } else {
                onFailure("Votre email n'a pas encore été vérifié.")
            }
        }.addOnFailureListener { e ->
            onFailure("Erreur lors de la vérification email : ${e.localizedMessage}")
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
