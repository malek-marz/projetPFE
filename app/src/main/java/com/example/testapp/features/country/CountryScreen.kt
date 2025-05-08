package com.example.journeybuddy.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journeybuddy.ui.viewmodels.CountryViewModel
import com.example.testapp.R

@Composable
fun CountryScreen(
    interests: List<String> = listOf("nature", "camping", "randonnée"),
    viewModel: CountryViewModel = viewModel() // Utilisation de CountryViewModel
) {
    // Obtenir les pays du ViewModel
    val countries by viewModel.countries
    var loading by remember { mutableStateOf(false) } // Indicateur de chargement

    // Sélection du pays
    val selectedCountry = remember { mutableStateOf("") }

    // Charger les pays en fonction des intérêts
    LaunchedEffect(interests) {
        loading = true
        viewModel.getCountriesFromApi(interests)
        loading = false
    }

    // Colonne principale contenant tout le contenu de l'écran
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFE3F2FD)) // Fond bleu clair
    ) {
        // Titre principal
        Text(
            text = "Votre pays est : ${selectedCountry.value}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D47A1), // Couleur du texte bleu foncé
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Affichage de l'indicateur de chargement si les pays sont vides ou en chargement
        if (loading) {
            CircularProgressIndicator() // Afficher un indicateur de chargement si les pays sont vides
        } else if (countries.isEmpty()) {
            Text("Aucun pays trouvé", fontSize = 18.sp, color = Color.Gray)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                items(countries) { country ->
                    CountryItem(
                        country = country,
                        selected = selectedCountry.value == country,
                        onClick = { selectedCountry.value = country }
                    )
                }
            }
        }

        // Bouton pour confirmer la sélection
        Button(
            onClick = { /* Action à réaliser après la sélection */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5)), // Couleur bleue
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Explorer", fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
fun CountryItem(country: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) Color(0xFF2196F3) else Color.White

    // Card avec une bordure et un fond coloré si sélectionné
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Lorsqu'on clique sur un pays, on le sélectionne
            .padding(vertical = 8.dp), // Ajouter de l'espace vertical entre les cartes
        elevation = CardDefaults.cardElevation(4.dp), // Ombre légère
        shape = RoundedCornerShape(16.dp), // Coins arrondis
        colors = CardDefaults.cardColors(containerColor = backgroundColor) // Couleur de fond dynamique
    ) {
        // Contenu de chaque élément, ici le nom du pays et une photo
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image du pays (tu peux remplacer cette ressource par l'image réelle du pays)
            /*Image(
                painter = painterResource(id = R.drawable.ic_country_image), // Remplace par l'image correspondante
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )*/

            Spacer(modifier = Modifier.width(16.dp))

            // Nom du pays
            Text(
                text = country,
                fontSize = 18.sp,
                color = if (selected) Color.White else Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCountryScreen() {
    CountryScreen()
}
