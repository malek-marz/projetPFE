package com.example.journeybuddy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ProfileScreenState(navController: NavController) {
    val context = LocalContext.current
    // On utilise produceState pour récupérer l'état du profil de manière asynchrone.
    val profileCompleted by produceState(initialValue = false, key1 = context) {
        value=true
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (profileCompleted) "Profil Complet" else "Profil Incomplet",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (!profileCompleted) {
                    navController.navigate("profileScreen")
                }
            }
        ) {
            Text("Compléter mon Profil")
        }
    }
}
