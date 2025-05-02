package com.example.testapp.repository

import com.example.testapp.models.FriendSuggestion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val firestore: FirebaseFirestore) {

    suspend fun getFriendSuggestions(userId: String): List<FriendSuggestion> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("friend_suggestions")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(FriendSuggestion::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addFriend(userId: String, friendEmail: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .collection("friends")
                .document(friendEmail)
                .set(mapOf("timestamp" to System.currentTimeMillis()))
                .await()

            firestore.collection("users")
                .document(userId)
                .collection("friend_suggestions")
                .document(friendEmail)
                .delete()
                .await()
        } catch (e: Exception) {
            throw Exception("Error adding friend: ${e.message}")
        }
    }
}
