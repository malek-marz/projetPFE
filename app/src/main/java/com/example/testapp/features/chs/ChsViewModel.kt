package com.example.testapp.features.chs

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Use StateFlow to manage the state of usernames
    private val _usernames = MutableStateFlow<List<String>>(emptyList())
    val usernames: StateFlow<List<String>> = _usernames

    init {
        fetchUsers()
    }

    private fun fetchUsers() {

        viewModelScope.launch {
            try {

                val result = withContext(Dispatchers.IO) {
                    db.collection("users").get().await()
                }

                // Extract usernames from the result
                val userList = mutableListOf<String>()
                for (document in result) {
                    val username = document.getString("username")
                    username?.let { userList.add(it) }
                }

                // Update the usernames state
                _usernames.value = userList

            } catch (exception: Exception) {
                Log.w("Firestore", "Error getting documents: ", exception)
            }
        }
    }
}
