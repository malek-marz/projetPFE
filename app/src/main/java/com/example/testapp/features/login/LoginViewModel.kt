package com.example.testapp.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.features.login.model.LoginState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
                    _state.update { it.copy(errorMessage = "Connexion réussie", isLoading = false) }
                    onLoginSuccess()  // Redirige après connexion réussie
                } else {
                    _state.update { it.copy(errorMessage = "Échec : ${task.exception?.message}", isLoading = false) }
                }
            }
    }
}
