package com.example.testapp.features.chs

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationScreen(
    navController: NavController,
    viewModel: InvitationViewModel = viewModel()
) {
    val invitedUsers by viewModel.invitedUsers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Invitations reçues", fontSize = 20.sp)
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFF1976D2),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE3F2FD))
                .padding(padding)
        ) {
            if (invitedUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucune invitation reçue", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(invitedUsers) { user ->
                        InvitationItem(
                            user = user,
                            onClick = {
                                Log.d("InvitationScreen", "Clicked user: ${user.username}")
                                navController.navigate("chat_partner_profile/${user.username}")
                            },
                            onAccept = { inviterUid ->
                                viewModel.acceptInvitation(inviterUid)
                            },
                            onReject = { inviterUid ->
                                viewModel.rejectInvitation(inviterUid)
                            }
                        )
                        Divider(color = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun InvitationItem(
    user: UserDisplay,
    onClick: () -> Unit,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Color(0xFF64B5F6),
            modifier = Modifier
                .size(48.dp)
                .clickable {
                    Log.d("InvitationItem", "Clicked avatar for user ${user.username}")
                    onClick()
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = user.username.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() }
        ) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
        }

        // Bouton Accepter
        IconButton(
            onClick = { onAccept(user.uid) },
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Accepter",
                tint = Color(0xFF388E3C) // vert foncé
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Bouton Refuser
        IconButton(
            onClick = { onReject(user.uid) },
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Refuser",
                tint = Color(0xFFD32F2F) // rouge foncé
            )
        }
    }
}
