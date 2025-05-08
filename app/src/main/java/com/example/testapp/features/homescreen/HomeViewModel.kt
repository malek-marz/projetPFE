package com.example.testapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.models.FriendSuggestion
import com.example.testapp.repository.UserRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

        private val repository = UserRepository(Firebase.firestore)

        private val _friendSuggestions = MutableStateFlow<List<FriendSuggestion>>(emptyList())
        val friendSuggestions: StateFlow<List<FriendSuggestion>> = _friendSuggestions

        private val _isLoading = MutableStateFlow(true)
        val isLoading: StateFlow<Boolean> = _isLoading

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage

        fun loadFriendSuggestions(userId: String) {
                _isLoading.value = true
                viewModelScope.launch {
                        try {
                                val suggestions = repository.getFriendSuggestions(userId)
                                _friendSuggestions.value = suggestions
                        } catch (e: Exception) {
                                _errorMessage.value = "Failed to load suggestions: ${e.message}"
                        } finally {
                                _isLoading.value = false
                        }
                }
        }

        fun addFriend(currentUserId: String, friendEmail: String) {
                viewModelScope.launch {
                        try {
                                repository.addFriend(currentUserId, friendEmail)
                                loadFriendSuggestions(currentUserId) // Refresh after adding
                        } catch (e: Exception) {
                                _errorMessage.value = "Failed to add friend: ${e.message}"
                        }
                }
        }

        fun clearErrorMessage() {
                _errorMessage.value = null
        }
}
