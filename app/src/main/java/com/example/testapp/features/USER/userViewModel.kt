import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    fun loadCriteriaForUser(userId: String) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("criteria")) {
                    val criteriaList = document.get("criteria") as? List<String> ?: emptyList()
                    _selectedCriteria.value = criteriaList.toSet()
                }
            }
    }
}
