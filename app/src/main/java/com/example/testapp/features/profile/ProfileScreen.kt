package com.example.journeybuddy.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.example.testapp.R

class ProfileScreen {
    companion object{
    @Composable
    fun InterestSelectionScreen(navController: NavController) {
        val interests = listOf(
            "Aviation", "Art", "Cars",
            "Technology", "Fashion",
            "Health care", "Geography", "Finance",
            "Mental Health", "Programming", "Cinema", "Sports", "Travel",
            "Gaming", "Photography", "Design", "Music"
        )

        val selectedInterests = remember { mutableStateListOf<String>() }

        // Icônes par défaut de Jetpack Compose
        val interestIcons = mapOf(
            "Aviation" to Icons.Default.AirplanemodeActive,
            "Art" to Icons.Default.Brush,
            "Cars" to Icons.Default.DirectionsCar,
            "Technology" to Icons.Default.DeviceHub,
            "Fashion" to Icons.Default.Checkroom,
            "Health care" to Icons.Default.HealthAndSafety,
            "Geography" to Icons.Default.Public,
            "Finance" to Icons.Default.Money,
            "Mental Health" to Icons.Default.Face,
            "Programming" to Icons.Default.Code,
            "Cinema" to Icons.Default.Movie,
            "Sports" to Icons.Default.Sports,
            "Travel" to Icons.Default.Flight,
            "Gaming" to Icons.Default.Gamepad,
            "Photography" to Icons.Default.CameraAlt,
            "Design" to Icons.Default.DesignServices,
            "Music" to Icons.Default.MusicNote
        )

        // Couleur personnalisée pour les boutons
        val blueNormal = Color(0xFF2196F3) // Bleu clair

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Let's select your interests.",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Please select two or more to proceed.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            FlowRow(
                mainAxisSpacing = 12.dp,
                crossAxisSpacing = 12.dp
            ) {
                interests.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest)
                    OutlinedButton(
                        onClick = {
                            if (isSelected) selectedInterests.remove(interest)
                            else selectedInterests.add(interest)
                        },
                        shape = RoundedCornerShape(50),
                        border = if (isSelected)
                            BorderStroke(2.dp, blueNormal)
                        else
                            BorderStroke(1.dp, Color.LightGray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        // Affichage de l'icône associée à l'intérêt
                        Icon(
                            imageVector = interestIcons[interest]
                                ?: Icons.Default.Star, // Icône par défaut si aucune icône n'est définie
                            contentDescription = "$interest icon",
                            tint = blueNormal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = interest)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    navController.navigate("next_screen") // Remplace par ta vraie route
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = true, // Toujours activé
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF03A9F4), // Bleu clair
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text("Continue")
            }
        }
    }
}}
@Preview(showBackground = true)
@Composable
fun PreviewInterestSelection() {
    val navController = rememberNavController()
    ProfileScreen.InterestSelectionScreen(navController)
}
