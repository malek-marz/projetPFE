package com.example.testapp.features.chat

import ChatViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    initialEmail: String,
    initialUsername: String? = null,
    viewModel: ChatViewModel = viewModel(),
    onProfileClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser
    val messages by viewModel.messages.collectAsState()
    val recipientUsername by viewModel.recipientUsername.collectAsState()

    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(key1 = initialEmail) {
        viewModel.setRecipientEmail(initialEmail)
        viewModel.listenForMessages()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = recipientUsername ?: "Chargement...",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        recipientUsername?.let { onProfileClick(it) }
                    }
                    .padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE3EEFF), shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "View Profile",
                        tint = Color(0xFF0D47A1),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Profil",
                    color = Color(0xFF0D47A1),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Séparation visuelle
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF0D47A1).copy(alpha = 0.5f))
        )

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages.reversed()) { msg ->
                val isMine = msg.sendergmail == currentUser?.email
                MessageBubblePro(
                    message = msg.messageText ?: "",
                    isMine = isMine,
                    createdAt = msg.createdAt
                )
            }
        }

        // Séparation visuelle
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF0D47A1).copy(alpha = 0.5f))
                .padding(vertical = 4.dp)
        )

        // Input field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = MaterialTheme.shapes.large)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color.Black, MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF0D47A1)),
                cursorBrush = SolidColor(Color.Black)
            )

            Text(
                text = "Envoyer",
                modifier = Modifier
                    .clickable(enabled = textFieldValue.text.isNotBlank()) {
                        viewModel.setMessageText(textFieldValue.text.trim())
                        viewModel.sendMessage(context)
                        textFieldValue = TextFieldValue("")
                    }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                color = if (textFieldValue.text.isNotBlank()) Color.Black else Color(0xFFAAAAAA),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
fun MessageBubblePro(
    message: String,
    isMine: Boolean,
    createdAt: Long = 0L
) {
    val timeFormatted = remember(createdAt) {
        if (createdAt != 0L) {
            val date = Date(createdAt)
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        } else {
            ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMine) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFE3EEFF), shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Sender Profile",
                    tint = Color(0xFF0D47A1),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isMine) Color(0xFF8AB4F8) else Color(0xFFE3EEFF),
                        shape = MaterialTheme.shapes.extraLarge
                    )
                    .padding(16.dp)
                    .widthIn(max = 280.dp)
            ) {
                Text(
                    text = message,
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp
                    )
                )
            }

            if (timeFormatted.isNotEmpty()) {
                Text(
                    text = timeFormatted,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                )
            }
        }

        if (isMine) {
            Spacer(modifier = Modifier.width(40.dp))
        }
    }
}
