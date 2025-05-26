package com.example.testapp.repository

import android.util.Log
import com.example.testapp.models.FriendSuggestion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val firestore: FirebaseFirestore) {
    private val TAG = "UserRepository"

    suspend fun getUserIdByEmail(email: String): String? {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents[0].id
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur getUserIdByEmail: ${e.message}", e)
            null
        }
    }

    suspend fun getFriendSuggestionsByCriteriaOnly(
        currentUserId: String,
        thresholdPercent: Double = 10.0
    ): List<FriendSuggestion> {
        return try {
            Log.d(TAG, "Début récupération des suggestions")

            // 1. Critères de l'utilisateur courant
            val currentUserSnapshot = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()
            val currentUserCriteria = currentUserSnapshot.get("criteria") as? List<String> ?: emptyList()
            if (currentUserCriteria.isEmpty()) {
                Log.d(TAG, "Utilisateur courant sans critères")
                return emptyList()
            }

            // 2. Pays courant (lecture du dernier pays sauvegardé dans la liste "countries")
            val currentUserCountryDoc = firestore.collection("users")
                .document(currentUserId)
                .collection("savedCountries")
                .document("selected_country")
                .get()
                .await()

            val currentUserCountriesList = currentUserCountryDoc.get("countries") as? List<Map<String, Any>>

            val currentUserCountry = currentUserCountriesList
                ?.maxByOrNull { (it["timestamp"] as? com.google.firebase.Timestamp) ?: com.google.firebase.Timestamp.now() }
                ?.get("name") as? String

            if (currentUserCountry.isNullOrEmpty()) {
                Log.d(TAG, "Utilisateur courant sans pays")
                return emptyList()
            }

            Log.d(TAG, "Pays utilisateur courant : $currentUserCountry")

            // 3. Tous les utilisateurs
            val allUsersSnapshot = firestore.collection("users").get().await()
            val suggestions = mutableListOf<FriendSuggestion>()

            for (doc in allUsersSnapshot.documents) {
                val otherUserId = doc.id
                if (otherUserId == currentUserId) continue

                val otherCriteria = doc.get("criteria") as? List<String> ?: emptyList()
                if (otherCriteria.isEmpty()) {
                    Log.d(TAG, "Utilisateur $otherUserId ignoré : aucun critère")
                    continue
                }

                // 4. Pays autre utilisateur (lecture du dernier pays sauvegardé dans la liste "countries")
                val otherCountryDoc = firestore.collection("users")
                    .document(otherUserId)
                    .collection("savedCountries")
                    .document("selected_country")
                    .get()
                    .await()

                val otherCountriesList = otherCountryDoc.get("countries") as? List<Map<String, Any>>

                val otherCountry = otherCountriesList
                    ?.maxByOrNull { (it["timestamp"] as? com.google.firebase.Timestamp) ?: com.google.firebase.Timestamp.now() }
                    ?.get("name") as? String

                Log.d(TAG, "Pays utilisateur $otherUserId : $otherCountry")
                if (otherCountry.isNullOrEmpty()) {
                    Log.d(TAG, "Utilisateur $otherUserId ignoré : aucun pays")
                    continue
                }

                // 5. Vérifie pays en commun
                if (currentUserCountry != otherCountry) {
                    Log.d(TAG, "Utilisateur $otherUserId ignoré : pas de pays en commun")
                    continue
                }

                // 6. Pourcentage d'intersection critères
                val intersection = currentUserCriteria.intersect(otherCriteria.toSet())
                val matchPercentage = (intersection.size.toDouble() / currentUserCriteria.size.toDouble()) * 100

                Log.d(TAG, "Utilisateur $otherUserId : % match = $matchPercentage")

                if (matchPercentage >= thresholdPercent) {
                    val suggestion = FriendSuggestion(
                        userId = otherUserId,
                        firstName = doc.getString("firstName") ?: "",
                        lastName = doc.getString("lastName") ?: "",
                        email = doc.getString("email") ?: "",
                        profilePictureUrl = doc.getString("profilePicture") ?: "",
                        criteria = otherCriteria,
                        matchPercentage = matchPercentage
                    )
                    suggestions.add(suggestion)
                }
            }

            Log.d(TAG, "Suggestions finales: ${suggestions.size}")
            return suggestions
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du calcul des suggestions criteria+country: ${e.message}", e)
            return emptyList()
        }
    }




    suspend fun addFriend(currentUserId: String, friendEmail: String) {
        try {
            val friendQuery = firestore.collection("users")
                .whereEqualTo("email", friendEmail)
                .get()
                .await()

            if (friendQuery.documents.isEmpty()) {
                Log.e(TAG, "Utilisateur avec email $friendEmail non trouvé")
                return
            }

            val friendDoc = friendQuery.documents[0]
            val friendId = friendDoc.id

            val userFriendsRef = firestore.collection("users")
                .document(currentUserId)
                .collection("friends")

            userFriendsRef.document(friendId).set(mapOf("friendId" to friendId)).await()

            Log.d(TAG, "Ami ajouté avec succès : $friendEmail")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'ajout d'ami: ${e.message}", e)
            throw e
        }
    }
}
