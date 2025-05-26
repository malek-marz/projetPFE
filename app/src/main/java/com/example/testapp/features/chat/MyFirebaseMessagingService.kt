package com.example.testapp.features.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.testapp.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FirebaseMessageService", "Message reçu : ${message.data}, ${message.notification}")
        Log.d("FirebaseMessageService", "From: ${message.from} Data: ${message.data} Notification: ${message.notification}")
        Log.d("FirebaseMessageService", "Test de réception de message")

        // Create channel once before showing notification
        createNotificationChannel(applicationContext)

        // Extract sender name and message text from data or notification
        val title = message.data["senderName"]
            ?: message.notification?.title
            ?: "Nouveau message"
        val body = message.data["messageText"]
            ?: message.notification?.body
            ?: "Vous avez un nouveau message"

        // NEW: Check if sender is muted
        val senderUid = message.data["senderUid"]
        val currentUser = Firebase.auth.currentUser
        if (senderUid != null && currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(currentUser.uid)

            userRef.get().addOnSuccessListener { document ->
                val mutedList = document.get("muted") as? List<*>
                if (mutedList?.contains(senderUid) == true) {
                    Log.d("FirebaseMessageService", "Notification ignorée : l'utilisateur $senderUid est coupé")
                    return@addOnSuccessListener
                }
                showNotification(title, body)
            }.addOnFailureListener {
                Log.e("FirebaseMessageService", "Erreur lors de la vérification des utilisateurs coupés", it)
                showNotification(title, body) // fallback: show anyway
            }
        } else {
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String?, message: String?) {
        // Ignore notification if the message was sent by the current user
        Firebase.auth.currentUser?.let {
            val email = it.email ?: ""
            if ((message?.contains(email, ignoreCase = true) == true)) {
                Log.d("FirebaseMessageService", "Notification ignorée pour l'utilisateur lui-même")
                return
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = Random().nextInt(1000)

        val notification = NotificationCompat.Builder(this, "chat_notifications")
            .setContentTitle(title ?: "Nouveau message")
            .setContentText(message ?: "")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Make sure this icon exists in your res/drawable
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d("FirebaseMessageService", "Notification affichée avec ID $notificationId")
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "chat_notifications"
            val channelName = "Chat Notifications"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for chat messages"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
