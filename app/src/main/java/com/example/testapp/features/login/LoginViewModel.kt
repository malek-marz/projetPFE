package com.example.testapp.features.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.features.login.model.LoginState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class LoginViewModel : ViewModel() {
    private val TAG = "LoginViewModel"
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun updateEmail(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    private fun validateCredentials(): Boolean {
        return if (_state.value.email.isEmpty() || _state.value.password.isEmpty()) {
            _state.update { it.copy(errorMessage = "Veuillez remplir tous les champs") }
            false
        } else {
            _state.update { it.copy(errorMessage = "") }
            true
        }
    }

    fun login(onLoginSuccess: () -> Unit) {
        if (!validateCredentials()) return

        _state.update { it.copy(isLoading = true) }

        val email = _state.value.email
        val password = _state.value.password

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userUid = auth.currentUser?.uid
                    if (userUid == null) {
                        _state.update {
                            it.copy(
                                errorMessage = "Erreur inconnue : utilisateur non trouvé",
                                isLoading = false
                            )
                        }
                        return@addOnCompleteListener
                    }

                    // Check if user is banned
                    checkUserBanStatus(userUid, onLoginSuccess)

                } else {
                    _state.update {
                        it.copy(
                            errorMessage = "Échec : ${task.exception?.message}",
                            isLoading = false
                        )
                    }
                }
            }
    }

    private fun checkUserBanStatus(userUid: String, onLoginSuccess: () -> Unit) {
        db.collection("users").document(userUid).get()
            .addOnSuccessListener { document ->
                val banned = document.getBoolean("banned") ?: false
                val banExpiresAtMillis = document.getLong("banExpiresAt") ?: 0L
                val nowMillis = Date().time

                if (banned && banExpiresAtMillis > nowMillis) {
                    // User is currently banned
                    val remainingMillis = banExpiresAtMillis - nowMillis
                    val remainingHours = remainingMillis / (1000 * 60 * 60)

                    _state.update {
                        it.copy(
                            errorMessage = "Vous êtes temporairement bloqué. Réessayez dans $remainingHours heures.",
                            isLoading = false
                        )
                    }
                    auth.signOut()  // Prevent access by signing out
                } else {
                    // Ban expired or no ban, clear ban flags if necessary
                    if (banned && banExpiresAtMillis <= nowMillis) {
                        db.collection("users").document(userUid)
                            .update("banned", false, "banExpiresAt", 0L)
                            .addOnSuccessListener {
                                Log.d(TAG, "Ban expired, flags reset for user $userUid")
                            }
                    }
                    _state.update {
                        it.copy(
                            errorMessage = "Connexion réussie",
                            isLoading = false
                        )
                    }
                    onLoginSuccess()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to check ban status for user $userUid", e)
                _state.update {
                    it.copy(
                        errorMessage = "Erreur lors de la vérification du statut de l'utilisateur",
                        isLoading = false
                    )
                }
                auth.signOut()  // Sign out to be safe
            }
    }
}
