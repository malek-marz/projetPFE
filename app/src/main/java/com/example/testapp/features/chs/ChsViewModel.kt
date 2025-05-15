package com.example.testapp.features.chs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _emails = MutableStateFlow<List<String>>(emptyList())
    val emails: StateFlow<List<String>> = _emails

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                val currentUserUid = auth.currentUser?.uid
                if (currentUserUid == null) {
                    Log.w("Firestore", "No authenticated user")
                    return@launch
                }

                val currentUserEmail = withContext(Dispatchers.IO) {
                    db.collection("users")
                        .document(currentUserUid)
                        .get()
                        .await()
                        .getString("email")
                }

                if (currentUserEmail == null) {
                    Log.w("Firestore", "Could not fetch current user's email")
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    db.collection("users")
                        .whereNotEqualTo("email", currentUserEmail)
                        .get()
                        .await()
                }

                val userEmails = result.documents.mapNotNull { it.getString("email") }
                _emails.value = userEmails

            } catch (e: Exception) {
                Log.w("Firestore", "Error getting users: ", e)
            }
        }
    }
}
