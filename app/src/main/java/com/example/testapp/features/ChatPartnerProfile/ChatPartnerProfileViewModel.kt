package com.example.testapp.features.chatPartnerProfile

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ChatPartnerUser(
    val email: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthday: String = "",
    val country: String = "",
    val gender: String = "",
    val criteria: List<String> = emptyList(),
    val profilePicUrl: String = "",
    val savedCountryName: String = "" // <-- 🔥 Ajout ici
)



class ChatPartnerProfileViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChatPartnerUser())
    val state: StateFlow<ChatPartnerUser> = _state

    private val firestore = FirebaseFirestore.getInstance()

    fun fetchUserProfileByUsername(username: String) {
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val uid = doc.id

                    val user = ChatPartnerUser(
                        email = doc.getString("email") ?: "",
                        username = doc.getString("username") ?: "",
                        firstName = doc.getString("firstName") ?: "",
                        lastName = doc.getString("lastName") ?: "",
                        birthday = doc.getString("birthday") ?: "",
                        country = doc.getString("country") ?: "",
                        gender = doc.getString("gender") ?: "",
                        criteria = doc.get("criteria") as? List<String> ?: emptyList(),
                        profilePicUrl = doc.getString("profilePicUrl") ?: ""
                    )

                    // 🔥 Structure exacte : users/{uid}/savedCountries/selected_country
                    firestore.collection("users")
                        .document(uid)
                        .collection("savedCountries")
                        .document("selected_country")
                        .get()
                        .addOnSuccessListener { savedDoc ->
                            val savedCountryName = savedDoc.getString("name") ?: ""
                            _state.value = user.copy(savedCountryName = savedCountryName)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Erreur pays sauvegardé", e)
                            _state.value = user
                        }
                } else {
                    Log.w("Firestore", "Aucun utilisateur trouvé avec ce username : $username")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erreur lors de la récupération du profil", e)
            }
    }
}
