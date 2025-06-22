package com.example.testapp.features.USER

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class userViewModel : ViewModel() {

    private val _selectedCriteria = MutableStateFlow(setOf<String>())
    val selectedCriteria: StateFlow<Set<String>> = _selectedCriteria

    private val _selectedLanguages = MutableStateFlow(setOf<String>())
    val selectedLanguages: StateFlow<Set<String>> = _selectedLanguages

    private val db = FirebaseFirestore.getInstance()

    fun toggleCriterion(criterion: String) {
        _selectedCriteria.value = _selectedCriteria.value.toMutableSet().also {
            if (!it.add(criterion)) it.remove(criterion)
        }
    }

    fun toggleLanguage(language: String) {
        _selectedLanguages.value = _selectedLanguages.value.toMutableSet().also {
            if (!it.add(language)) it.remove(language)
        }
    }

    fun saveCriteriaForUser(userId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val data = mapOf(
            "criteria" to _selectedCriteria.value.toList(),
            "selectedLanguages" to _selectedLanguages.value.toList()
        )

        db.collection("users")
            .document(userId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    fun loadCriteriaForUser(userId: String) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val criteriaList = document.get("criteria") as? List<String> ?: emptyList()
                    _selectedCriteria.value = criteriaList.toSet()

                    val langs = document.get("selectedLanguages") as? List<String> ?: emptyList()
                    _selectedLanguages.value = langs.toSet()
                }
            }
    }
}
