package com.example.testapp.features.profileUser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview

class ProfileUserScreen {
    companion object {
        const val profileUserScreenRoute = "profileUser"

        @Composable
        fun profileUser(
            navController: NavController,
            viewModel: ProfileUserViewModel = viewModel()
        ) {
            val state by viewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.fetchUserProfile()
            }

            ProfileUserContent(user = state)
        }
    }
}
@Composable
fun ProfileUserContent(user: User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Profil Utilisateur",
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colors.primary
        )

        Card(
            elevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                InfoRow(label = "Nom", value = user.name)
                InfoRow(label = "Email", value = user.gmail)
                InfoRow(label = "Âge", value = "${user.age} ans")
            }
        }

        Text(
            "Critères de voyage",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colors.onBackground
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(user.criteria) { critere ->
                Chip(text = critere)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colors.primaryVariant)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colors.secondary.copy(alpha = 0.2f),
        contentColor = MaterialTheme.colors.secondary,
        elevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp
        )
    }
}

@Preview(device = "id:Nexus S", showBackground = true)
@Composable
private fun UserPreviewPhone() {
    val navController = rememberNavController()
    val mockUser = User(
        gmail = "jean.dupont@gmail.com",
        name = "Jean Dupont",
        age = 30,
        criteria = listOf("Plage", "Montagne", "Culture")
    )
    ProfileUserContent(user = mockUser)
}
