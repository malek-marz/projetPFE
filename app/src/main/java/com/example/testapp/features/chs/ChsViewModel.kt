package com.example.testapp.features.chs

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow

import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.asStateFlow
import com.example.testapp.features.chs.Channel
import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore

class ChsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    var usernames by mutableStateOf(listOf<String>())
        private set

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val userList = mutableListOf<String>()
                for (document in result) {
                    val username = document.getString("username")
                    username?.let { userList.add(it) }
                }
                usernames = userList
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }
}