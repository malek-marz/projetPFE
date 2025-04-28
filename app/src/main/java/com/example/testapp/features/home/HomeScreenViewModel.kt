package com.example.journeybuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeScreenViewModel : ViewModel() {

    private val _friends = MutableStateFlow(
        listOf(
            Friend("Sophie", "https://randomuser.me/api/portraits/women/1.jpg"),
            Friend("Daniel", "https://randomuser.me/api/portraits/men/1.jpg"),
            Friend("Olivia", "https://randomuser.me/api/portraits/women/2.jpg"),
            Friend("Jacob", "https://randomuser.me/api/portraits/men/2.jpg")
        )
    )
    val friends: StateFlow<List<Friend>> = _friends

    data class Friend(
        val name: String,
        val photoUrl: String
    )
}
