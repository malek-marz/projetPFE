import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.android.gms.tasks.Tasks

data class BlockedUser(
    val uid: String = "",
    val username: String = "",
    val profilePicUrl: String = ""
)

class BlockedUsersViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _blockedUsers = MutableStateFlow<List<BlockedUser>>(emptyList())
    val blockedUsers: StateFlow<List<BlockedUser>> = _blockedUsers

    fun loadBlockedUsers() {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUid)
            .get()
            .addOnSuccessListener { document ->
                val blockedList = document.get("blocked") as? List<String> ?: emptyList()
                android.util.Log.d("BlockedUsersVM", "Blocked list: $blockedList")

                if (blockedList.isEmpty()) {
                    _blockedUsers.value = emptyList()
                    return@addOnSuccessListener
                }

                val users = mutableListOf<BlockedUser>()
                val tasks = blockedList.map { uid ->
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                users.add(
                                    BlockedUser(
                                        uid = userDoc.id,
                                        username = userDoc.getString("username") ?: "Inconnu",
                                        profilePicUrl = userDoc.getString("profilePicUrl") ?: ""
                                    )
                                )
                            }
                        }
                }

                Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        _blockedUsers.value = users
                        android.util.Log.d("BlockedUsersVM", "Loaded blocked users: $users")
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("BlockedUsersVM", "Failed to load blocked list", e)
            }
    }

    fun unblockUser(blockedUid: String, onSuccess: () -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUid)
            .update("blocked", FieldValue.arrayRemove(blockedUid))
            .addOnSuccessListener {
                loadBlockedUsers()
                onSuccess()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("BlockedUsersVM", "Failed to unblock user", e)
            }
    }
}
