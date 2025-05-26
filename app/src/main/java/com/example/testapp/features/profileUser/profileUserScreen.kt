package com.example.testapp.features.profileUser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.flowlayout.FlowRow

class ProfileUserScreen {
    companion object {
        @Composable
        fun profileUser(navController: NavController, viewModel: ProfileUserViewModel = viewModel()) {
            val state by viewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.fetchUserProfile()
            }

            ProfileUserContent(user = state, navController = navController)
        }

        const val profileUserScreenRoute = "profileUser"
    }
}

@Composable
fun ProfileUserContent(user: User, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 30.dp)
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Votre Profil",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            elevation = 0.dp,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            backgroundColor = Color.Transparent,
            contentColor = Color.Transparent
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                InfoRow("Nom", user.username, Icons.Filled.Person)
                InfoRow("Email", user.email, Icons.Filled.Email)
                InfoRow("Birthday", user.birthday, Icons.Filled.CalendarToday)
                InfoRow("Country", user.country, Icons.Filled.Public)
                InfoRow("Gender", user.gender, Icons.Filled.Person)
            }
        }

        // ✅ Vos critères
        Text(
            text = "Vos critères :",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        if (user.criteria.isEmpty()) {
            Text(text = "Aucun critère défini.", color = Color.Gray)
        } else {
            FlowRow(
                mainAxisSpacing = 12.dp,
                crossAxisSpacing = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                user.criteria.forEach { critere ->
                    ChipItem(text = critere)
                }
            }
        }

        // 👇 Texte + bouton pour modifier
        Text(
            text = "Modifier vos critères ?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Button(
            onClick = { navController.navigate("User") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
        ) {
            Text("Changer critères", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colors.primary, modifier = Modifier.size(28.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.primaryVariant)
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colors.onSurface)
        }
    }
}

@Composable
fun ChipItem(text: String) {
    Surface(
        color = MaterialTheme.colors.primary.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.medium,
        elevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colors.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserPreviewPhone() {
    val navController = rememberNavController()
    ProfileUserScreen.profileUser(navController)
}