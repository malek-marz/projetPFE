package com.example.testapp.features.Buddys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserMatch(
    val sexe: String = "",
    val age: Int = 0,
    val criteria: List<String> = emptyList()
)

data class buddyState(
    val matches: List<UserMatch> = emptyList()
)

class buddyViewModel : ViewModel() {
    private val _state = MutableStateFlow(buddyState())
    val state: StateFlow<buddyState> = _state

    private val firestore = FirebaseFirestore.getInstance()

    fun findCompatibleUsers(
        selectedSex: String,
        selectedAges: List<Int>,
        selectedCriteria: Set<String>
    ) {
        viewModelScope.launch {
            try {
                val result = firestore.collection("users").get().await()

                val compatible = result.documents.mapNotNull { doc ->
                    val sexe = doc.getString("sexe") ?: return@mapNotNull null
                    val age = doc.getLong("age")?.toInt() ?: return@mapNotNull null
                    val criteria = doc.get("criteria") as? List<String> ?: emptyList()

                    val sexeMatch = (selectedSex == "Peu importe" || sexe == selectedSex)
                    val ageMatch = age in selectedAges
                    val criteriaMatchCount = criteria.count { selectedCriteria.contains(it) }

                    if (sexeMatch && ageMatch && criteriaMatchCount >= 3) {
                        UserMatch(sexe = sexe, age = age, criteria = criteria)
                    } else null
                }

                _state.value = buddyState(matches = compatible)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
