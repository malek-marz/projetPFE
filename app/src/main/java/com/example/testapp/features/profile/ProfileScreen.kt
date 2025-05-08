package com.example.journeybuddy.ui.screens

import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.flowlayout.FlowRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.testapp.R

// --- Dummy UserProfile à adapter selon ton modèle réel
data class UserProfile(
    val id: String, // Utilisation de l'ID plutôt que de l'email
    val name: String,
    val interests: List<String>
)

class ProfileViewModel : androidx.lifecycle.ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    fun updateUserProfile(
        userId: String,
        updatedProfile: UserProfile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Mise à jour dans Firestore avec l'ID utilisateur
        db.collection("users")
            .document(userId)
            .set(updatedProfile)
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "Profil mis à jour : $updatedProfile")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileViewModel", "Erreur lors de la mise à jour", exception)
                onFailure(exception)
            }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val interests = listOf(
        "Aviation", "Art", "Cars", "Technology", "Fashion", "Health care",
        "Geography", "Finance", "Mental Health", "Programming", "Cinema",
        "Sports", "Travel", "Gaming", "Photography", "Design", "Music"
    )

    val selectedInterests = remember { mutableStateListOf<String>() }

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

    val blueNormal = Color(0xFF2196F3)

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

        Spacer(modifier = Modifier.weight(1f))

        val canContinue = selectedInterests.size >= 2

        Button(
            onClick = {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid // Utilisation de l'ID utilisateur plutôt que de l'email

                if (userId != null && canContinue) {
                    val updatedProfile = UserProfile(
                        id = userId,  // Passer l'ID utilisateur
                        name = currentUser.displayName ?: "",
                        interests = selectedInterests.toList()
                    )

                    viewModel.updateUserProfile(
                        userId = userId,  // Passer l'ID utilisateur à la méthode du ViewModel
                        updatedProfile = updatedProfile,
                        onSuccess = {
                            navController.navigate("next_screen")
                        },
                        onFailure = { error ->
                            Log.e("ProfileScreen", "Erreur lors de la mise à jour : $error")
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
