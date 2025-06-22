package com.example.testapp.features.profileUser

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class User(
    val email: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthday: String = "",
    val country: String = "",
    val gender: String = "",
    val criteria: List<String> = emptyList(),
    val profilePicUrl: String = "",
    val visitedCountries: List<String> = emptyList() // <-- 🔥 Ajout ici
)



class ProfileUserViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(User())
    val state: StateFlow<User> = _state

    fun fetchUserProfile() {
        val currentUserUid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(currentUserUid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val email = document.getString("email") ?: ""
                    val username = document.getString("username") ?: ""
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val birthday = document.getString("birthday") ?: ""
                    val country = document.getString("country") ?: ""
                    val gender = document.getString("gender") ?: ""
                    val criteria = document.get("criteria") as? List<String> ?: emptyList()
                    val profilePicUrl = document.getString("profilePicUrl") ?: ""

                    val baseUser = User(
                        email, username, firstName, lastName,
                        birthday, country, gender, criteria, profilePicUrl
                    )

                    // Fetch the list of visited countries
                    firestore.collection("users")
                        .document(currentUserUid)
                        .collection("savedCountries")
                        .document("selected_country")
                        .get()
                        .addOnSuccessListener { savedCountryDoc ->
                            val countriesList = savedCountryDoc.get("countries") as? List<Map<String, Any>> ?: emptyList()

                            val visitedCountryNames = countriesList.mapNotNull { it["name"] as? String }

                            _state.value = baseUser.copy(visitedCountries = visitedCountryNames)
                        }
                        .addOnFailureListener {
                            Log.e("Firestore", "Error fetching visited countries", it)
                            _state.value = baseUser
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching profile", exception)
            }
    }

    fun uploadProfilePicture(uri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/$uid.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    firestore.collection("users").document(uid)
                        .update("profilePicUrl", downloadUrl.toString())
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }
}