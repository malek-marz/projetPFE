package com.example.testapp.features.chatPartnerProfile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import androidx.compose.foundation.layout.FlowRow

@Composable
fun ChatPartnerProfileScreen(
    partnerUsername: String,
    viewModel: ChatPartnerProfileViewModel = viewModel()
) {
    val userState by viewModel.state.collectAsState()

    LaunchedEffect(partnerUsername) {
        viewModel.fetchUserProfileByUsername(partnerUsername)
    }

    val primaryColor = Color(0xFF42A5F5) // Bleu plus clair
    val secondaryColor = Color(0xFF64B5F6)
    val backgroundColor = Color(0xFFF2F4F7)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profil de $partnerUsername",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(bottom = 24.dp)
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

        Spacer(modifier = Modifier.height(32.dp))

        ProfileInfoItem("Nom", userState.username, Icons.Default.Person, primaryColor)
        ProfileInfoItem("Email", userState.email, Icons.Default.Email, primaryColor)
        ProfileInfoItem("Date de naissance", userState.birthday, Icons.Default.DateRange, primaryColor)
        ProfileInfoItem("Pays", userState.country, Icons.Default.Public, primaryColor)
        ProfileInfoItem("Sexe", userState.gender, Icons.Default.Wc, primaryColor)

        if (userState.savedCountryNames.isNotEmpty()) {
            SectionTitle("Pays visités", primaryColor)
            ChipsRow(userState.savedCountryNames, secondaryColor)
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("préférences", primaryColor)
        ChipsRow(userState.criteria, secondaryColor)
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String, icon: ImageVector, iconColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 16.dp)
            )
            Column {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, color: Color) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp)
    )
}

@Composable
fun ChipItem(text: String, color: Color, isHighlighted: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = BorderStroke(1.dp, color),
        elevation = 0.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            if (isHighlighted) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Dernier pays visité",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ChipsRow(chips: List<String>, color: Color) {
    if (chips.isEmpty()) {
        Text(
            text = "Aucun élément défini.",
            color = Color.Gray,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(start = 8.dp)
        )
    } else {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            chips.forEachIndexed { index, chip ->
                val isLast = index == chips.lastIndex
                ChipItem(text = chip, color = color, isHighlighted = isLast)

                if (index < chips.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}
