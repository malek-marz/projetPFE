package com.example.testapp.features.profileUser

import android.widget.Toast
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.accompanist.flowlayout.FlowRow

class ProfileUserScreen {
    companion object {
        @Composable
        fun profileUser(navController: NavController , viewModel: ProfileUserViewModel = viewModel()) {
            val state by viewModel.state.collectAsState()
            val context = LocalContext.current

            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    Log.d("ProfileScreen", "Image selected: $uri")
                    viewModel.uploadProfilePicture(
                        uri = uri,
                        onSuccess = {
                            Toast.makeText(context, "Photo mise à jour", Toast.LENGTH_SHORT).show()
                            Log.d("ProfileScreen", "Image upload and Firestore update successful")
                        },
                        onFailure = {
                            Toast.makeText(context, "Erreur : ${it.message}", Toast.LENGTH_SHORT).show()
                            Log.e("ProfileScreen", "Image upload or Firestore update failed", it)
                        }
                    )
                } else {
                    Log.w("ProfileScreen", "No image selected")
                }
            }

            LaunchedEffect(Unit) {
                viewModel.fetchUserProfile()
            }

            ProfileUserContent(user = state, navController = navController, onChangePhoto = {
                launcher.launch("image/*")
            })
        }
    }
}

@Composable
fun ProfileUserContent(user: User, navController: NavController, onChangePhoto: () -> Unit) {
    val primaryColor = Color(0xFF1A73E8)
    val lightPrimaryColor = primaryColor.copy(alpha = 0.65f)
    val backgroundColor = Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Votre Profil",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = lightPrimaryColor,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(primaryColor)
                .clickable { onChangePhoto() },
            contentAlignment = Alignment.Center
        ) {
            if (user.profilePicUrl.isNotEmpty()) {
                Image(
                    painter = rememberImagePainter(user.profilePicUrl),
                    contentDescription = "Photo de profil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                val initials = "${user.firstName.firstOrNull()?.uppercaseChar() ?: ""}${user.lastName.firstOrNull()?.uppercaseChar() ?: ""}"
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        InfoRow("Nom", user.username, Icons.Default.Person, lightPrimaryColor)
        InfoRow("Email", user.email, Icons.Default.Email, lightPrimaryColor)
        InfoRow("Date de naissance", user.birthday, Icons.Default.DateRange, lightPrimaryColor)
        InfoRow("Pays", user.country, Icons.Default.Public, lightPrimaryColor)
        InfoRow("Sexe", user.gender, Icons.Default.Wc, lightPrimaryColor)
        if (user.savedCountryName.isNotEmpty()) {
            InfoRow("Pays visités", user.savedCountryName, Icons.Default.Flag, lightPrimaryColor)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Vos critères :",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = lightPrimaryColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (user.criteria.isEmpty()) {
            Text(text = "Aucun critère défini.", color = Color.Gray)
        } else {
            FlowRow(
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                user.criteria.forEach { critere ->
                    ChipItem(text = critere, color = lightPrimaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { navController.navigate("User") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(backgroundColor = primaryColor)
        ) {
            Text("Modifier mes critères", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: ImageVector, iconTint: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 12.dp)
        )
        Column {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF888888),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ChipItem(text: String, color: Color) {
    Surface(
        color = Color(0xFFF0F0F0),  // fond gris très clair pour douceur
        shape = MaterialTheme.shapes.small,
        elevation = 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
