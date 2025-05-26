package com.example.journeybuddy.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val name: String = "",
    val interests: List<String> = emptyList(),
    val pays: String = ""
)

class ProfileScreenViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)



    fun updateUserInterestsOnly(
        userId: String,
        interests: List<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val update = mapOf("interests" to interests)
        db.collection("users").document(userId)
            .update(update)
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "Intérêts mis à jour : $interests")
                _userProfile.value = _userProfile.value?.copy(interests = interests)
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Erreur mise à jour intérêts", e)
                onFailure(e)
            }
    }
}
