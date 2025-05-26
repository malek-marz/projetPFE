package com.example.testapp.presentation.country

import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CountryScreen(viewModel: CountryViewModel, Interests: List<String>) {
    Log.d("CountryScreen", "Intérêts reçus dans CountryScreen: $Interests")
    val country by viewModel.countryData
    val isLoading by viewModel.isLoading
    val imageUrl = country?.image ?: ""

    LaunchedEffect(Interests) {
        viewModel.fetchCountryInfoBasedOnInterests(Interests)
    }

    var showDialog by remember { mutableStateOf(false) }
    var userReview by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEFF6FB))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header avec le bouton de retour et le titre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { /* Navigation back logic */ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Votre Destination",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // Affichage du contenu en fonction de l'état de chargement
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    country?.let { data ->
                        // Affichage de la carte via WebView
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    webViewClient = WebViewClient() // Charge dans l'application
                                    webChromeClient = WebChromeClient() // Assure une expérience plus fluide
                                    loadUrl(data.mapUrl) // L'URL de la carte
                                    settings.javaScriptEnabled = true // Active JavaScript si nécessaire
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp) // Taille de la WebView
                                .clip(RoundedCornerShape(16.dp)) // Coins arrondis
                        )

                        // Espacement entre la carte et les informations
                        Spacer(modifier = Modifier.height(24.dp))

                        // Informations du pays : Nom, capitale, langue, etc.
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = data.name,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            InfoRow(label = "Capitale:", value = data.capital)
                            InfoRow(label = "Langue:", value = data.language)
                            InfoRow(label = "Population:", value = data.population)
                            InfoRow(label = "Devise:", value = data.currency)
                            if (data.landmarks.isNullOrEmpty()) {
                                InfoRow(label = "Places recommandées :", value = "Aucune place recommandée")
                            } else {
                                Column {
                                    Text(
                                        text = "Places recommandées :",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    // Pour chaque place, afficher une ligne avec un point devant
                                    data.landmarks.forEach { place ->
                                        Text(
                                            text = "• $place",
                                            fontSize = 16.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Affichage du drapeau
                            Image(
                                painter = rememberAsyncImagePainter(data.flagUrl),
                                contentDescription = "Drapeau",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Section de description
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = data.description,
                                        fontSize = 18.sp,
                                        color = Color.Black
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Image de monument ou image emblématique
                                    Image(
                                        painter = rememberAsyncImagePainter(data.image.orEmpty()),
                                        contentDescription = "Image emblématique",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Ajout du bouton "Donner votre avis" juste avant les autres boutons
                            Button(
                                onClick = { showDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Donner votre avis à propos de l'ancien voyage",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp)) // espace entre les boutons

                            // Boutons d'actions
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { viewModel.exploreMore() },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                                ) {
                                    Text("Explorer plus", color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        viewModel.saveSelectedCountry(
                                            onSuccess = {
                                                Toast.makeText(context, "Pays enregistré avec succès", Toast.LENGTH_SHORT).show()
                                            },
                                            onFailure = { e ->
                                                Toast.makeText(context, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                                ) {
                                    Text("Planifier le voyage", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Suppression du bouton flottant "Donner votre avis" (plus besoin ici)
        /*
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF1D4ED8))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clickable {
                    showDialog = true
                }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Donner votre avis à propos de l'ancien voyage",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.widthIn(max = 180.dp)
                )
            }
        }
        */

        // Dialog pour écrire l'avis
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Donnez votre avis") },
                text = {
                    TextField(
                        value = userReview,
                        onValueChange = { userReview = it },
                        placeholder = { Text("Écrivez votre description ici...") },
                        singleLine = false,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (userReview.isNotBlank()) {
                            viewModel.saveUserReview(userReview,
                                onSuccess = {
                                    Toast.makeText(context, "Avis sauvegardé !", Toast.LENGTH_SHORT).show()
                                    userReview = ""
                                    showDialog = false
                                },
                                onFailure = { e ->
                                    Toast.makeText(context, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }) {
                        Text("Enregistrer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CountryScreenPreview() {
    // Pas de données réelles ici, à adapter selon besoin
    Text("Aperçu CountryScreen")
}
