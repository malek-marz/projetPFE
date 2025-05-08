package com.example.testapp.features.profileUser

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class User(
    val email: String = "",
    val username: String = "",
    val birthday: String = "",
    val country: String = "",
    val gender: String = "",
    val criteria: List<String> = emptyList() // 👈 Ajout de critères
)

class ProfileUserViewModel : ViewModel() {
    private val _state = MutableStateFlow(User())
    val state: StateFlow<User> = _state

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun fetchUserProfile() {
        val currentUserUid = auth.currentUser?.uid

        if (currentUserUid != null) {
            firestore.collection("users").document(currentUserUid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val email = document.getString("email") ?: ""
                        val username = document.getString("username") ?: ""
                        val birthday = document.getString("birthday") ?: ""
                        val country = document.getString("country") ?: ""
                        val gender = document.getString("gender") ?: ""
                        val criteria = document.get("criteria") as? List<String> ?: emptyList()

                        _state.value = User(email, username, birthday, country, gender, criteria)
                    } else {
                        Log.w("Firestore", "User document does not exist.")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error fetching user profile", exception)
                }
        } else {
            Log.w("Auth", "User not authenticated")
        }
    }
}
