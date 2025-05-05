package com.example.journeybuddy.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val profilePicture: String = "",
    val interests: List<String> = emptyList()
)
//import androidx.lifecycle.viewModelScope

class ProfileScreenViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    fun getUserProfile(userId: String, onResult: (UserProfile?) -> Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userProfile = document.toObject<UserProfile>()
                onResult(userProfile)
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileScreen", "Error getting profile", exception)
                onResult(null)
            }
    }

    fun updateUserProfile(userId: String, updatedProfile: UserProfile, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("users")
            .document(userId)
            .set(updatedProfile)
            .addOnSuccessListener {
                Log.d("ProfileScreen", "Profile updated successfully")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileScreen", "Error updating profile", exception)
                onFailure("Error updating profile: ${exception.message}")
            }
    }
}
