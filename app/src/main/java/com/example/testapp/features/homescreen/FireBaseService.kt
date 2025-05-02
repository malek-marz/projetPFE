package com.example.testapp.services

import android.util.Log
import com.example.testapp.models.FriendSuggestion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseService @Inject constructor() {
    private val db: FirebaseFirestore = Firebase.firestore

    private val TAG = "FirebaseService" // For logging errors

    // Fetch friend suggestions for a user
    suspend fun getFriendSuggestions(userId: String): List<FriendSuggestion> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("friend_suggestions")
                .limit(10)
                .get()
                .await()

            // Mapping documents to FriendSuggestion objects
            snapshot.documents.mapNotNull { doc ->
                // Retrieve email from the document
                val email = doc.getString("email") ?: ""

                // Handle potential nulls or invalid data
                doc.toObject(FriendSuggestion::class.java)?.copy(email = email)
                    ?: run {
                        Log.e(TAG, "Invalid FriendSuggestion data for document: ${doc.id}")
                        null
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching friend suggestions: ${e.message}")
            emptyList() // Return an empty list on failure
        }
    }

    // Add a friend to the user's friend list and remove from suggestions
    suspend fun addFriend(userId: String, friendId: String) {
        try {
            // Adding to the user's friend list
            db.collection("users")
                .document(userId)
                .collection("friends")
                .document(friendId)
                .set(mapOf("timestamp" to System.currentTimeMillis()))
                .await()

            // Removing from the friend's suggestions
            db.collection("users")
                .document(userId)
                .collection("friend_suggestions")
                .document(friendId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding friend: ${e.message}")
            throw Exception("Failed to add friend: ${e.message}") // Throw with more context
        }
    }
}
