package com.example.testapp.viewmodels

import User
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.features.Home.model.UserReview
import com.example.testapp.models.FriendSuggestion
import com.example.testapp.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.Query


import java.util.Calendar

data class Review(
        val uid: String = "",
        val email: String = "",
        val note: String = "",        // Tu peux remplacer String par Int si tu stockes un entier
        val description: String = "", // Ajoute ce champ si tu veux stocker une description texte
        val timestamp: Long = 0L,
        val ownerName: String? = null,
        val location: String? = null// stocke la valeur du champ "username"
) {


        class HomeViewModel : ViewModel() {

                private val TAG = "HomeViewModel"
                private val _reviews = mutableStateListOf<UserReview>()
                val reviews: List<UserReview> get() = _reviews


                private val repository = UserRepository(Firebase.firestore)

                private val _friendSuggestions =
                        MutableStateFlow<List<FriendSuggestion>>(emptyList())
                val friendSuggestions: StateFlow<List<FriendSuggestion>> =
                        _friendSuggestions

                private val _isLoading = MutableStateFlow(false)
                val isLoading: StateFlow<Boolean> = _isLoading

                private val _searchResults = MutableStateFlow<List<User>>(emptyList())
                val searchResults: StateFlow<List<User>> = _searchResults

                private val _showSearchResults = MutableStateFlow(false)
                val showSearchResults: StateFlow<Boolean> = _showSearchResults
                var searchQuery = mutableStateOf("")
                var selectedCountryFilter = mutableStateOf<String?>(null)
                var selectedTravelStyleFilter = mutableStateOf<String?>(null)

                private val _allUsers = MutableStateFlow<List<User>>(emptyList())
                val allUsers: StateFlow<List<User>> get() = _allUsers



                private val _errorMessage = MutableStateFlow<String?>(null)
                val errorMessage: StateFlow<String?> = _errorMessage

                private val _notes = MutableStateFlow<List<Review>>(emptyList())
                val notes: StateFlow<List<Review>> = _notes
                var pays: Map<String, Any>? = null
                private val _usersMap = MutableStateFlow<Map<String, User>>(emptyMap())
                fun logout(navigateToLogin: () -> Unit) {
                        viewModelScope.launch {
                                try {
                                        FirebaseAuth.getInstance().signOut()
                                        Log.d(TAG, "Utilisateur déconnecté. Nettoyage du ViewModel.") // Ligne ajoutée pour le log
                                        clearAllUserData() // <-- APPEL DE LA NOUVELLE FONCTION ICI
                                        navigateToLogin()
                                } catch (e: Exception) {
                                        _errorMessage.value = "Échec de la déconnexion: ${e.message}"
                                        Log.e(TAG, "Logout failed", e)
                                }
                        }
                }

                fun loadFriendSuggestionsByEmail(currentUserEmail: String) {
                        Log.d(
                                TAG,
                                "Chargement des suggestions pour l'email : $currentUserEmail"
                        )
                        viewModelScope.launch {
                                _isLoading.value = true
                                try {
                                        val userId =
                                                repository.getUserIdByEmail(currentUserEmail)
                                        Log.d(TAG, "ID utilisateur récupéré : $userId")

                                        if (userId == null) {
                                                _errorMessage.value =
                                                        "Utilisateur non trouvé"
                                                _friendSuggestions.value = emptyList()
                                                Log.w(
                                                        TAG,
                                                        "Utilisateur non trouvé pour l'email : $currentUserEmail"
                                                )
                                        } else {
                                                val suggestions =
                                                        repository.getFriendSuggestionsByCriteriaOnly(
                                                                userId
                                                        )
                                                Log.d(
                                                        TAG,
                                                        "${suggestions.size} suggestions récupérées"
                                                )
                                                _friendSuggestions.value = suggestions
                                                _errorMessage.value = null

                                                val friendIds =
                                                        suggestions.map { it.userId }
                                                loadNotes(userId, friendIds)
                                        }
                                } catch (e: Exception) {
                                        Log.e(
                                                TAG,
                                                "Erreur lors du chargement des suggestions : ${e.message}",
                                                e
                                        )
                                        _errorMessage.value = e.message
                                        _friendSuggestions.value = emptyList()
                                } finally {
                                        _isLoading.value = false
                                        Log.d(TAG, "Fin du chargement des suggestions")
                                }
                        }
                }


                fun loadReviews() {
                        Log.d(TAG, "Début du chargement des reviews...")

                        val db = Firebase.firestore
                        db.collectionGroup("user_reviews")
                                .get()
                                .addOnSuccessListener { snapshot ->
                                        _reviews.clear()
                                        Log.d(
                                                TAG,
                                                "Nombre de documents récupérés : ${snapshot.size()}"
                                        )

                                        for (doc in snapshot.documents) {
                                                val reviewText = doc.getString("review") ?: ""
                                                val timestamp = doc.getLong("timestamp") ?: 0L

                                                // Récupère l'ID de l'utilisateur parent (document user)
                                                val pathSegments = doc.reference.path.split("/")
                                                val userId = pathSegments.getOrNull(
                                                        pathSegments.indexOf("users") + 1
                                                ) ?: ""

                                                // Charge son nom depuis /users/{userId}
                                                db.collection("users").document(userId).get()
                                                        .addOnSuccessListener { userDoc ->
                                                                val ownerName =
                                                                        userDoc.getString("username")
                                                                                ?: "Utilisateur inconnu"

                                                                val userReview = UserReview(
                                                                        review = reviewText,
                                                                        timestamp = timestamp,
                                                                        ownerName = ownerName
                                                                )

                                                                _reviews.add(userReview)
                                                                Log.d(
                                                                        "ReviewDebug",
                                                                        "Review ajoutée : ${userReview.review} de $ownerName"
                                                                )
                                                        }
                                        }
                                }
                                .addOnFailureListener { exception ->
                                        Log.e(
                                                TAG,
                                                "Erreur lors du chargement des reviews : ${exception.message}",
                                                exception
                                        )
                                }
                }


                fun addFriend(currentUserId: String, friendEmail: String) {
                        Log.d(
                                TAG,
                                "Tentative d'ajout d'ami : $friendEmail pour l'utilisateur $currentUserId"
                        )
                        viewModelScope.launch {
                                try {
                                        repository.addFriend(currentUserId, friendEmail)
                                        Log.d(TAG, "Ami ajouté avec succès.")
                                        val currentEmail =
                                                FirebaseAuth.getInstance().currentUser?.email
                                                        ?: ""
                                        loadFriendSuggestionsByEmail(currentEmail)
                                } catch (e: Exception) {
                                        val error =
                                                "Erreur lors de l'ajout d'un ami : ${e.message}"
                                        Log.e(TAG, error, e)
                                        _errorMessage.value = error
                                }
                        }
                }


                fun clearErrorMessage() {
                        Log.d(TAG, "Effacement du message d'erreur.")
                        _errorMessage.value = null
                }


                fun saveNote(noteText: String, description: String = "", userEmail: String) {
                        Log.d(TAG, "Tentative de sauvegarde de la note : $noteText")
                        viewModelScope.launch {
                                try {
                                        val currentUser = FirebaseAuth.getInstance().currentUser
                                        if (currentUser == null) {
                                                _errorMessage.value = "Utilisateur non connecté"
                                                return@launch
                                        }

                                        val userId = currentUser.uid
                                        val email = currentUser.email ?: ""

                                        // 🔍 Étape 1 : récupérer le username depuis Firestore
                                        Firebase.firestore.collection("users").document(userId)
                                                .get()
                                                .addOnSuccessListener { userSnapshot ->
                                                        val username =
                                                                userSnapshot.getString("username")
                                                                        ?: "Utilisateur inconnu"
                                                        Log.d(
                                                                TAG,
                                                                "Nom récupéré depuis Firestore: $username"
                                                        )

                                                        // 📝 Étape 2 : Préparer les données à sauvegarder
                                                        val noteRef = Firebase.firestore
                                                                .collection("users")
                                                                .document(userId)
                                                                .collection("notes")
                                                                .document("unique_note") // ID fixe

                                                        val data = mapOf(
                                                                "uid" to userId,
                                                                "email" to email,
                                                                "note" to noteText,
                                                                "description" to description,
                                                                "timestamp" to System.currentTimeMillis(),
                                                                "ownerName" to username
                                                        )

                                                        // 💾 Étape 3 : Enregistrer la note
                                                        noteRef.set(data)
                                                                .addOnSuccessListener {
                                                                        Log.d(
                                                                                TAG,
                                                                                "Note enregistrée avec succès (avec username)."
                                                                        )
                                                                        loadNotes(
                                                                                userId,
                                                                                emptyList()
                                                                        )
                                                                }
                                                                .addOnFailureListener { e ->
                                                                        Log.e(
                                                                                TAG,
                                                                                "Erreur lors de la sauvegarde de la note : ${e.message}"
                                                                        )
                                                                        _errorMessage.value =
                                                                                "Erreur lors de la sauvegarde de la note"
                                                                }
                                                }
                                                .addOnFailureListener { e ->
                                                        Log.e(
                                                                TAG,
                                                                "Erreur lors de la récupération du username : ${e.message}"
                                                        )
                                                        _errorMessage.value =
                                                                "Erreur lors de la récupération du nom d'utilisateur"
                                                }

                                } catch (e: Exception) {
                                        Log.e(TAG, "Exception dans saveNote: ${e.message}", e)
                                        _errorMessage.value = "Erreur interne lors de la sauvegarde"
                                }
                        }
                }


                fun loadNotes(currentUserId: String, friendIds: List<String>) {
                        if (currentUserId.isEmpty()) return

                        Log.d(TAG, "Chargement des notes pour utilisateur : $currentUserId et amis : $friendIds")

                        _isLoading.value = true
                        val allNotes = mutableListOf<Review>()
                        val idsToLoad = listOf(currentUserId) + friendIds

                        var completed = 0
                        var hadFailure = false

                        idsToLoad.forEach { userId ->
                                val userRef = Firebase.firestore.collection("users").document(userId)

                                // Lecture du pays actuel dans savedCountries/selected_country/name
                                userRef.collection("savedCountries")
                                        .document("selected_country")
                                        .get()
                                        .addOnSuccessListener { countrySnapshot ->
                                                val currentCountry = countrySnapshot.getString("name") ?: "Pays inconnu"
                                                Log.d(TAG, "Pays actuel pour $userId : $currentCountry")

                                                // Charger les notes de cet utilisateur
                                                userRef.collection("notes")
                                                        .get()
                                                        .addOnSuccessListener { notesSnapshot ->
                                                                val notes = notesSnapshot.documents.mapNotNull { doc ->
                                                                        try {
                                                                                Review(
                                                                                        uid = doc.getString("uid") ?: "",
                                                                                        email = doc.getString("email") ?: "",
                                                                                        note = doc.getString("note") ?: "",
                                                                                        description = doc.getString("description") ?: "",
                                                                                        timestamp = doc.getLong("timestamp") ?: 0L,
                                                                                        ownerName = doc.getString("ownerName") ?: "Utilisateur inconnu",
                                                                                        location = currentCountry
                                                                                )
                                                                        } catch (e: Exception) {
                                                                                Log.e(TAG, "Erreur de conversion de note pour user $userId", e)
                                                                                null
                                                                        }
                                                                }

                                                                allNotes.addAll(notes)
                                                                completed++
                                                                Log.d(TAG, "Notes chargées pour $userId, total chargés : $completed / ${idsToLoad.size}")

                                                                if (completed == idsToLoad.size) {
                                                                        _notes.value = allNotes.sortedByDescending { it.timestamp }
                                                                        _errorMessage.value = if (hadFailure) "Certaines notes ont échoué" else null
                                                                        _isLoading.value = false
                                                                        Log.d(TAG, "Chargement des notes terminé avec succès")
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                                Log.e(TAG, "Erreur chargement notes pour $userId : ${e.message}", e)
                                                                hadFailure = true
                                                                completed++
                                                                if (completed == idsToLoad.size) {
                                                                        _notes.value = allNotes.sortedByDescending { it.timestamp }
                                                                        _errorMessage.value = "Certaines notes n'ont pas pu être chargées"
                                                                        _isLoading.value = false
                                                                        Log.d(TAG, "Chargement des notes terminé avec erreurs")
                                                                }
                                                        }
                                        }
                                        .addOnFailureListener { e ->
                                                Log.e(TAG, "Erreur récupération savedCountries pour $userId : ${e.message}", e)
                                                hadFailure = true
                                                completed++
                                                if (completed == idsToLoad.size) {
                                                        _notes.value = allNotes.sortedByDescending { it.timestamp }
                                                        _errorMessage.value = "Certaines notes n'ont pas pu être chargées"
                                                        _isLoading.value = false
                                                        Log.d(TAG, "Chargement des notes terminé avec erreurs")
                                                }
                                        }
                        }
                }



                fun updateReviewRating(review: UserReview, newRating: Int) {
                        // Trouver l'index de la review à modifier
                        val index = _reviews.indexOfFirst { it == review }
                        if (index != -1) {
                                // Créer une nouvelle review avec la note modifiée
                                val updatedReview = review.copy(rating = newRating)

                                // Mettre à jour la liste
                                _reviews[index] = updatedReview

                                // TODO : mettre à jour dans Firestore si besoin
                                // Ex : updateReviewInFirestore(updatedReview)
                        }
                }


                fun saveSelectedCountry(
                        userId: String,
                        pays: String,
                        onSuccess: () -> Unit,
                        onFailure: (Exception) -> Unit
                ) {
                        val db = Firebase.firestore
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                                onFailure(Exception("Utilisateur non connecté"))
                                return
                        }

                        val userCountriesDocRef = db.collection("users")
                                .document(user.uid)
                                .collection("savedCountries")
                                .document("selected_country")

                        // Récupérer le document selected_country existant
                        userCountriesDocRef.get()
                                .addOnSuccessListener { document ->
                                        val existingCountries = document.get("countries") as? List<Map<String, Any>> ?: emptyList()

                                        // Ajouter le nouveau pays avec timestamp
                                        val updatedCountries = existingCountries.toMutableList().apply {
                                                add(
                                                        mapOf(
                                                                "name" to pays,
                                                                "timestamp" to com.google.firebase.Timestamp.now()
                                                        )
                                                )
                                        }

                                        // Mettre à jour le document avec la liste mise à jour
                                        userCountriesDocRef.set(mapOf("countries" to updatedCountries))
                                                .addOnSuccessListener {
                                                        onSuccess()
                                                }
                                                .addOnFailureListener { e ->
                                                        onFailure(e)
                                                }
                                }
                                .addOnFailureListener { e ->
                                        onFailure(e)
                                }
                }

                fun loadUsers() {
                        val db = Firebase.firestore
                        db.collection("users")
                                .get()
                                .addOnSuccessListener { snapshot ->
                                        val usersList = snapshot.documents.mapNotNull { doc ->
                                                doc.toObject(User::class.java)
                                        }
                                        _allUsers.value = usersList
                                }
                                .addOnFailureListener { e ->
                                        Log.e("HomeViewModel", "Erreur chargement users: ${e.message}")
                                }
                }
                fun calculateAge(birthday: String?): Int? {
                        if (birthday.isNullOrEmpty()) return null
                        return try {
                                val parts = birthday.split("/")
                                if (parts.size != 3) return null

                                val day = parts[0].toInt()
                                val month = parts[1].toInt()
                                val year = parts[2].toInt()

                                val today = Calendar.getInstance()
                                val birthDate = Calendar.getInstance().apply {
                                        set(year, month - 1, day) // mois indexé de 0
                                }

                                var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
                                if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
                                        age--
                                }
                                age
                        } catch (e: Exception) {
                                null
                        }
                }



                fun searchUsers(
                        selectedCountry: String? = null,
                        selectedgender: String? = null,
                        selectedMinAge: Int? = null,
                        selectedMaxAge: Int? = null,
                        onResult: (List<User>) -> Unit,
                        onError: (String) -> Unit
                ) {
                        val TAG = "SearchUsers"
                        Log.d(TAG, "Recherche avec filtres : country=$selectedCountry, gender=$selectedgender, minAge=$selectedMinAge, maxAge=$selectedMaxAge")

                        FirebaseFirestore.getInstance().collection("users")
                                .get()
                                .addOnSuccessListener { result ->
                                        Log.d(TAG, "Documents reçus: ${result.size()}")

                                        val users = result.documents.mapNotNull { doc ->
                                                try {
                                                        val user = doc.toObject(User::class.java)
                                                        val ageCalc = calculateAge(doc.getString("birthday"))
                                                        val countryFromDoc = doc.getString("country")
                                                        user?.copy(age = ageCalc, country = countryFromDoc)
                                                } catch (e: Exception) {
                                                        Log.e(TAG, "Erreur conversion document en User: ${e.message}")
                                                        null
                                                }
                                        }.filter { user ->
                                                val matchesCountry = selectedCountry.isNullOrBlank() || user.country == selectedCountry
                                                val matchesGender = selectedgender.isNullOrBlank() || user.gender == selectedgender
                                                val matchesAge = (selectedMinAge == null || (user.age ?: 0) >= selectedMinAge) &&
                                                        (selectedMaxAge == null || (user.age ?: 0) <= selectedMaxAge)

                                                val match = matchesCountry && matchesGender && matchesAge
                                                Log.d(TAG, "Filtrage utilisateur ${user.firstName} -> matches=$match")
                                                match
                                        }

                                        Log.d(TAG, "Utilisateurs après filtrage: ${users.size}")
                                        _searchResults.value = users
                                        onResult(users)
                                }
                                .addOnFailureListener { e ->
                                        Log.e(TAG, "Erreur récupération utilisateurs: ${e.message}")
                                        onError(e.message ?: "Erreur lors de la récupération des utilisateurs.")
                                }
                }
                fun clearSearch() {
                        searchQuery.value = ""
                        selectedCountryFilter.value = null
                        selectedTravelStyleFilter.value = null
                        _searchResults.value = _allUsers.value
                }

                fun clearAllUserData() {
                        Log.d(TAG, "Nettoyage de toutes les données utilisateur spécifiques dans le ViewModel.")
                        _friendSuggestions.value = emptyList() // Efface les suggestions d'amis
                        _reviews.clear() // Efface la liste des revues
                        _notes.value = emptyList() // Efface les notes
                        _searchResults.value = emptyList() // Efface les résultats de recherche
                        _errorMessage.value = null
                        _isLoading.value = false
                        searchQuery.value = ""
                        selectedCountryFilter.value = null
                        selectedTravelStyleFilter.value = null
                        // Laissez _allUsers si c'est une liste de TOUS les utilisateurs de l'app.
                        // Si elle ne doit contenir que des utilisateurs connectés, décommentez la ligne ci-dessous :
                        // _allUsers.value = emptyList()
                }







        }


        }











