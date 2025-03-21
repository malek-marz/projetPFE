package com.example.testapp.features.homescreen.model

import androidx.lifecycle.ViewModel
import com.example.testapp.features.login.model.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
class HomeScreenViewModel : ViewModel() { // Class name fixed
        private val _state = MutableStateFlow(LoginState())
        val state: StateFlow<LoginState> = _state
}
