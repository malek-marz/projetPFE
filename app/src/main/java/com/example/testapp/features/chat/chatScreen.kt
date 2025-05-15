package com.example.testapp.features.chat

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    initialEmail: String = "",
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    var recipientEmail by remember { mutableStateOf(TextFieldValue(initialEmail)) }
    var messageInput by remember { mutableStateOf(TextFieldValue("")) }
    val userEmail = Firebase.auth.currentUser?.email ?: ""

    // Listen for messages whenever recipientEmail changes (and is valid)
    LaunchedEffect(recipientEmail.text) {
        if (recipientEmail.text.isNotBlank() && recipientEmail.text.contains("@")) {
            viewModel.setRecipientEmail(recipientEmail.text)
            viewModel.listenForMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = recipientEmail,
            onValueChange = { recipientEmail = it },
            label = { Text("Email destinataire") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages) { message ->
                val isMine = message.gmail == userEmail
                ChatMessageItem(message = message, isMine = isMine)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = messageInput,
            onValueChange = {
                messageInput = it
                viewModel.setMessageText(it.text)
            },
            label = { Text("Votre message") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (recipientEmail.text.isBlank() || !recipientEmail.text.contains("@")) {
                    Toast.makeText(context, "Email destinataire invalide", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (messageInput.text.isBlank()) {
                    Toast.makeText(context, "Message vide", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                // No need to call setRecipientEmail here, LaunchedEffect does it automatically
                viewModel.sendMessage(context)
                messageInput = TextFieldValue("")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Envoyer")
        }
    }
}

@Composable
fun ChatMessageItem(message: Message, isMine: Boolean) {
    val alignment = if (isMine) Arrangement.End else Arrangement.Start
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = alignment
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "${message.senderName}: ${message.messageText}",
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
