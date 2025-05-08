package com.example.testapp.features.Buddys

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

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
                    val sexe = doc.getString("gender") ?: return@mapNotNull null
                    val age = doc.getString("birthday")?.extractAge() ?: return@mapNotNull null

                    val criteria = doc.get("criteria") as? List<String> ?: emptyList()

                    val sexeMatch = (selectedSex == "Peu importe" || sexe == selectedSex)
                    val ageMatch = age in selectedAges
                    val criteriaMatchCount = criteria.count { selectedCriteria.contains(it) }

                    if (sexeMatch && ageMatch && criteriaMatchCount >= 3) {
                        UserMatch(sexe = sexe, age = age, criteria = emptyList())
                    } else null
                }

                _state.value = buddyState(matches = compatible)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun String.extractAge(): Int? {
    // Expected format: "birthday:dd/MM/yyyy"
    val birthdayString = this.substringAfter("birthday:").trim()

    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val birthDate = sdf.parse(birthdayString) ?: return null

        val today = Calendar.getInstance()
        val dob = Calendar.getInstance().apply { time = birthDate }

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        // Adjust if birthday hasn't occurred yet this year
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        age
    } catch (e: Exception) {
        null
    }
}