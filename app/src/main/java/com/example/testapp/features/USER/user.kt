package com.example.testapp.features.USER

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import userViewModel

data class CriteriaCategory(val title: String, val criteria: List<String>)

class user {
    companion object {
        const val ROUTE = "UserSelectionScreen"

        private val criteriaCategories = listOf(
            CriteriaCategory("🌍 Style de voyage", listOf(
                "Aventurier", "Voyage organisé", "Road trip", "Nature", "Ville",
                "Voyage économique", "Voyage de luxe", "Auberge", "Hôtel", "Local"
            )),
            CriteriaCategory("🧠 Personnalité & Valeurs", listOf(
                "Respectueux", "Tolérant", "Curieux", "Sociable", "Introverti",
                "Ouvert d’esprit", "Flexible", "Organisé", "Ponctuel", "Prudent"
            )),
            CriteriaCategory("🧘‍♀️ Habitudes de vie", listOf(
                "Non-fumeur", "Végétarien", "Végane", "Cuisiner", "Couche-tôt",
                "Couche-tard", "Sportif", "Détente", "Marcheur", "Accro au café"
            )),
            CriteriaCategory("🎧 Centres d’intérêt", listOf(
                "Musique", "Cinéma", "Photo", "Randonnée", "Musées",
                "Gastronomie", "Plage", "Festivals", "Lecture", "Histoire"
            )),
            CriteriaCategory("🗣️ Langues & Communication", listOf(
                "Anglais", "Espagnol", "Français", "Polyglotte", "Communicatif", "Discret", "Rencontres locales"
            ))
        )

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun user(navController: NavController, viewModel: userViewModel = viewModel()) {
            val selectedCriteria by viewModel.selectedCriteria.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                currentUser?.uid?.let { viewModel.loadCriteriaForUser(it) }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Critères de voyage", style = MaterialTheme.typography.titleLarge) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White
                        )
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                LazyColumn(
                    contentPadding = paddingValues,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Text(
                            text = "Sélectionnez les critères qui vous correspondent pour trouver un partenaire de voyage compatible.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    criteriaCategories.forEach { category ->
                        item {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(130.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 250.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                userScrollEnabled = false
                            ) {
                                items(category.criteria) { criterion ->
                                    val isSelected = selectedCriteria.contains(criterion)
                                    FilterChip(
                                        text = criterion,
                                        isSelected = isSelected,
                                        onClick = { viewModel.toggleCriterion(criterion) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val user = FirebaseAuth.getInstance().currentUser
                                user?.uid?.let { uid ->
                                    viewModel.saveCriteriaForUser(
                                        userId = uid,
                                        onSuccess = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("✅ Critères enregistrés !")
                                                navController.navigate("ProfileUserScreen") {
                                                    popUpTo("UserSelectionScreen") { inclusive = true }
                                                }
                                            }
                                        },
                                        onError = { e ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("❌ Erreur : ${e.message}")
                                            }
                                        }
                                    )
                                } ?: coroutineScope.launch {
                                    snackbarHostState.showSnackbar("❗ Utilisateur non connecté.")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(
                                text = "Valider mes critères",
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                            )
                        }
                    }
                }
            }
        }

        @Composable
        fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
            Surface(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                tonalElevation = if (isSelected) 2.dp else 0.dp,
                shadowElevation = 1.dp,
                modifier = Modifier.clickable { onClick() }
            ) {
                Text(
                    text = text,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserPreview() {
    val navController = rememberNavController()
    user.user(navController)
}
