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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class CriteriaCategory(val title: String, val criteria: List<String>)

class User {
    companion object {

        private val criteriaCategories = listOf(
            CriteriaCategory("🧠 Personnalité & Valeurs", listOf(
                "Respectueux", "Tolérant", "Curieux", "Sociable", "Introverti",
                "Extraverti", "Ouvert d’esprit", "Flexible", "Organisé", "Ponctuel",
                "Prudent", "Calme", "Optimiste", "Réfléchi", "Empathique"
            )),
            CriteriaCategory("🧘‍♀️ Habitudes & Rythme", listOf(
                "Non-fumeur", "Fumeur", "Couche-tôt", "Couche-tard", "Sportif",
                "Détente", "Marcheur", "Amateur de café", "Noctambule", "Matinal",
                "Flexible"
            )),
            CriteriaCategory("🗣️ Communication", listOf(
                "Communicatif", "Discret", "Humoristique", "Direct", "Patient",
                "Calme", "À l’écoute", "Ouvert aux discussions", "Respectueux"
            )),
            CriteriaCategory("👫 Qualités d’un(e) ami(e) de voyage", listOf(
                "Calme", "Empathique", "Humoristique", "Curieux", "Respectueux",
                "Partageur", "Compromis", "Spontané", "Fiable", "Motivant",
                "Organisé", "Patient", "Adaptable", "Optimiste", "Collaboratif",
                "Soutenant", "Ouvert aux imprévus", "Bienveillant", "Autonome"
            )),
            CriteriaCategory("🌍 Langue", listOf(
                "Anglais", "Français", "Espagnol", "Allemand", "Italien",
                "Chinois", "Japonais", "Russe", "Arabe", "Portugais"
            ))
        )

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun user(navController: NavController, viewModel: userViewModel = viewModel()) {
            val selectedCriteria by viewModel.selectedCriteria.collectAsState()
            val selectedLanguages by viewModel.selectedLanguages.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                currentUser?.uid?.let { viewModel.loadCriteriaForUser(it) }
            }

            val blueColorScheme = MaterialTheme.colorScheme.copy(
                primary = Color(0xFF1E88E5),
                onPrimary = Color.White,
                surfaceVariant = Color(0xFFE3F2FD),
                onSurface = Color(0xFF0D47A1)
            )

            MaterialTheme(colorScheme = blueColorScheme) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("préférences partenaire de voyage", style = MaterialTheme.typography.titleLarge) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },

                    // ✅ Bouton fixé en bas
                    bottomBar = {
                        Button(
                            onClick = {
                                if (selectedLanguages.isEmpty()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("❗ Sélectionnez au moins une langue.")
                                    }
                                    return@Button
                                }

                                val user = FirebaseAuth.getInstance().currentUser
                                user?.uid?.let { uid ->
                                    viewModel.saveCriteriaForUser(
                                        userId = uid,
                                        onSuccess = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("✅ préférences enregistrés !")
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
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(
                                text = "Valider mes préférences",
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                            )
                        }
                    }
                ) { paddingValues ->
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding(),
                            bottom = 100.dp // espace pour ne pas cacher le contenu par le bouton
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            Text(
                                text = "Choisissez les mots qui décrivent le mieux votre partenaire de voyage idéal.",
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
                                        val isSelected = if (category.title == "🌍 Langue")
                                            selectedLanguages.contains(criterion)
                                        else
                                            selectedCriteria.contains(criterion)

                                        FilterChip(
                                            text = criterion,
                                            isSelected = isSelected,
                                            onClick = {
                                                if (category.title == "🌍 Langue") {
                                                    viewModel.toggleLanguage(criterion)
                                                } else {
                                                    viewModel.toggleCriterion(criterion)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
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
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
