package com.example.testapp.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.codewithfk.chatter.feature.chat.ChatViewModel

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel(),
    username: String
) {
    val messages by viewModel.messages.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.listenForMessages(username)
    }
    val context = LocalContext.current

    ChatMessages(
        username = username,
        messages = messages,
        onSendMessage = { message ->
            viewModel.sendMessage(context, username, message)
        }
    )
}

@Composable
fun ChatMessages(
    username: String,
    messages: List<Message>,
    onSendMessage: (String) -> Unit
) {
    val hideKeyboardController = LocalSoftwareKeyboardController.current
    var msg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            reverseLayout = false
        ) {
            item {
                // Box to wrap the title with a background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()  // Make the background fill the width
                        .background(Color(0xFFE3F2FD))  // Light blue background
                        .padding(16.dp)  // Add padding for the text inside the box
                ) {
                    Text(
                        text = "$username",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterStart)  // Align the text inside the box
                    )
                }
            }
            items(messages) { message ->
                ChatBubble(message)
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = msg,
                onValueChange = { msg = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") },
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    hideKeyboardController?.hide()
                    if (msg.trim().isNotEmpty()) {
                        onSendMessage(msg.trim())
                        msg = ""
                    }
                }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray
                )
            )

            IconButton(
                onClick = {
                    if (msg.trim().isNotEmpty()) {
                        onSendMessage(msg.trim())
                        msg = ""
                        hideKeyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF4FC3F7), shape = CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isUser = message.senderName == "You" // Adjust this condition as needed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = if (isUser) Color(0xFF4FC3F7) else Color(0xFFF0F0F0),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            if (!isUser) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = message.messageText ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) Color.White else Color.Black
            )
        }
    }
}