package com.example.testapp.features.chs

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview

class Chs {
    companion object {

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun ChsScreen(navController: NavController, viewModel: ChsViewModel = viewModel()) {
            val allUsernames by viewModel.usernames.collectAsState()
            var searchQuery by remember { mutableStateOf("") }

            val filteredUsernames = remember(searchQuery, allUsernames) {
                if (searchQuery.isBlank()) allUsernames
                else allUsernames.filter {
                    it.contains(searchQuery, ignoreCase = true)
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
                    // 🔍 Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search friends") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium,  // Rounded corners
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

                    // 🧑‍🤝‍🧑 Friend List
                    LazyColumn {
                        items(filteredUsernames) { username ->
                            FriendListItem(username = username, navController = navController)
                            Divider(
                                modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }

                    // Back to Home Button
                    Spacer(modifier = Modifier.weight(1f)) // Push the button to the bottom
                    Button(
                        onClick = {
                            navController.navigate("home") // Navigate back to the home screen
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(MaterialTheme.shapes.medium),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0288D1), // Blue color
                            contentColor = Color.White // White text
                        )
                    ) {
                        Text(
                            text = "Back to Home",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
        @Composable
        fun FriendListItem(username: String, navController: NavController) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("chat/${username}") }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.medium),  // Rounded corners for avatar
                    color = Color(0xFF90CAF9),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = username.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = "Tap to start a chat",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        @Preview(showBackground = true)
        @Composable
        fun PreviewChsScreen() {
            val navController = rememberNavController()
            ChsScreen(navController = navController)
        }
    }
}
