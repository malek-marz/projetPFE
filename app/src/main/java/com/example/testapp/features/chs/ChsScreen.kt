package com.example.testapp.features.chs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class Chs {
    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun ChsScreen(navController: NavController, viewModel: ChsViewModel = viewModel()) {
            val allUsers by viewModel.users.collectAsState()
            var searchQuery by remember { mutableStateOf("") }
            val db = FirebaseFirestore.getInstance()
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid

            var mutedUsers by remember { mutableStateOf<List<String>>(emptyList()) }

            LaunchedEffect(currentUid) {
                currentUid?.let { uid ->
                    val doc = db.collection("users").document(uid).get().await()
                    mutedUsers = doc.get("muted") as? List<String> ?: emptyList()
                }
            }

            val filteredUsers = remember(searchQuery, allUsers) {
                if (searchQuery.isBlank()) allUsers
                else allUsers.filter {
                    it.username.contains(searchQuery, ignoreCase = true)
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Chats", fontSize = 20.sp) },
                        colors = TopAppBarDefaults.mediumTopAppBarColors(
                            containerColor = Color(0xFF2196F3),
                            titleContentColor = Color.White
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFFF0F7FF))
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Trouver amis") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Color.Gray)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF64B5F6),
                            unfocusedBorderColor = Color(0xFFBBDEFB),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color(0xFF2196F3)
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("invitation_screen") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Inviter", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Invitation", color = Color.White)
                        }

                        Button(
                            onClick = { navController.navigate("blocked_users") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF616161)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Block, contentDescription = "Bloqués", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bloqués", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn {
                        items(filteredUsers) { user ->
                            FriendListItem(
                                user = user,
                                navController = navController,
                                isMuted = mutedUsers.contains(user.uid),
                                onReportClick = {
                                    navController.navigate("report_user/${user.uid}")
                                },
                                onDeleteClick = {
                                    val db = FirebaseFirestore.getInstance()
                                    val currentUserId = currentUid ?: return@FriendListItem

                                    // Supprimer l'ami du côté de l'utilisateur courant
                                    db.collection("users")
                                        .document(currentUserId)
                                        .collection("friends")
                                        .document(user.uid)
                                        .delete()

                                    // Supprimer l'utilisateur courant du côté de l'ami
                                    db.collection("users")
                                        .document(user.uid)
                                        .collection("friends")
                                        .document(currentUserId)
                                        .delete()
                                }


                            )
                            Divider(
                                modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }
                }
            }
        }

        @Composable
        fun FriendListItem(
            user: UserDisplay,
            navController: NavController,
            isMuted: Boolean,
            onReportClick: () -> Unit,
            onDeleteClick: () -> Unit
        ) {
            var showDeleteDialog by remember { mutableStateOf(false) }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            onDeleteClick()
                            showDeleteDialog = false
                        }) {
                            Text("Supprimer", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Annuler")
                        }
                    },
                    title = { Text("Confirmer la suppression") },
                    text = { Text("Voulez-vous vraiment supprimer cet utilisateur ?") }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("chat_screen/${user.username}/${user.email}")
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.medium),
                    color = Color(0xFF90CAF9),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user.username.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.Black
                    )

                    Text(
                        text = "Appuyez pour discuter",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { onReportClick() }) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = "Report",
                        tint = Color(0xFFEF5350)
                    )
                }

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Supprimer",
                        tint = Color(0xFF757575)
                    )
                }
            }
        }
    }
}
