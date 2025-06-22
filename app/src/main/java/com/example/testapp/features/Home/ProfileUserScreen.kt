package com.example.testapp.presentation.profilescreen

import User
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

@Composable
fun UserProfile(userId: String, onBack: () -> Unit) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()
            user = snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("UserProfile", "Erreur chargement profil: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    val primaryColor = Color(0xFF1A73E8)
    val lightPrimaryColor = primaryColor.copy(alpha = 0.65f)
    val backgroundColor = Color(0xFFF5F5F5)

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = primaryColor)
        }
    } else {
        user?.let { u ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Profil de ${u.firstName} ${u.lastName}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = lightPrimaryColor,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = "${u.firstName.firstOrNull()?.uppercaseChar() ?: ""}${u.lastName.firstOrNull()?.uppercaseChar() ?: ""}"
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                InfoRow("Email", u.email ?: "Non spécifié", Icons.Default.Email, lightPrimaryColor)
                InfoRow("Pays", u.country ?: "Non spécifié", Icons.Default.Public, lightPrimaryColor)
                InfoRow("Langue", u.selectedLanguage ?: "Non spécifiée", Icons.Default.Language, lightPrimaryColor)
                InfoRow("Genre", u.gender ?: "Non spécifié", Icons.Default.Wc, lightPrimaryColor)
                InfoRow("Date de naissance", u.birthday ?: "Inconnue", Icons.Default.DateRange, lightPrimaryColor)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Critères :",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = lightPrimaryColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ChipsRow(u.criteria ?: emptyList(), lightPrimaryColor)

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = onBack) {
                    Text("⬅️ Retour")
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Utilisateur non trouvé", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color) {
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
            modifier = Modifier.size(24.dp).padding(end = 12.dp)
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
        color = Color(0xFFF0F0F0),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 0.dp
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

@Composable
fun ChipsRow(chips: List<String>, color: Color) {
    if (chips.isEmpty()) {
        Text(text = "Aucun critère défini.", color = Color.Gray)
    } else {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chips) { critere ->
                ChipItem(text = critere, color = color)
            }
        }
    }
}
