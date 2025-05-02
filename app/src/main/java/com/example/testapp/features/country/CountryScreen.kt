package com.example.testapp.presentation.country



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CountryScreen(viewModel: CountryViewModel, selectedInterests: List<String>) {
    val country by viewModel.countryData
    val isLoading by viewModel.isLoading

    LaunchedEffect(selectedInterests) {
        viewModel.fetchCountryInfoBasedOnInterests(selectedInterests)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFF6FB))
            .padding(24.dp), // Plus d'espace autour du contenu
        verticalArrangement = Arrangement.spacedBy(16.dp) // Espacement plus grand entre les éléments
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
                fontSize = 26.sp, // Taille de police plus grande pour le titre
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
                    // Image de la carte
                    Image(
                        painter = rememberAsyncImagePainter(data.mapUrl),
                        contentDescription = "Carte du pays",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp) // Taille de l'image plus grande
                            .clip(RoundedCornerShape(16.dp)) // Coins plus arrondis
                    )

                    // Espacement entre l'image et les informations
                    Spacer(modifier = Modifier.height(24.dp))

                    // Informations du pays : Nom, capitale, langue, etc.
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = data.name,
                            fontSize = 32.sp, // Augmenter la taille du nom du pays
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRow(label = "Capitale:", value = data.capital)
                        InfoRow(label = "Langue:", value = data.language)
                        InfoRow(label = "Population:", value = data.population)
                        InfoRow(label = "Devise:", value = data.currency)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Affichage du drapeau
                        Image(
                            painter = rememberAsyncImagePainter(data.flagUrl),
                            contentDescription = "Drapeau",
                            modifier = Modifier
                                .size(80.dp)
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
                                    painter = rememberAsyncImagePainter(data.landmarkUrl),
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

                        // Boutons d'actions
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { /* Action explorer plus */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                            ) {
                                Text("Explorer plus", color = Color.White)
                            }

                            Button(
                                onClick = { /* Action planifier voyage */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                            ) {
                                Text("Planifier mon voyage", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp) // Augmenter l'espacement entre les lignes
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
    // Créer un viewModel pour la preview
    val viewModel: CountryViewModel = viewModel()
    // Passer un ensemble d'intérêts d'exemple
    CountryScreen(viewModel = viewModel, selectedInterests = listOf("Nature", "Aventure"))
}
