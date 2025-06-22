package com.example.testapp.repository

import android.util.Log
import com.example.testapp.models.FriendSuggestion
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
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
            Log.d(TAG, "📍 Début récupération des suggestions pour userId = $currentUserId")

            // ✅ 1. Récupérer les IDs bloqués et amis
            val blockedUserIds = getBlockedUserIds(currentUserId)
            Log.d(TAG, "✅ Utilisateurs bloqués : $blockedUserIds")

            val friendIds = getFriendIds(currentUserId)
            Log.d(TAG, "✅ Amis actuels : $friendIds")

            // ✅ 1.b Récupérer les invitations envoyées
            val sentInvitationIdsSnapshot = firestore.collection("users")
                .document(currentUserId)
                .collection("invitations_envoyees")
                .get()
                .await()
            val sentInvitationIds = sentInvitationIdsSnapshot.documents.map { it.id }
            Log.d(TAG, "✅ Invitations envoyées : $sentInvitationIds")

            // ✅ 1.c Récupérer les invitations reçues
            val receivedInvitationIdsSnapshot = firestore.collection("users")
                .document(currentUserId)
                .collection("invitations_recues")
                .get()
                .await()
            val receivedInvitationIds = receivedInvitationIdsSnapshot.documents.map { it.id }
            Log.d(TAG, "✅ Invitations reçues : $receivedInvitationIds")

            // ✅ 2. Critères utilisateur courant
            val currentUserSnapshot = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val currentUserCriteria = currentUserSnapshot.get("criteria") as? List<String> ?: emptyList()
            Log.d(TAG, "✅ Critères utilisateur courant : $currentUserCriteria")

            if (currentUserCriteria.isEmpty()) {
                Log.d(TAG, "⚠️ Utilisateur courant sans critères.")
                return emptyList()
            }

            // ✅ 3. Dernier pays visité utilisateur courant
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

            Log.d(TAG, "✅ Dernier pays visité utilisateur courant : $currentUserCountry")

            if (currentUserCountry.isNullOrEmpty()) {
                Log.d(TAG, "⚠️ Utilisateur courant sans pays visité.")
                return emptyList()
            }

            // ✅ 4. Récupérer tous les utilisateurs
            val allUsersSnapshot = firestore.collection("users").get().await()
            Log.d(TAG, "📄 Nombre total d'utilisateurs récupérés : ${allUsersSnapshot.documents.size}")

            val suggestions = mutableListOf<FriendSuggestion>()

            for (doc in allUsersSnapshot.documents) {
                val otherUserId = doc.id

                // ✅ Exclure soi-même, amis, bloqués, invitations envoyées et reçues
                if (
                    otherUserId == currentUserId ||
                    blockedUserIds.contains(otherUserId) ||
                    friendIds.contains(otherUserId) ||
                    sentInvitationIds.contains(otherUserId) ||
                    receivedInvitationIds.contains(otherUserId)
                ) {
                    Log.d(TAG, "⛔ Utilisateur ignoré (ami, bloqué, ou invitation envoyée/reçue) : $otherUserId")
                    continue
                }

                val otherCriteria = doc.get("criteria") as? List<String> ?: emptyList()
                if (otherCriteria.isEmpty()) {
                    Log.d(TAG, "⚠️ Utilisateur $otherUserId sans critères, ignoré.")
                    continue
                }

                // ✅ Dernier pays visité autre utilisateur
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

                if (otherCountry.isNullOrEmpty()) {
                    Log.d(TAG, "⚠️ Utilisateur $otherUserId sans pays visité, ignoré.")
                    continue
                }

                if (otherCountry != currentUserCountry) {
                    Log.d(TAG, "🌍 Pays différent pour $otherUserId : $otherCountry ≠ $currentUserCountry")
                    continue
                }

                // ✅ Pourcentage d'intersection des critères
                val intersection = currentUserCriteria.intersect(otherCriteria.toSet())
                val matchPercentage = (intersection.size.toDouble() / currentUserCriteria.size.toDouble()) * 100
                Log.d(TAG, "🔍 Match $otherUserId - ${intersection.size} critères communs - $matchPercentage%")

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
                    Log.d(TAG, "✅ Suggestion ajoutée pour $otherUserId")
                }
            }

            Log.d(TAG, "🎯 Suggestions finales : ${suggestions.size}")
            return suggestions
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors du calcul des suggestions criteria+country: ${e.message}", e)
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
    suspend fun getBlockedUserIds(userId: String): List<String> {
        val snapshot = Firebase.firestore.collection("users").document(userId).get().await()
        return snapshot.get("blocked") as? List<String> ?: emptyList()
    }
    suspend fun getFriendIds(userId: String): List<String> {
        val snapshot = Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("friends")
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.id }
    }


}
