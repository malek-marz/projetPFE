package com.example.testapp.features.USER

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testapp.features.Buddys.Buddy

class User {
    companion object {

        private val criteriaList = listOf(
            "Aime voyager", "Non-fumeur", "Parle anglais", "Flexible", "Végétarien(ne)",
            "Sportif(ve)", "Respectueux(se)", "Sociable", "Ponctuel(le)", "Organisé(e)",
            "Ouvert(e) d’esprit", "Amoureux(se) de la nature", "Expérimenté(e) en voyage",
            "Prudent(e)", "Aime la musique", "Aventureux(se)"
        )

        @Composable



        fun user(navController: NavController, viewModel: userViewModel = viewModel()) {
            val selectedCriteria by viewModel.selectedCriteria.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White) // 👈 Fond blanc
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Sélectionnez vos critères :",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(120.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(criteriaList) { criterion ->
                            val isSelected = selectedCriteria.contains(criterion)
                            FilterChip(
                                text = criterion,
                                isSelected = isSelected,
                                onClick = { viewModel.toggleCriterion(criterion) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val userId = "monUserId123"

                            viewModel.saveCriteriaForUser(
                                userId = userId,
                                onSuccess = {
                                    println("Critères enregistrés avec succès !")
                                    // navController.navigate(...) si besoin
                                },
                                onError = { e ->
                                    println("Erreur d'enregistrement : ${e.message}")
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(text = "Valider")
                    }
                }
            }
        }


        @Composable
        fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
            Surface(
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onClick() }
            ) {
                Text(
                    text = text,
                    color = if (isSelected) Color.White else Color.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(device = "id:Nexus S")
@Composable
private fun userPreviewPhone() {
    val navController = rememberNavController()
    User.user(navController)
}

