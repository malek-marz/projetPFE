package com.example.testapp.features.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.testapp.R
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

class ChatViewModel : ViewModel() {

    private val db = Firebase.database

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private var recipientEmail: String? = null
    private var messageText: String? = null

    private fun sanitize(input: String): String {
        return input.replace(".", "_")
    }

    private fun getChatId(email1: String, email2: String): String {
        val safe1 = sanitize(email1.trim().lowercase())
        val safe2 = sanitize(email2.trim().lowercase())
        return if (safe1 < safe2) "$safe1-$safe2" else "$safe2-$safe1"
    }

    fun setRecipientEmail(email: String) {
        if (!email.contains("@")) {
            Log.e("ChatViewModel", "❌ Email invalide fourni comme destinataire: '$email'")
            return
        }
        this.recipientEmail = email
    }

    fun setMessageText(text: String) {
        this.messageText = text
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun sendMessage(context: Context) {
        val sender = Firebase.auth.currentUser ?: return
        val recipient = recipientEmail ?: return
        if (!recipient.contains("@")) {
            Log.e("ChatViewModel", "❌ Destinataire invalide. Pas d'envoi.")
            return
        }

        val chatId = getChatId(sender.email ?: return, recipient)
        Log.d("ChatViewModel", "Sending message from: ${sender.email} to: $recipient")

        val message = Message(
            gmail = sender.email ?: "",
            sendergmail = sender.email ?: "",
            messageText = messageText,
            createdAt = System.currentTimeMillis(),
            senderName = sender.displayName ?: ""
        )

        db.reference.child("messages").child(chatId).push().setValue(message)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "✅ Message sent")
                postNotificationToUsers(context, chatId, message.senderName, message.messageText ?: "")
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "❌ Failed to send message", it)
            }

        registerUserToChat(chatId)
    }

    fun listenForMessages() {
        val sender = Firebase.auth.currentUser ?: return
        val recipient = recipientEmail ?: return
        if (!recipient.contains("@")) {
            Log.e("ChatViewModel", "❌ Destinataire invalide pour l'écoute des messages")
            return
        }

        val chatId = getChatId(sender.email ?: return, recipient)

        db.reference.child("messages").child(chatId).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(Message::class.java)
                    }
                    _messages.value = list
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatViewModel", "❌ Failed to load messages", error.toException())
                }
            })

        subscribeToNotification(chatId)
    }

    private fun registerUserToChat(chatId: String) {
        val currentUser = Firebase.auth.currentUser ?: return
        val ref = db.reference.child("chatUsers").child(chatId).child("users")
        ref.child(currentUser.uid).setValue(currentUser.email)
    }

    private fun subscribeToNotification(chatId: String) {
        FirebaseMessaging.getInstance().subscribeToTopic("group_$chatId")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("ChatViewModel", "🔔 Subscribed to topic group_$chatId")
                }
            }
    }

    private fun postNotificationToUsers(context: Context, chatId: String, senderName: String, content: String) {
        val fcmUrl = "https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send"  // Remplace par ton projectId Firebase
        val jsonBody = JSONObject().apply {
            put("message", JSONObject().apply {
                put("topic", "group_$chatId")
                put("notification", JSONObject().apply {
                    put("title", "Nouveau message")
                    put("body", "$senderName: $content")
                })
            })
        }

        val request = object : StringRequest(Method.POST, fcmUrl, Response.Listener {
            Log.d("ChatViewModel", "✅ Notification sent")
        }, Response.ErrorListener {
            Log.e("ChatViewModel", "❌ Notification failed", it)
        }) {
            override fun getBody(): ByteArray = jsonBody.toString().toByteArray()
            override fun getHeaders(): MutableMap<String, String> {
                val headers = mutableMapOf<String, String>()
                headers["Authorization"] = "Bearer ${getAccessToken(context)}"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun getAccessToken(context: Context): String {
        val stream = context.resources.openRawResource(R.raw.chatter_key)
        val credentials = GoogleCredentials.fromStream(stream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        return credentials.refreshAccessToken().tokenValue
    }
}
