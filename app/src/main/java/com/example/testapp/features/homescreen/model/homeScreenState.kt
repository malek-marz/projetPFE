package com.example.testapp.features.homescreen

sealed class HomeScreenState {
    object Loading : HomeScreenState()
    data class Success(val users: List<User>) : HomeScreenState()
    data class Error(val message: String) : HomeScreenState()
}
