// Imports principaux (inchangés)
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testapp.viewmodels.Review
import com.google.firebase.auth.FirebaseAuth
import com.example.testapp.features.Home.model.UserReview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

class HomeScreen {
    companion object {
        const val HomeScreenRoute = "HomeScreen"

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun HomeScreen(
            navController: NavController,
            currentUserEmail: String,
            viewModel: Review.HomeViewModel = viewModel()
        ) {
            // States et données (inchangés)
            val friendSuggestions by viewModel.friendSuggestions.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val errorMessage by viewModel.errorMessage.collectAsState()
            val notes by viewModel.notes.collectAsState()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val searchResults by viewModel.searchResults.collectAsState()
            val showSearchResults by viewModel.showSearchResults.collectAsState()

            var showPopup by remember { mutableStateOf(false) }
            var showCountryInput by remember { mutableStateOf(false) }
            var enteredCountry by remember { mutableStateOf("") }
            var showSearchDialog by remember { mutableStateOf(false) }

            // Palette de couleurs professionnelle
            val primaryColor = Color(0xFF1976D2)
            val secondaryColor = Color(0xFF2196F3)
            val backgroundColor = Color(0xFFF5F5F5)
            val cardColor = Color.White
            val textColorPrimary = Color(0xFF333333)
            val textColorSecondary = Color(0xFF666666)
            val noteColor = Color(0xFF8A2BE2) // Couleur mauve pour les notes

            LaunchedEffect(currentUserEmail) {
                viewModel.loadFriendSuggestionsByEmail(currentUserEmail)
                viewModel.loadNotes(currentUserEmail, emptyList())
                viewModel.loadReviews()
                viewModel.loadUsers()
            }

            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    primary = primaryColor,
                    secondary = secondaryColor,
                    background = backgroundColor
                )
            ) {
                if (showSearchResults) {
                    SearchResultsScreen(
                        results = searchResults,
                        onBack = { viewModel.clearSearch() },
                        onUserSelected = { userId ->
                            navController.navigate("ProfileUserScreen")
                        },
                        onAddFriend = { user ->
                            user.email?.let {
                                viewModel.addFriend(it)
                                viewModel.removeUserFromSearchResults(user)
                            } ?: Log.e("AddFriend", "Email de l'utilisateur introuvable")
                        },
                        onIgnore = { user ->
                            viewModel.ignoreUser(user.userId)
                            viewModel.removeUserFromSearchResults(user)
                        }
                    )
                } else {
                    Scaffold(
                        containerColor = backgroundColor,
                        topBar = {
                            Column {
                                TopAppBar(
                                    title = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "JourneyBuddy",
                                                color = Color.White,
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            if (notes.isNotEmpty()) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Badge(
                                                    containerColor = Color.White,
                                                    contentColor = primaryColor
                                                ) {
                                                    Text(
                                                        text = "📌 " + notes.first().note.take(20) + "...",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    actions = {
                                        IconButton(onClick = {
                                            FirebaseAuth.getInstance().signOut()
                                            navController.navigate("LoginScreen") {
                                                popUpTo(0)
                                            }
                                        }) {
                                            Icon(
                                                Icons.Default.Logout,
                                                contentDescription = "Déconnexion",
                                                tint = Color.White
                                            )
                                        }

                                        IconButton(onClick = { showPopup = true }) {
                                            Icon(
                                                Icons.Default.Message,
                                                contentDescription = "Notes",
                                                tint = Color.White
                                            )
                                        }

                                        IconButton(onClick = {
                                            navController.navigate("ProfileUserScreen")
                                        }) {
                                            Icon(
                                                Icons.Default.AccountCircle,
                                                contentDescription = "Profil utilisateur",
                                                tint = Color.White
                                            )
                                        }

                                        IconButton(onClick = { showSearchDialog = true }) {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = "Recherche",
                                                tint = Color.White
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = primaryColor
                                    )
                                )

                                if (notes.isNotEmpty()) {
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(notes) { note ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .width(120.dp)
                                                    .padding(4.dp)
                                            ) {
                                                // 🏷️ Pays actuel
                                                Text(
                                                    text = note.location ?: "Pays inconnu",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color.DarkGray,
                                                    modifier = Modifier
                                                        .background(
                                                            color = Color(0xFFE0F7FA),
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                                )

                                                Spacer(modifier = Modifier.height(6.dp))

                                                // 💬 Bulle de note
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFFD0E8FF),
                                                    shadowElevation = 4.dp,
                                                    modifier = Modifier.size(90.dp)
                                                ) {
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(10.dp)
                                                    ) {
                                                        Text(
                                                            text = note.note.take(40),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.Black,
                                                            maxLines = 4,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                // 👤 Nom de l'utilisateur
                                                Text(
                                                    text = note.ownerName ?: "ownerName",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            BottomNavigationBar(navController)
                        }
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .background(backgroundColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Suggestions d'amis",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textColorPrimary
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (isLoading) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.padding(16.dp),
                                                    color = primaryColor
                                                )
                                            }
                                        }
                                    } else if (friendSuggestions.isEmpty()) {
                                        item {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(8.dp)
                                            ) {
                                                ElevatedCard(
                                                    modifier = Modifier
                                                        .width(200.dp)
                                                        .padding(8.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = cardColor
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(16.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Icon(
                                                            Icons.Default.PersonSearch,
                                                            contentDescription = null,
                                                            tint = primaryColor,
                                                            modifier = Modifier.size(48.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            "Aucune suggestion",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = textColorSecondary
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Button(
                                                            onClick = { navController.navigate("User") },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = primaryColor
                                                            ),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("Modifier critères")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        items(friendSuggestions) { friend ->
                                            FriendSuggestionCard(
                                                friend = friend,
                                                onAddFriend = { email ->
                                                    viewModel.addFriend(email)
                                                    viewModel.removeFriendSuggestion(email)
                                                },
                                                onDismiss = { viewModel.removeFriendSuggestion(friend.email) },
                                                onProfileClick = {
                                                    navController.navigate("UserProfile/${friend.userId}")
                                                }
                                            )
                                        }
                                        // Ajout de la carte pour modifier les critères à la fin
                                        item {
                                            ElevatedCard(
                                                modifier = Modifier
                                                    .width(200.dp)
                                                    .padding(8.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = cardColor
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Icon(
                                                        Icons.Default.Tune,
                                                        contentDescription = "Modifier critères",
                                                        tint = primaryColor,
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        "Changer préferences",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = textColorSecondary
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Button(
                                                        onClick = { navController.navigate("User") },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = primaryColor
                                                        ),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Modifier")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "Avis récents",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textColorPrimary
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                if (viewModel.reviews.isEmpty()) {
                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = cardColor
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.StarOutline,
                                                contentDescription = null,
                                                tint = textColorSecondary,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Aucun avis disponible",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = textColorSecondary
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(viewModel.reviews) { review ->
                                            ReviewCard(
                                                userReview = review,
                                                onRatingChange = { newRating ->
                                                    viewModel.updateReviewRating(review, newRating)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (showPopup) {
                                NotePopup(
                                    onDismiss = { showPopup = false },
                                    onNoteSaved = { note ->
                                        viewModel.saveNote(
                                            noteText = note,
                                            userEmail = currentUserEmail
                                        )
                                        viewModel.loadNotes(currentUserEmail, emptyList())
                                        showPopup = false
                                    }
                                )
                            }

                            if (showSearchDialog) {
                                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                                SearchDialog(
                                    onDismiss = { showSearchDialog = false },
                                    onSearch = { country, languages, ageRange, gender ->
                                        if (currentUserId != null) {
                                            viewModel.searchUsers(
                                                currentUserId = currentUserId,
                                                selectedCountry = country,
                                                selectedgender = gender,
                                                selectedMinAge = ageRange?.first,
                                                selectedMaxAge = ageRange?.last,
                                                selectedLanguage = languages,
                                                onResult = { /* handle result */ },
                                                onError = { errorMsg -> /* handle error */ }
                                            )
                                        }
                                        showSearchDialog = false
                                    },
                                    onClear = {
                                        viewModel.clearSearch()
                                        showSearchDialog = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        @Composable
        private fun FriendSuggestionCard(
            friend: User,
            onAddFriend: (String) -> Unit,
            onDismiss: () -> Unit,
            onProfileClick: () -> Unit,
            modifier: Modifier = Modifier
        ) {
            ElevatedCard(
                modifier = modifier
                    .width(200.dp)
                    .clickable(onClick = onProfileClick),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1976D2).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = friend.firstName?.take(1)?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF1976D2)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = friend.firstName ?: "Utilisateur",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    friend.country?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = { friend.email?.let(onAddFriend) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF1976D2), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = "Ajouter",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.LightGray, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Ignorer",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        @Composable
        private fun ReviewCard(
            userReview: UserReview,
            onRatingChange: (Int) -> Unit,
            modifier: Modifier = Modifier
        ) {
            ElevatedCard(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1976D2).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                userReview.ownerName.take(1).uppercase(),
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                userReview.ownerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                "Avis sur ${userReview.countryName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= userReview.rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Star $i",
                                tint = if (i <= userReview.rating) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { onRatingChange(i) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        userReview.review,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Posté le ${formatTimestamp(userReview.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF666666),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        @Composable
        private fun BottomNavigationBar(navController: NavController) {
            var expanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                NavigationBar(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp)),
                    containerColor = Color(0xFF1976D2),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { expanded = true },
                        icon = {
                            Box {
                                Icon(
                                    Icons.Default.Flight,
                                    contentDescription = "Options voyage",
                                    tint = Color.White
                                )

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text("Chercher un pays", color = Color.Black)
                                        },
                                        onClick = {
                                            expanded = false
                                            navController.navigate("profile_screen")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text("Déjà un pays", color = Color.Black)
                                        },
                                        onClick = {
                                            expanded = false
                                            navController.navigate("SelectCountryMapScreen")
                                        }
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                "Voyage",
                                color = Color.White
                            )
                        }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("Chs") },
                        icon = {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = "chat",
                                tint = Color.White
                            )
                        },
                        label = {
                            Text(
                                "Chat",
                                color = Color.White
                            )
                        }
                    )
                }
            }
        }

        @Composable
        private fun NotePopup(
            onDismiss: () -> Unit,
            onNoteSaved: (String) -> Unit
        ) {
            var noteText by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        "Nouvelle note",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            "Partagez une note sur votre voyage",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = {
                                Text("Ex: Visité Tokyo aujourd'hui, c'était incroyable !")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onNoteSaved(noteText)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) {
                        Text("Enregistrer")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Annuler")
                    }
                }
            )
        }

        @Composable
        private fun SearchDialog(
            onDismiss: () -> Unit,
            onSearch: (String?, String?, IntRange?, String?) -> Unit,
            onClear: () -> Unit,
            viewModel: Review.HomeViewModel = viewModel()
        ) {
            var selectedCountry by remember { mutableStateOf<String?>(null) }
            var selectedLanguage by remember { mutableStateOf<String?>(null) }
            var selectedgender by remember { mutableStateOf<String?>(null) }
            var ageRange by remember { mutableStateOf(20..35) }

            val countries = listOf("France", "USA", "Japan", "Canada", "UK")
            val languages = listOf("fr", "en", "es", "de", "it")
            val sexes = listOf("Homme", "Femme", "Autre")

            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        "Recherche avancée",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        DropdownSelector("Choisir un pays", countries, selectedCountry) {
                            selectedCountry = it
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        DropdownSelector("Choisir une langue", languages, selectedLanguage) {
                            selectedLanguage = it
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Âge entre ${ageRange.first} et ${ageRange.last}")
                        RangeSlider(
                            value = ageRange.first.toFloat()..ageRange.last.toFloat(),
                            onValueChange = {
                                ageRange = it.start.toInt()..it.endInclusive.toInt()
                            },
                            valueRange = 18f..60f
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DropdownSelector("Choisir un sexe", sexes, selectedgender) {
                            selectedgender = it
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onSearch(selectedCountry, selectedLanguage, ageRange, selectedgender)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) {
                        Text("Rechercher")
                    }
                },
                dismissButton = {
                    Row {
                        OutlinedButton(
                            onClick = {
                                onClear()
                                selectedCountry = null
                                selectedLanguage = null
                                selectedgender = null
                                ageRange = 20..35
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Effacer")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Annuler")
                        }
                    }
                }
            )
        }

        @Composable
        private fun DropdownSelector(
            label: String,
            options: List<String>,
            selectedOption: String?,
            onOptionSelected: (String) -> Unit
        ) {
            var expanded by remember { mutableStateOf(false) }

            Column {
                Text(text = label)
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedOption ?: label)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        private fun SearchResultsScreen(
            results: List<User>,
            onBack: () -> Unit,
            onAddFriend: (User) -> Unit,
            onIgnore: (User) -> Unit,
            onUserSelected: (String) -> Unit
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Résultats de recherche") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3))
                    )
                },
                containerColor = Color(0xFFBBDEFB)
            ) { padding ->
                if (results.isEmpty()) {
                    EmptyResultsMessage()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results) { user ->
                            UserSearchResultItem(
                                user = user,
                                onUserClicked = { onUserSelected(user.userId ?: "") },
                                onAddFriend = { onAddFriend(user) },
                                onIgnore = { onIgnore(user) }
                            )
                        }
                    }
                }
            }
        }

        @Composable
        private fun EmptyResultsMessage() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = "Aucun résultat",
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Aucun utilisateur trouvé",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Essayez de modifier vos critères de recherche",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        @Composable
        private fun UserSearchResultItem(
            user: User,
            onUserClicked: () -> Unit,
            onAddFriend: () -> Unit,
            onIgnore: () -> Unit
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onUserClicked),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.firstName?.take(1)?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "${user.firstName ?: "Utilisateur"} ${user.lastName ?: ""}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                user.country?.let {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Pays",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        it,
                                        modifier = Modifier.padding(start = 4.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                user.selectedLanguage?.takeIf { it.isNotEmpty() }
                                    ?.let { languages ->
                                        Icon(
                                            Icons.Default.Language,
                                            contentDescription = "Langues",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = languages,
                                            modifier = Modifier.padding(start = 4.dp),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAddFriend,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Ajouter")
                        }

                        OutlinedButton(
                            onClick = onIgnore,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFF44336)
                            )
                        ) {
                            Text("Ignorer")
                        }
                    }
                }
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("dd MMM yyyy à HH:mm", Locale.getDefault())
            return format.format(date)
        }

        @Preview(showBackground = true)
        @Composable
        fun HomeScreenPreview() {
            val navController = rememberNavController()
            HomeScreen(
                navController = navController,
                currentUserEmail = "test@example.com"
            )
        }
    }
}