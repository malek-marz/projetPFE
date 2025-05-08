package com.example.testapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testapp.models.FriendSuggestion
import com.example.testapp.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    currentUserEmail: String,
    viewModel: HomeViewModel = viewModel()
) {
    val friendSuggestions by viewModel.friendSuggestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showPopup by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(currentUserEmail) {
        viewModel.loadFriendSuggestions(currentUserEmail)
    }

    // Affichage d'un message d'erreur si nécessaire
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text("Erreur") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { viewModel.clearErrorMessage() }) {
                    Text("OK")
                }
            }
        )
    }

    // Structure de l'interface utilisateur avec la barre supérieure et la barre de navigation inférieure
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("JourneyBuddy", color = Color.White) },
                actions = {
                    IconButton(onClick = {
                        showPopup = true
                    }) {
                        Icon(Icons.Default.Message, contentDescription = "Notes", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3)
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFBBDEFB))  // Fond bleu clair
        ) {
            // Section Friend Suggestions placée ici
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = "Friend Suggestions",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Chargement des suggestions d'amis
                if (isLoading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                } else if (friendSuggestions.isEmpty()) {
                    item {
                        Text("No suggestions available", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(friendSuggestions) { friend ->
                        FriendSuggestionCard(
                            friend = friend,
                            onAddFriend = { email -> viewModel.addFriend(currentUserEmail, email) },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Zone de texte centrale pour la description
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Write a description about your journey:", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("e.g., My adventure in Paris!") },
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.White, shape = MaterialTheme.shapes.medium)
                        .padding(16.dp)
                )
            }

            // Affichage du popup de note si nécessaire
            if (showPopup) {
                NotePopup(
                    onDismiss = { showPopup = false },
                    onNoteSaved = { note ->
                        // Tu peux faire quelque chose avec la note ici, comme l'enregistrer
                        println("Note saved: $note")
                    }
                )
            }
        }
    }
}

@Composable
fun NotePopup(onDismiss: () -> Unit, onNoteSaved: (String) -> Unit) {
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Write a Note") },
        text = {
            Column {
                Text("Share a note about your journey!")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("e.g., Just visited Tokyo 🇯🇵") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onNoteSaved(noteText)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(containerColor = Color(0xFF2196F3)) {
        NavigationBarItem(
            selected = true,
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("chat") },
            icon = { Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color.White) }
        )
    }
}

@Composable
fun FriendSuggestionCard(
    friend: FriendSuggestion,
    onAddFriend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(friend.name, style = MaterialTheme.typography.bodyLarge)
                Text(friend.email, style = MaterialTheme.typography.bodyMedium)
                if (friend.mutualFriends > 0) {
                    Text("${friend.mutualFriends} mutual friends", style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(onClick = { onAddFriend(friend.email) }) {
                Text("Add")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController, currentUserEmail = "test@example.com")
}
