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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class ChatViewModel : ViewModel() {

    private val db = Firebase.database
    private val firestore = FirebaseFirestore.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _recipientUsername = MutableStateFlow<String?>(null)
    val recipientUsername = _recipientUsername.asStateFlow()

    private var recipientEmail: String? = null
    private var messageText: String? = null
    private val _lastMessage = MutableStateFlow<String?>(null)
    val lastMessage = _lastMessage.asStateFlow()

    private val _lastMessageTime = MutableStateFlow<String?>(null)
    val lastMessageTime = _lastMessageTime.asStateFlow()

    private fun sanitize(input: String): String {
        return input
            .replace("@", "_at_")
            .replace(".", "_dot_")
    }

    private fun getChatId(email1: String, email2: String): String {
        val safe1 = sanitize(email1.trim().lowercase())
        val safe2 = sanitize(email2.trim().lowercase())
        return if (safe1 < safe2) "$safe1-$safe2" else "$safe2-$safe1"
    }

    fun setRecipientEmail(email: String) {
        if (!email.contains("@")) {
            Log.e("ChatViewModel", "❌ Invalid recipient email provided: '$email'")
            return
        }
        this.recipientEmail = email
        fetchRecipientUsername(email)
    }

    fun setMessageText(text: String) {
        this.messageText = text
    }

    private fun fetchRecipientUsername(email: String) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val username = doc.getString("username") ?: "Unknown"
                    _recipientUsername.value = username
                } else {
                    _recipientUsername.value = "Unknown"
                }
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "❌ Error fetching username", it)
                _recipientUsername.value = "Error"
            }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun sendMessage(context: Context) {
        val sender = Firebase.auth.currentUser ?: run {
            Log.e("ChatViewModel", "❌ No authenticated user")
            return
        }
        val recipient = recipientEmail ?: run {
            Log.e("ChatViewModel", "❌ Recipient email is null")
            return
        }
        if (!recipient.contains("@")) {
            Log.e("ChatViewModel", "❌ Invalid recipient email: $recipient")
            return
        }

        val senderEmail = sender.email ?: run {
            Log.e("ChatViewModel", "❌ Sender email is null")
            return
        }

        val chatId = getChatId(senderEmail, recipient)
        Log.d("ChatViewModel", "Sending message from: $senderEmail to: $recipient with chatId: $chatId")

        val msgText = messageText ?: run {
            Log.e("ChatViewModel", "❌ Message text is null")
            return
        }

        // Fetch sender name from Firestore here before pushing message and sending notification
        fetchSenderNameAndSend(context, chatId, senderEmail, msgText)

    }

    private fun fetchSenderNameAndSend(
        context: Context, chatId: String, senderEmail: String, msgText: String
    ) {
        Log.d("ChatViewModel", "Fetching senderName for email: $senderEmail")

        firestore.collection("users")
            .whereEqualTo("email", senderEmail)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("ChatViewModel", "Documents found: ${documents.size()}")
                if (!documents.isEmpty) {
                    val username = documents.documents[0].getString("username")
                    Log.d("ChatViewModel", "Username field value: $username")
                } else {
                    Log.d("ChatViewModel", "No user found with email: $senderEmail")
                }

                val senderName = if (!documents.isEmpty) {
                    documents.documents[0].getString("username") ?: "Unknown Sender"
                } else {
                    "Unknown Sender"
                }
                Log.d("ChatViewModel", "Fetched senderName from Firestore: $senderName")

                val message = Message(
                    gmail = senderEmail,
                    sendergmail = senderEmail,
                    messageText = msgText,
                    createdAt = System.currentTimeMillis(),
                    senderName = senderName
                )

                Log.d("ChatViewModel", "Pushing message to DB: $message")

                db.reference.child("messages").child(chatId).push().setValue(message)
                    .addOnSuccessListener {
                        Log.d("ChatViewModel", "✅ Message sent to DB")
                        postNotificationToUsers(context, chatId, senderName, msgText)
                    }
                    .addOnFailureListener {
                        Log.e("ChatViewModel", "❌ Failed to send message to DB", it)
                    }

                registerUserToChat(chatId)
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "❌ Failed to fetch sender name from Firestore", it)
                val fallbackName = senderEmail.substringBefore('@')
                Log.d("ChatViewModel", "Using fallback senderName: $fallbackName")

                val message = Message(
                    gmail = senderEmail,
                    sendergmail = senderEmail,
                    messageText = msgText,
                    createdAt = System.currentTimeMillis(),
                    senderName = fallbackName
                )

                Log.d("ChatViewModel", "Pushing message to DB (fallback): $message")

                db.reference.child("messages").child(chatId).push().setValue(message)
                    .addOnSuccessListener {
                        Log.d("ChatViewModel", "✅ Message sent to DB (fallback)")
                        postNotificationToUsers(context, chatId, fallbackName, msgText)
                    }
                    .addOnFailureListener { error ->
                        Log.e("ChatViewModel", "❌ Failed to send message to DB (fallback)", error)
                    }

                registerUserToChat(chatId)
            }
    }


    fun listenForMessages() {
        val sender = Firebase.auth.currentUser ?: return
        val recipient = recipientEmail ?: return
        if (!recipient.contains("@")) {
            Log.e("ChatViewModel", "❌ Invalid recipient email for listening: $recipient")
            return
        }

        val senderEmail = sender.email ?: return
        val chatId = getChatId(senderEmail, recipient)

        db.reference.child("messages").child(chatId).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(Message::class.java)
                    }
                    _messages.value = list

                    if (list.isNotEmpty()) {
                        val last = list.last()
                        _lastMessage.value = last.messageText
                        _lastMessageTime.value = formatTimestampToHourMinute(last.createdAt)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatViewModel", "❌ Failed to load messages", error.toException())
                }
            })

        subscribeToNotification(chatId)
    }

    private fun formatTimestampToHourMinute(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return format.format(date)
    }

    private fun registerUserToChat(chatId: String) {
        val currentUser = Firebase.auth.currentUser ?: return
        val ref = db.reference.child("chatUsers").child(chatId).child("users")
        ref.child(currentUser.uid).setValue(currentUser.email)
    }

    private fun subscribeToNotification(chatId: String) {
        FirebaseMessaging.getInstance().subscribeToTopic("group_$chatId")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to group_$chatId successfully")
                } else {
                    Log.e("FCM", "Failed to subscribe to group_$chatId", task.exception)
                }
            }

    }

    private fun postNotificationToUsers(context: Context, chatId: String, senderName: String, content: String) {
        Log.d("ChatViewModel", "Posting notification to topic group_$chatId with message: $senderName: $content")

        val fcmUrl = "https://fcm.googleapis.com/v1/projects/journeybuddy-83c5e/messages:send"
        val jsonBody = JSONObject().apply {
            put("message", JSONObject().apply {
                put("topic", "group_$chatId")
                put("notification", JSONObject().apply {
                    put("title", "Nouveau message")
                    put("body", "$senderName: $content")
                })
                put("data", JSONObject().apply {
                    put("senderName", senderName)
                    put("messageText", content)
                })
                put("android", JSONObject().apply {
                    put("priority", "high")
                })
            })
        }

        Log.d("ChatViewModel", "Notification JSON body: ${jsonBody.toString(2)}")

        val request = object : StringRequest(Method.POST, fcmUrl, Response.Listener {
            Log.d("ChatViewModel", "✅ Notification sent successfully")
        }, Response.ErrorListener { error ->
            Log.e("ChatViewModel", "❌ Notification failed", error)
            if (error.networkResponse != null) {
                val statusCode = error.networkResponse.statusCode
                val data = String(error.networkResponse.data ?: ByteArray(0))
                Log.e("ChatViewModel", "❌ Network response code: $statusCode, data: $data")
            }
        }) {
            override fun getBody(): ByteArray = jsonBody.toString().toByteArray()
            override fun getHeaders(): MutableMap<String, String> {
                val headers = mutableMapOf<String, String>()
                val token = try {
                    getAccessToken(context)
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "❌ Failed to get access token", e)
                    ""
                }
                headers["Authorization"] = "Bearer $token"
                headers["Content-Type"] = "application/json"
                Log.d("ChatViewModel", "Request headers: $headers")
                return headers
            }
        }

        Volley.newRequestQueue(context).add(request)
        Log.d("ChatViewModel", "Notification request added to Volley queue")
    }

    private fun getAccessToken(context: Context): String {
        val stream = context.resources.openRawResource(R.raw.chatter_key)
        val credentials = GoogleCredentials.fromStream(stream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        val token = credentials.refreshAccessToken().tokenValue
        Log.d("ChatViewModel", "Access token retrieved successfully")
        return token
    }
}