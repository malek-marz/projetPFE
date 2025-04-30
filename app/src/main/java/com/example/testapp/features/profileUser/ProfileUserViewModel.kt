package com.example.testapp.features.profileUser

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class User(
    val gmail: String = "",
    val name: String = "",
    val age: Int = 0,
    val criteria: List<String> = emptyList()
)

class ProfileUserViewModel : ViewModel() {
    private val _state = MutableStateFlow(User())
    val state: StateFlow<User> = _state

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun fetchUserProfile() {
        val currentUserEmail = auth.currentUser?.email

        if (currentUserEmail != null) {
            firestore.collection("users")
                .whereEqualTo("gmail", currentUserEmail)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val document = querySnapshot.documents.firstOrNull()
                    if (document != null) {
                        val user = User(
                            gmail = document.getString("gmail") ?: "",
                            name = document.getString("name") ?: "",
                            age = (document.getLong("age") ?: 0L).toInt(),
                            criteria = document.get("criteria") as? List<String> ?: emptyList()
                        )
                        _state.value = user
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }
}