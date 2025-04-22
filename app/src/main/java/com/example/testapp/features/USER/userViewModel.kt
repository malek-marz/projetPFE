// fichier : userViewModel.kt
package com.example.testapp.features.USER

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.FirebaseFirestore

class userViewModel : ViewModel() {
    private val _selectedCriteria = MutableStateFlow(setOf<String>())
    val selectedCriteria: StateFlow<Set<String>> = _selectedCriteria

    private val db = FirebaseFirestore.getInstance()

    fun toggleCriterion(criterion: String) {
        _selectedCriteria.value = _selectedCriteria.value.toMutableSet().also {
            if (!it.add(criterion)) it.remove(criterion)
        }
    }

    fun saveCriteriaForUser(userId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val data = mapOf("criteria" to _selectedCriteria.value.toList())

        db.collection("users")
            .document(userId)
            .update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }
}
