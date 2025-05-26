package com.example.testapp.features.chatPartnerProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

@Composable
fun ChatPartnerProfileScreen(
    partnerUsername: String,
    viewModel: ChatPartnerProfileViewModel = viewModel()
) {
    val userState by viewModel.state.collectAsState()

    LaunchedEffect(partnerUsername) {
        viewModel.fetchUserProfileByUsername(partnerUsername)
    }

    val primaryColor = Color(0xFF1A73E8)
    val lightPrimaryColor = primaryColor.copy(alpha = 0.65f)
    val backgroundColor = Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),  // increased top padding here
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profil de $partnerUsername",
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
            if (userState.profilePicUrl.isNotEmpty()) {
                Image(
                    painter = rememberImagePainter(userState.profilePicUrl),
                    contentDescription = "Photo de profil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                val initials = "${userState.firstName.firstOrNull()?.uppercaseChar() ?: ""}${userState.lastName.firstOrNull()?.uppercaseChar() ?: ""}"
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        InfoRow("Nom", userState.username, Icons.Default.Person, lightPrimaryColor)
        InfoRow("Email", userState.email, Icons.Default.Email, lightPrimaryColor)
        InfoRow("Date de naissance", userState.birthday, Icons.Default.DateRange, lightPrimaryColor)
        InfoRow("Pays", userState.country, Icons.Default.Public, lightPrimaryColor)
        InfoRow("Sexe", userState.gender, Icons.Default.Wc, lightPrimaryColor)
        if (userState.savedCountryName.isNotEmpty()) {
            InfoRow("Pays visités", userState.savedCountryName, Icons.Default.Flag, lightPrimaryColor)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Critères :",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = lightPrimaryColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ChipsRow(userState.criteria, lightPrimaryColor)
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
        color = Color(0xFFF0F0F0),
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
