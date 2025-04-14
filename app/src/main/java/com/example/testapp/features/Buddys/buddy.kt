package com.example.testapp.features.Buddys

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState

class buddys {
    companion object {
        const val buddyRoute = "buddy"

        @Composable
        fun buddy(navController: NavController, viewModel: buddyViewModel = viewModel()) {
            val state by viewModel.state.collectAsState()

            // Sexe
            val sexOptions = listOf("Homme", "Femme", "Peu importe")
            var selectedSex by remember { mutableStateOf("Choisir le sexe") }
            var sexExpanded by remember { mutableStateOf(false) }

            // Âge
            val ageOptions = (18..90).toList()
            val selectedAges = remember { mutableStateMapOf<Int, Boolean>() }
            var ageExpanded by remember { mutableStateOf(false) }
            ageOptions.forEach { selectedAges.putIfAbsent(it, false) }

            val selectedAgeText = selectedAges
                .filter { it.value }
                .keys
                .sorted()
                .joinToString(", ")
                .ifEmpty { "Choisir les âges" }

            // Descriptions / Critères recherchés
            val descriptionOptions = listOf(
                "Aime voyager", "Non-fumeur", "Parle anglais", "Flexible", "Végétarien(ne)",
                "Sportif(ve)", "Respectueux(se)", "Sociable", "Ponctuel(le)", "Organisé(e)",
                "Ouvert(e) d’esprit", "Amoureux(se) de la nature", "Expérimenté(e) en voyage",
                "Prudent(e)", "Aime la musique", "Aventureux(se)"
            )
            val selectedDescriptions = remember { mutableStateMapOf<String, Boolean>() }
            descriptionOptions.forEach { selectedDescriptions.putIfAbsent(it, false) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF1F1F1) // Un fond clair avec un léger contraste
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Titre
                    Text(
                        text = "Trouvez un Partenaire",
                        style = MaterialTheme.typography.h5.copy(fontSize = 15.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp), // Réduit l'espace en haut
                        color = MaterialTheme.colors.primary,
                        textAlign = TextAlign.Center
                    )

                    // Sexe préféré
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { sexExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text("Sexe préféré: $selectedSex", style = MaterialTheme.typography.body1)
                        }
                    }

                    DropdownMenu(
                        expanded = sexExpanded,
                        onDismissRequest = { sexExpanded = false }
                    ) {
                        sexOptions.forEach { option ->
                            DropdownMenuItem(onClick = {
                                selectedSex = option
                                sexExpanded = false
                            }) {
                                Text(option)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Âges préférés
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { ageExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text("Âges préférés: $selectedAgeText", style = MaterialTheme.typography.body1)
                        }
                    }

                    DropdownMenu(
                        expanded = ageExpanded,
                        onDismissRequest = { ageExpanded = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        ageOptions.forEach { age ->
                            DropdownMenuItem(onClick = {
                                selectedAges[age] = !(selectedAges[age] ?: false)
                            }) {
                                Row {
                                    Checkbox(
                                        checked = selectedAges[age] ?: false,
                                        onCheckedChange = null
                                    )
                                    Text("$age ans", modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Critères recherchés sous forme de boutons
                    Text("Critères recherchés", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(descriptionOptions.size) { index ->
                            val desc = descriptionOptions[index]
                            FilterButton(
                                desc = desc,
                                isSelected = selectedDescriptions[desc] ?: false,
                                onClick = {
                                    selectedDescriptions[desc] =
                                        !(selectedDescriptions[desc] ?: false)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bouton de validation
                    Button(
                        onClick = {
                            val selectedAgesList = selectedAges.filter { it.value }.keys.sorted()
                            val selectedCriteriaSet = selectedDescriptions.filter { it.value }.keys.toSet()

                            // Appeler la fonction du ViewModel pour trouver des utilisateurs compatibles
                            viewModel.findCompatibleUsers(
                                selectedSex = selectedSex,
                                selectedAges = selectedAgesList,
                                selectedCriteria = selectedCriteriaSet
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text("Valider")
                    }
                }
            }
        }

        @Composable
        fun FilterButton(desc: String, isSelected: Boolean, onClick: () -> Unit) {
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colors.primary else Color(0xFFE0E0E0),
                animationSpec = tween(durationMillis = 300)
            )

            val textColor = if (isSelected) Color.White else Color.Black

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onClick() },
                color = backgroundColor,
                elevation = 6.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = desc,
                        color = textColor,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}

@Preview(device = "id:Nexus S")
@Composable
private fun buddyPreviewPhone() {
    val navController = rememberNavController()
    buddys.buddy(navController)
}
