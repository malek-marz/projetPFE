package com.example.testapp.features.chs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
class InvitationViewModel : ViewModel() {

    private val _invitedUsers = MutableStateFlow<List<UserDisplay>>(emptyList())
    val invitedUsers: StateFlow<List<UserDisplay>> = _invitedUsers

    private val db = FirebaseFirestore.getInstance()
    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    init {
        loadReceivedInvitations()
    }

    fun loadReceivedInvitations() {
        viewModelScope.launch {
            val result = mutableListOf<UserDisplay>()
            try {
                if (currentUid != null) {
                    Log.d("InvitationVM", "Current UID: $currentUid")

                    val invitationsSnapshot = db.collection("users")
                        .document(currentUid)
                        .collection("invitations_recues")
                        .get()
                        .await()

                    val inviterUids = invitationsSnapshot.documents.map { it.id }
                    Log.d("InvitationVM", "Fetched inviter UIDs: $inviterUids")

                    inviterUids.forEach { inviterUid ->
                        val userSnapshot = db.collection("users")
                            .document(inviterUid)
                            .get()
                            .await()

                        if (userSnapshot.exists()) {
                            val username = userSnapshot.getString("username") ?: "Unknown"
                            val email = userSnapshot.getString("email") ?: ""

                            Log.d("InvitationVM", "Fetched user - UID: $inviterUid, Username: $username, Email: $email")

                            result.add(
                                UserDisplay(
                                    uid = inviterUid,
                                    username = username,
                                    email = email
                                )
                            )
                        } else {
                            Log.w("InvitationVM", "User document does not exist for UID: $inviterUid")
                        }
                    }

                    _invitedUsers.value = result
                    Log.d("InvitationVM", "Total users loaded: ${result.size}")
                } else {
                    Log.e("InvitationVM", "Current UID is null")
                }
            } catch (e: Exception) {
                Log.e("InvitationVM", "Error loading invitations", e)
            }
        }
    }

    fun acceptInvitation(inviterUid: String) {
        viewModelScope.launch {
            try {
                if (currentUid == null) {
                    Log.e("InvitationVM", "Current UID is null")
                    return@launch
                }

                // Références Firestore
                val invitationRef = db.collection("users")
                    .document(currentUid)
                    .collection("invitations_recues")
                    .document(inviterUid)

                val friendsRef = db.collection("users")
                    .document(currentUid)
                    .collection("friends")
                    .document(inviterUid)

                // Supprimer l’invitation reçue dans user2
                invitationRef.delete().await()
                Log.d("InvitationVM", "Invitation from $inviterUid deleted from invitations_recues of current user")

                // Ajouter à friends sous currentUid (user2)
                friendsRef.set(mapOf("since" to System.currentTimeMillis())).await()
                Log.d("InvitationVM", "User $inviterUid added to friends of current user")

                // Ajouter currentUid dans friends de inviterUid (amitié bidirectionnelle)
                db.collection("users")
                    .document(inviterUid)
                    .collection("friends")
                    .document(currentUid)
                    .set(mapOf("since" to System.currentTimeMillis()))
                    .await()

                Log.d("InvitationVM", "Current user added to friends of $inviterUid")

                // --- Suppression de currentUid (user2) dans invitations_envoyees de inviterUid ---
                val sentInvitationRef = db.collection("users")
                    .document(inviterUid)
                    .collection("invitations_envoyees")
                    .document(currentUid)

                try {
                    val sentDocSnapshot = sentInvitationRef.get().await()
                    if (sentDocSnapshot.exists()) {
                        Log.d("InvitationVM", "Document to delete exists in invitations_envoyees for current user")
                        sentInvitationRef.delete().await()
                        Log.d("InvitationVM", "Removed current user from invitations_envoyees of $inviterUid")
                    } else {
                        Log.w("InvitationVM", "No document found in invitations_envoyees for current user under $inviterUid")
                    }
                } catch (e: Exception) {
                    Log.e("InvitationVM", "Error trying to delete invitations_envoyees doc", e)
                }

                // Rafraîchir la liste des invitations
                loadReceivedInvitations()

            } catch (e: Exception) {
                Log.e("InvitationVM", "Error accepting invitation", e)
            }
        }
    }



    fun rejectInvitation(inviterUid: String) {
        viewModelScope.launch {
            try {
                if (currentUid == null) {
                    Log.e("InvitationVM", "Current UID is null")
                    return@launch
                }

                val invitationRef = db.collection("users")
                    .document(currentUid)
                    .collection("invitations_recues")
                    .document(inviterUid)

                // Supprimer simplement l’invitation reçue
                invitationRef.delete().await()
                Log.d("InvitationVM", "Invitation from $inviterUid rejected and deleted")

                loadReceivedInvitations()

            } catch (e: Exception) {
                Log.e("InvitationVM", "Error rejecting invitation", e)
            }
        }
    }
}
