package com.example.testapp.features.homescreen

import androidx.lifecycle.ViewModel
import com.example.testapp.features.homescreen.model.homeScreenState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
class HomeScreenViewModel : ViewModel() {
        private val _state = MutableStateFlow(homeScreenState())
        val state: StateFlow<homeScreenState> = _state
        private val auth: FirebaseAuth = FirebaseAuth.getInstance()

}
