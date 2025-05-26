package com.example.journeybuddy.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.flowlayout.FlowRow
import com.google.firebase.auth.FirebaseAuth

class ProfileScreen {
    companion object {
        val ProfileScreenRoute = "profile_screen"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileScreenViewModel = viewModel()
) {
    // Regroupement des intérêts par catégorie
    val interestsByCategory = mapOf(
        "Type de voyage" to listOf("Road Trip", "City Break", "Nature", "Plage", "Aventure", "Culture", "Croisière", "Luxe", "Camping"),
        "Activités touristiques" to listOf("Randonnée", "Musées", "Photographie", "Plongée", "Ski", "Safari", "Gastronomie", "Shopping"),
        "Ambiance recherchée" to listOf("Relax", "Fête", "Romantique", "Spiritualité", "Famille", "Soleil", "Neige"),
        "Préférences climatiques" to listOf("Tropical", "Tempéré", "Froid", "Désert", "Montagneux"),
        "Moyens de transport" to listOf("Voiture", "Train", "Avion", "Bateau", "Vélo")
    )


    val selectedInterests = remember { mutableStateListOf<String>() }

    val interestIcons = mapOf(
        "Road Trip" to Icons.Default.DirectionsCar,
        "City Break" to Icons.Default.LocationCity,
        "Nature" to Icons.Default.Nature,
        "Plage" to Icons.Default.BeachAccess,
        "Aventure" to Icons.Default.Hiking,
        "Culture" to Icons.Default.AccountBalance,
        "Croisière" to Icons.Default.DirectionsBoat,
        "Luxe" to Icons.Default.Star,
        "Camping" to Icons.Default.Landscape,

        "Randonnée" to Icons.Default.DirectionsWalk,
        "Musées" to Icons.Default.Museum,
        "Photographie" to Icons.Default.CameraAlt,
        "Plongée" to Icons.Default.Pool,
        "Ski" to Icons.Default.AcUnit,
        "Safari" to Icons.Default.Pets,
        "Gastronomie" to Icons.Default.Restaurant,
        "Shopping" to Icons.Default.ShoppingCart,

        "Relax" to Icons.Default.SelfImprovement,
        "Fête" to Icons.Default.Celebration,
        "Romantique" to Icons.Default.Favorite,
        "Spiritualité" to Icons.Default.Spa,
        "Famille" to Icons.Default.Groups,
        "Soleil" to Icons.Default.WbSunny,
        "Neige" to Icons.Default.AcUnit,

        "Tropical" to Icons.Default.WbSunny,
        "Tempéré" to Icons.Default.Thermostat,
        "Froid" to Icons.Default.AcUnit,
        "Désert" to Icons.Default.Landscape,
        "Montagneux" to Icons.Default.Terrain,

        "Voiture" to Icons.Default.DirectionsCar,
        "Train" to Icons.Default.Train,
        "Avion" to Icons.Default.Flight,
        "Bateau" to Icons.Default.DirectionsBoat,
        "Vélo" to Icons.Default.DirectionsBike
    )


    val blueNormal = Color(0xFF2196F3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Sélectionnons vos intérêts.",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Veuillez sélectionner deux ou plusieurs options pour continuer.",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // **Ajout scroll ici**
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            interestsByCategory.forEach { (category, interests) ->
                Text(
                    text = category,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
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
                            Icon(
                                imageVector = interestIcons[interest] ?: Icons.Default.Star,
                                contentDescription = "$interest icon",
                                tint = blueNormal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = interest)
                        }
                    }
                }
            }
        }

        val canContinue = selectedInterests.size >= 2

        Button(
            onClick = {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid

                if (userId != null && canContinue) {
                    viewModel.updateUserInterestsOnly(
                        userId = userId,
                        interests = selectedInterests.toList(),
                        onSuccess = {
                            val interestsArg = selectedInterests.joinToString(",")
                            navController.navigate("country_screen/$interestsArg")
                        },
                        onFailure = { error ->
                            Log.e("ProfileScreen", "Erreur mise à jour intérêts : $error")
                        }
                    )
                } else {
                    Log.w("ProfileScreen", "Utilisateur non connecté ou intérêts insuffisants.")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = canContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canContinue) Color(0xFF03A9F4) else Color.LightGray,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(50)
        ) {
            Text("Continue")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen(navController = rememberNavController())
}
