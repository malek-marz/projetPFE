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
import com.example.testapp.features.USER.user
import com.example.testapp.features.profileUser.ProfileUserScreen.Companion.profileUserScreenRoute
import com.example.testapp.viewmodels.Review
import com.google.firebase.auth.FirebaseAuth
import com.example.testapp.features.Home.model.UserReview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.collectAsState
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
            val friendSuggestions by viewModel.friendSuggestions.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val errorMessage by viewModel.errorMessage.collectAsState()
            val notes by viewModel.notes.collectAsState()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val searchResults by viewModel.searchResults.collectAsState()
            val showSearchResults by viewModel.showSearchResults.collectAsState()

            var showPopup by remember { mutableStateOf(false) }
            var interestsExpanded by remember { mutableStateOf(false) }
            var showCountryInput by remember { mutableStateOf(false) }
            var enteredCountry by remember { mutableStateOf("") }
            var showSearchDialog by remember { mutableStateOf(false) }

            LaunchedEffect(currentUserEmail) {
                viewModel.loadFriendSuggestionsByEmail(currentUserEmail)
                viewModel.loadNotes(currentUserEmail, emptyList())
                viewModel.loadReviews()
                viewModel.loadUsers()
            }

            if (showSearchResults) {
                SearchResultsScreen(
                    results = searchResults,
                    onBack = { viewModel.clearSearch() },
                    onUserSelected = { userId ->
                        navController.navigate("user_profile/$userId")
                    }
                )
            } else {
                Scaffold(
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("JourneyBuddy", color = Color.White)
                                        if (notes.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "📌 " + notes.first().note.take(20) + "...",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier
                                                    .background(Color(0xFF1976D2), shape = RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    IconButton(onClick = {
                                        FirebaseAuth.getInstance().signOut()
                                        navController.navigate("LoginScreen") {
                                            popUpTo(0) // Supprime toute la backstack
                                        }
                                    }) {
                                        Icon(Icons.Default.Logout, contentDescription = "Déconnexion", tint = Color.White)
                                    }

                                    IconButton(onClick = { showPopup = true }) {
                                        Icon(Icons.Default.Message, contentDescription = "Notes", tint = Color.White)
                                    }

                                    IconButton(onClick = {
                                        navController.navigate(profileUserScreenRoute)
                                    }) {
                                        Icon(Icons.Default.AccountCircle, contentDescription = "Profil utilisateur", tint = Color.White)
                                    }

                                    IconButton(onClick = { showSearchDialog = true }) {
                                        Icon(Icons.Default.Search, contentDescription = "Recherche", tint = Color.White)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3))
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
                    floatingActionButton = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.End,
                        ) {
                            Button(
                                onClick = { interestsExpanded = !interestsExpanded },
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Flight, contentDescription = "Avion", tint = Color.White)
                            }

                            if (interestsExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        interestsExpanded = false
                                        navController.navigate("profile_screen")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                ) {
                                    Text("Chercher un pays", color = Color.White)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        interestsExpanded = false
                                        showCountryInput = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                ) {
                                    Text("t'as déjà un pays", color = Color.White)
                                }
                                if (showCountryInput) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextField(
                                        value = enteredCountry,
                                        onValueChange = { enteredCountry = it },
                                        label = { Text("Entrez un pays") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (userId.isNotEmpty()) {
                                                viewModel.saveSelectedCountry(
                                                    pays = enteredCountry,
                                                    userId = userId,
                                                    onSuccess = {
                                                        showCountryInput = false
                                                    },
                                                    onFailure = { e ->
                                                        Log.e("Compose", "Erreur sauvegarde pays : ${e.message}")
                                                    }
                                                )
                                            } else {
                                                Log.e("Compose", "Utilisateur non connecté")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Text("Sauvegarder", color = Color.White)
                                    }
                                }
                            }
                            myBottomNavigationBar(navController)
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(Color(0xFFBBDEFB))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Suggestions d'amis",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyRow(modifier = Modifier.fillMaxWidth()) {
                                if (isLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                        }
                                    }
                                } else if (friendSuggestions.isEmpty()) {
                                    item {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .width(120.dp)
                                        ) {
                                            Button(
                                                onClick = { navController.navigate(user.ROUTE) },
                                                shape = CircleShape,
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                                modifier = Modifier.size(64.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Icon(Icons.Default.AccountCircle, contentDescription = "Changer critères", tint = Color.White)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Changer vos critères", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                        }
                                    }
                                    item {
                                        Text("No suggestions available", modifier = Modifier.padding(16.dp))
                                    }
                                } else {
                                    items(friendSuggestions) { friend ->
                                        FriendSuggestionCard(
                                            friend = friend,
                                            onAddFriend = { email -> viewModel.addFriend(currentUserEmail, email) },
                                            onDismiss = {},
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    item {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                                .width(260.dp)
                                        ) {
                                            Text(
                                                text = "Tu peux changer tes critères pour découvrir de nouveaux amis.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            IconButton(
                                                onClick = {},
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PersonAdd,
                                                    contentDescription = "Changer critères",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Avis des utilisateurs",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (viewModel.reviews.isEmpty()) {
                                    item {
                                        Text(
                                            "No reviews available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
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
                                    viewModel.saveNote(noteText = note, userEmail = currentUserEmail)
                                    viewModel.loadNotes(currentUserEmail, emptyList())
                                    showPopup = false
                                }
                            )
                        }

                        if (showSearchDialog) {
                            SearchDialog(
                                onDismiss = { showSearchDialog = false },
                                onSearch = { country, _, ageRange, gender ->
                                    viewModel.searchUsers(
                                        selectedCountry = country,
                                        selectedgender = gender,
                                        selectedMinAge = ageRange?.first,
                                        selectedMaxAge = ageRange?.last,
                                        onResult = { users ->
                                        },
                                        onError = { errorMsg ->
                                            // Handle error
                                        }
                                    )
                                    showSearchDialog = false
                                },
                                onClear = {
                                    viewModel.clearSearch()
                                }
                            )
                        }
                    }
                }
            }
        }
        // Nouveaux composants ajoutés à la fin (sans modifier les existants)
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun SearchResultsScreen(
            results: List<User>,
            onBack: () -> Unit,
            onUserSelected: (String) -> Unit
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Résultats de recherche") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3)))
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
                                onUserClicked = { onUserSelected(user.userId) }
                            )
                        }
                    }
                }
            }
        }

        @Composable
        fun EmptyResultsMessage() {
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
        fun UserSearchResultItem(user: User, onUserClicked: () -> Unit) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onUserClicked),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

                            user.language?.let {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = "Langue",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    it,
                                    modifier = Modifier.padding(start = 4.dp),
                                    style = MaterialTheme.typography.bodySmall
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
    fun NotePopup(onDismiss: () -> Unit, onNoteSaved: (String) -> Unit) {
        var noteText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Write a Note") },
            text = {
                Column {
                    Text("Share a note about your journey!")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("e.g., Just visited Tokyo 🇯🇵") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onNoteSaved(noteText)
                        onDismiss()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }




    @Composable
    fun myBottomNavigationBar(navController: NavController) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = Color(0xFF2196F3),
            modifier = Modifier.fillMaxWidth().offset(x = 16.dp),
        ) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent, // Pour que Surface colore le fond
                tonalElevation = 0.dp // déjà géré par Surface
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White) },
                    label = { Text("Home", color = Color.White) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("chat") },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color.White) },
                    label = { Text("Chat", color = Color.White) }
                )
            }
        }

    }
    @Composable
    fun SearchDialog(
        onDismiss: () -> Unit,
        onSearch: (String?, String?, IntRange?, String?) -> Unit,
        onClear: () -> Unit
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
            title = { Text("Rechercher des utilisateurs") },
            text = {
                Column {
                    // Pays
                    DropdownSelector("Choisir un pays", countries, selectedCountry) {
                        selectedCountry = it
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Langue
                    DropdownSelector("Choisir une langue", languages, selectedLanguage) {
                        selectedLanguage = it
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Âge
                    Text("Âge entre ${ageRange.first} et ${ageRange.last}")
                    RangeSlider(
                        value = ageRange.first.toFloat()..ageRange.last.toFloat(),
                        onValueChange = {
                            ageRange = it.start.toInt()..it.endInclusive.toInt()
                        },
                        valueRange = 18f..60f
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sexe
                    DropdownSelector("Choisir un sexe", sexes, selectedgender) {
                        selectedgender = it
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSearch(selectedCountry, selectedLanguage, ageRange, selectedgender)
                    }
                ) {
                    Text("Rechercher")
                }
            },
            dismissButton = {
                Row {
                    OutlinedButton(onClick = {
                        onClear()
                        selectedCountry = null
                        selectedLanguage = null
                        selectedgender = null
                        ageRange = 20..35
                    }) {
                        Text("Effacer")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                }
            }
        )
    }

    @Composable
    fun ReviewCard(
        userReview: UserReview,
        onRatingChange: (Int) -> Unit,  // callback quand l'utilisateur clique une étoile
        modifier: Modifier = Modifier
    ) {
        fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("dd MMM yyyy à HH:mm", Locale.getDefault())
            return format.format(date)
        }

        var currentRating by remember { mutableStateOf(userReview.rating) }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Nom de l'utilisateur
                Text(
                    text = userReview.ownerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Étoiles interactives
                RatingStars(
                    rating = currentRating,
                    onRatingSelected = { newRating ->
                        currentRating = newRating
                        onRatingChange(newRating)  // Notifie le changement à la ViewModel ou autre
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Texte de la review
                Text(
                    text = userReview.review,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp
                Text(
                    text = "Posté le : ${formatTimestamp(userReview.timestamp)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    @Composable
    fun RatingStars(rating: Int, onRatingSelected: (Int) -> Unit) {
        Row {
            repeat(5) { index ->
                val icon = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onRatingSelected(index + 1) }  // clic sur l'étoile
                )
            }
        }
    }
    @Composable
    fun DropdownSelector(
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

    @Composable
    fun UserSearchResultItem(user: User, onUserClicked: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onUserClicked() },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profil",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user.firstName ?: "Utilisateur inconnu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (!user.language.isNullOrEmpty()) {
                        Text(
                            text = "Langue: ${user.language}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (!user.country.isNullOrEmpty()) {
                        Text(
                            text = "Pays: ${user.country}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }





    @Preview(showBackground = true)
    @Composable
    fun HomeScreenPreview() {
        val navController = rememberNavController()
        HomeScreen.HomeScreen(navController = navController, currentUserEmail = "test@example.com")
    }
