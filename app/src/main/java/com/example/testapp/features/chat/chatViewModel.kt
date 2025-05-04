package com.codewithfk.chatter.feature.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.testapp.R
import com.example.testapp.features.chat.Message
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val db = Firebase.database

    fun sendMessage(context: Context, username: String, messageText: String?) {
        val message = Message(
            gmail = Firebase.auth.currentUser?.email ?: "",
            sendergmail = Firebase.auth.currentUser?.email ?: "",
            messageText = messageText,
            createdAt = System.currentTimeMillis(),
            senderName = Firebase.auth.currentUser?.displayName ?: ""
        )

        // Log to check if the function is called
        Log.d("sendMessage", "sendMessage function called for user: $username with message: $messageText")

        db.reference.child("messages").child(username).push().setValue(message)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("sendMessage", "Message sent successfully to $username")
                    postNotificationToUsers(context, username, message.senderName, messageText ?: "")
                } else {
                    Log.e("sendMessage", "Failed to send message to $username", task.exception)
                }
            }
    }



    fun listenForMessages(username: String) {
        db.getReference("messages").child(username).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Message>()
                    snapshot.children.forEach { data ->
                        val message = data.getValue(Message::class.java)
                        message?.let {
                            list.add(it)
                        }
                    }
                    _messages.value = list
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatViewModel", "Failed to load messages", error.toException())
                }
            })
        subscribeForNotification(username)
        registerUserIdtoChannel(username)
    }

    fun registerUserIdtoChannel(username: String) {
        val currentUser = Firebase.auth.currentUser
        val ref = db.reference.child("usernames").child(username).child("users")
        ref.child(currentUser?.uid ?: "").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        ref.child(currentUser?.uid ?: "").setValue(currentUser?.email)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }
        )
    }

    private fun subscribeForNotification(username: String) {
        FirebaseMessaging.getInstance().subscribeToTopic("group_$username")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("ChatViewModel", "Subscribed to topic: group_$username")
                } else {
                    Log.d("ChatViewModel", "Failed to subscribe to topic: group_$username")
                }
            }
    }

    private fun postNotificationToUsers(context: Context, username: String, senderName: String, messageContent: String) {
        val fcmUrl = "https://fcm.googleapis.com/v1/projects/journeybuddy-83c5e/messages:send"
        val jsonBody = JSONObject().apply {
            put("message", JSONObject().apply {
                put("topic", "group_$username")
                put("notification", JSONObject().apply {
                    put("title", "New message in $username")
                    put("body", "$senderName: $messageContent")
                })
            })
        }

        val requestBody = jsonBody.toString()

        val request = object : StringRequest(Method.POST, fcmUrl, Response.Listener {
            Log.d("ChatViewModel", "Notification sent successfully")
        }, Response.ErrorListener {
            Log.e("ChatViewModel", "Failed to send notification")
        }) {
            override fun getBody(): ByteArray = requestBody.toByteArray()

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${getAccessToken(context)}"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun getAccessToken(context: Context): String {
        val inputStream = context.resources.openRawResource(R.raw.chatter_key)
        val googleCreds = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        return googleCreds.refreshAccessToken().tokenValue
    }
}