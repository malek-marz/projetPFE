package com.example.testapp.features.login

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.testapp.features.login.model.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun validateCredentials(): Boolean {
        return if (_state.value.email.isEmpty() || _state.value.password.isEmpty()) {
            _state.value = _state.value.copy(errorMessage = "Veuillez remplir tous les champs")
            false
        } else {
            _state.value = _state.value.copy(errorMessage = "")
            true
        }
    }
    fun login():Boolean{
        return if(validateCredentials()){
            //firebase
            true
        }
        else{

            false
        }
    }

}
