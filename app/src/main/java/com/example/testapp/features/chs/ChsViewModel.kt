package com.example.testapp.features.chs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.features.chat.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ChsViewModel : ViewModel() {

    private val dbFirestore = FirebaseFirestore.getInstance()
    private val dbRealtime = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private val _users = MutableStateFlow<List<UserDisplay>>(emptyList())
    val users: StateFlow<List<UserDisplay>> = _users

    private val _chatPreviews = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chatPreviews: StateFlow<List<ChatPreview>> = _chatPreviews

    init {
        fetchUsers()
    }

    private fun sanitize(input: String): String {
        return input.replace(".", "_")
    }

    private fun getChatId(email1: String, email2: String): String {
        val safe1 = sanitize(email1.trim().lowercase(Locale.getDefault()))
        val safe2 = sanitize(email2.trim().lowercase(Locale.getDefault()))
        return if (safe1 < safe2) "$safe1-$safe2" else "$safe2-$safe1"
    }

    private fun formatTimestampToHourMinute(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                val currentUserUid = auth.currentUser?.uid
                if (currentUserUid == null) {
                    Log.w("Firestore", "No authenticated user")
                    return@launch
                }

                val friendsSnapshot = withContext(Dispatchers.IO) {
                    dbFirestore.collection("users")
                        .document(currentUserUid)
                        .collection("friends")
                        .get()
                        .await()
                }
                val friendUids = friendsSnapshot.documents.map { it.id }
                Log.d("ChsViewModel", "Friends UIDs: $friendUids")

                if (friendUids.isEmpty()) {
                    _users.value = emptyList()
                    return@launch
                }

                val blockedUsers = withContext(Dispatchers.IO) {
                    dbFirestore.collection("users")
                        .document(currentUserUid)
                        .get()
                        .await()
                        .get("blocked") as? List<String> ?: emptyList()
                }

                val usersList = mutableListOf<UserDisplay>()
                val chunkSize = 10
                friendUids.chunked(chunkSize).forEach { chunk ->
                    val querySnapshot = dbFirestore.collection("users")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .await()

                    querySnapshot.documents.forEach { doc ->
                        val uid = doc.id
                        val email = doc.getString("email")
                        val username = doc.getString("username")
                        if (email != null && username != null && uid !in blockedUsers) {
                            usersList.add(UserDisplay(uid = uid, username = username, email = email))
                        }
                    }
                }

                _users.value = usersList

                val currentUserEmail = withContext(Dispatchers.IO) {
                    dbFirestore.collection("users")
                        .document(currentUserUid)
                        .get()
                        .await()
                        .getString("email")
                }
                if (currentUserEmail != null) {
                    fetchChatPreviews(currentUserEmail, usersList)
                }

            } catch (e: Exception) {
                Log.w("Firestore", "Error getting friends users: ", e)
            }
        }
    }

    private fun fetchChatPreviews(currentUserEmail: String, userList: List<UserDisplay>) {
        viewModelScope.launch {
            val chatPreviewsList = mutableListOf<ChatPreview>()
            try {
                for (user in userList) {
                    val chatId = getChatId(currentUserEmail, user.email)
                    val lastMessageSnapshot = withContext(Dispatchers.IO) {
                        dbRealtime.child("messages").child(chatId)
                            .orderByChild("createdAt")
                            .limitToLast(1)
                            .get()
                            .await()
                    }

                    val lastMessageObj = lastMessageSnapshot.children.firstOrNull()?.getValue(
                        Message::class.java)
                    val lastMessageText = lastMessageObj?.messageText ?: ""
                    val lastMessageTime = if (lastMessageObj != null) {
                        formatTimestampToHourMinute(lastMessageObj.createdAt)
                    } else ""
                    Log.d("ChatPreview", "Fetched for ${user.username}: message='$lastMessageText' at '$lastMessageTime'")

                    chatPreviewsList.add(
                        ChatPreview(
                            username = user.username,
                            email = user.email,
                            lastMessage = lastMessageText,
                            lastMessageTime = lastMessageTime
                        )
                    )
                }
                _chatPreviews.value = chatPreviewsList
                Log.d("ChatPreview", "Total chat previews fetched: ${chatPreviewsList.size}")
            } catch (e: Exception) {
                Log.e("ChsViewModel", "Error fetching chat previews", e)
            }
        }
    }
}
