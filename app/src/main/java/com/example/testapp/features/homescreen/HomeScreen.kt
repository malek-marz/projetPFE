package com.example.testapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.testapp.models.FriendSuggestion
import com.example.testapp.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    currentUserEmail: String,
    viewModel: HomeViewModel = viewModel()
) {
    // Collect state from the ViewModel
    val friendSuggestions by viewModel.friendSuggestions.collectAsState(emptyList())
    val isLoading by viewModel.isLoading.collectAsState(true)
    val errorMessage by viewModel.errorMessage.collectAsState(null)


    LaunchedEffect(currentUserEmail) {
        viewModel.loadFriendSuggestions(currentUserEmail)
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text("Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { viewModel.clearErrorMessage() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Friend Suggestions",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (isLoading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            } else if (friendSuggestions.isEmpty()) {
                item {
                    Text(
                        text = "No suggestions available",
                        modifier = Modifier.padding(16.dp)
                    )
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
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomAppBar(
        content = {
            IconButton(onClick = { navController.navigate("home") }) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
        }
    )
}

@Composable
fun FriendSuggestionCard(
    friend: FriendSuggestion,
    onAddFriend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = friend.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (friend.mutualFriends > 0) {
                    Text(
                        text = "${friend.mutualFriends} mutual friends",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { onAddFriend(friend.email) },
                modifier = Modifier.height(36.dp)
            ) {
                Text("Add Friend")
            }
        }
    }
}

@Preview(device = "id:Nexus S")
@Composable
private fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController, currentUserEmail = "test@example.com")
}
