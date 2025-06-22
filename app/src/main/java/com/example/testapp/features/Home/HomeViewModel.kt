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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await


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
                val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()


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
                        Log.d(TAG, "Chargement des suggestions pour l'email : $currentUserEmail")
                        viewModelScope.launch {
                                _isLoading.value = true
                                try {
                                        val userId = repository.getUserIdByEmail(currentUserEmail)
                                        Log.d(TAG, "ID utilisateur récupéré : $userId")

                                        if (userId == null) {
                                                _errorMessage.value = "Utilisateur non trouvé"
                                                _friendSuggestions.value = emptyList()
                                                Log.w(TAG, "Utilisateur non trouvé pour l'email : $currentUserEmail")
                                        } else {
                                                val suggestions = repository.getFriendSuggestionsByCriteriaOnly(userId)
                                                Log.d(TAG, "${suggestions.size} suggestions récupérées")

                                                // Tri décroissant selon matchPercentage
                                                val sortedSuggestions = suggestions.sortedByDescending { it.matchPercentage }

                                                _friendSuggestions.value = sortedSuggestions
                                                _errorMessage.value = null

                                                val friendIds = sortedSuggestions.map { it.userId }
                                                loadNotes(userId, friendIds)
                                        }
                                } catch (e: Exception) {
                                        Log.e(TAG, "Erreur lors du chargement des suggestions : ${e.message}", e)
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
                                        Log.d(TAG, "Nombre de documents récupérés : ${snapshot.size()}")

                                        for (doc in snapshot.documents) {
                                                val reviewText = doc.getString("review") ?: ""
                                                val timestamp = doc.getLong("timestamp") ?: 0L
                                                val rating = doc.getLong("rating")?.toInt() ?: 0 // récupère la note, si tu l'as
                                                val countryName = doc.getString("country") ?: "Pays inconnu" // récupère le pays

                                                // Récupère l'ID de l'utilisateur parent (document user)
                                                val pathSegments = doc.reference.path.split("/")
                                                val userId = pathSegments.getOrNull(pathSegments.indexOf("users") + 1) ?: ""

                                                // Charge son nom depuis /users/{userId}
                                                db.collection("users").document(userId).get()
                                                        .addOnSuccessListener { userDoc ->
                                                                val ownerName = userDoc.getString("username") ?: "Utilisateur inconnu"

                                                                val userReview = UserReview(
                                                                        review = reviewText,
                                                                        timestamp = timestamp,
                                                                        ownerName = ownerName,
                                                                        rating = rating,
                                                                        countryName = countryName
                                                                )

                                                                _reviews.add(userReview)
                                                                Log.d("ReviewDebug", "Review ajoutée : ${userReview.review} de $ownerName")
                                                        }
                                        }
                                }
                                .addOnFailureListener { exception ->
                                        Log.e(TAG, "Erreur lors du chargement des reviews : ${exception.message}", exception)
                                }
                }


                fun addFriend(friendEmail: String) {
                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                        if (currentUserId == null) {
                                Log.e(TAG, "Utilisateur non connecté. Impossible d'envoyer une invitation.")
                                _errorMessage.value = "Veuillez vous connecter pour envoyer une invitation."
                                return
                        }

                        Log.d(TAG, "Tentative d'envoi d'invitation à : $friendEmail par $currentUserId")

                        viewModelScope.launch {
                                try {
                                        val db = FirebaseFirestore.getInstance()

                                        // Récupérer l'ID utilisateur destinataire via son email
                                        Log.d(TAG, "Recherche de l'utilisateur avec l'email : $friendEmail")
                                        val querySnapshot = db.collection("users")
                                                .whereEqualTo("email", friendEmail)
                                                .get()
                                                .await()

                                        if (!querySnapshot.isEmpty) {
                                                val friendDoc = querySnapshot.documents[0]
                                                val friendUserId = friendDoc.id
                                                Log.d(TAG, "Utilisateur trouvé. ID = $friendUserId")

                                                // Préparer les références Firestore
                                                val sentInvitationRef = db.collection("users")
                                                        .document(currentUserId)
                                                        .collection("invitations_envoyees")
                                                        .document(friendUserId)

                                                val receivedInvitationRef = db.collection("users")
                                                        .document(friendUserId)
                                                        .collection("invitations_recues")
                                                        .document(currentUserId)

                                                // Préparer les données à écrire
                                                val invitationData = mapOf("timestamp" to FieldValue.serverTimestamp())

                                                // Écrire les deux invitations simultanément avec await()
                                                sentInvitationRef.set(invitationData).await()
                                                Log.d(TAG, "Invitation ajoutée à invitations_envoyees avec succès.")

                                                receivedInvitationRef.set(invitationData).await()
                                                Log.d(TAG, "Invitation ajoutée à invitations_recues avec succès.")
// Après avoir récupéré friendUserId
                                                _friendSuggestions.value = _friendSuggestions.value.filter { it.userId != friendUserId }

                                                // Mise à jour des suggestions si nécessaire
                                                val currentEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
                                                Log.d(TAG, "Rafraîchissement des suggestions pour : $currentEmail")
                                                loadFriendSuggestionsByEmail(currentEmail)

                                        } else {
                                                val error = "Utilisateur avec l'email $friendEmail non trouvé."
                                                Log.e(TAG, error)
                                                _errorMessage.value = error
                                        }
                                } catch (e: Exception) {
                                        val error = "Erreur lors de l'envoi de l'invitation : ${e.message}"
                                        Log.e(TAG, error, e)
                                        _errorMessage.value = error
                                }
                        }
                }
                fun removeUserFromSearchResults(user: User) {
                        val currentList = _searchResults.value.toMutableList()
                        currentList.remove(user)
                        _searchResults.value = currentList
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
                                val selectedCountryRef = userRef.collection("savedCountries").document("selected_country")

                                // Récupérer le document selected_country
                                selectedCountryRef.get()
                                        .addOnSuccessListener { countrySnapshot ->
                                                val countriesList = countrySnapshot.get("countries") as? List<Map<String, Any>>
                                                val currentCountry = if (!countriesList.isNullOrEmpty()) {
                                                        countriesList
                                                                .sortedByDescending { (it["timestamp"] as? com.google.firebase.Timestamp)?.toDate() }
                                                                .firstOrNull()?.get("name") as? String ?: "Pays inconnu"
                                                } else {
                                                        "Pays inconnu"
                                                }
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
                                                Log.e(TAG, "Erreur récupération savedCountries/selected_country pour $userId : ${e.message}", e)
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
                        currentUserId: String,
                        selectedCountry: String? = null,
                        selectedgender: String? = null,
                        selectedMinAge: Int? = null,
                        selectedMaxAge: Int? = null,
                        selectedLanguage: String? = null, // Champ singulier, type String
                        onResult: (List<User>) -> Unit,
                        onError: (String) -> Unit
                ) {
                        val TAG = "SearchUsers"
                        Log.d(TAG, "🔍 Recherche avec filtres : country=$selectedCountry, gender=$selectedgender, minAge=$selectedMinAge, maxAge=$selectedMaxAge, language=$selectedLanguage")

                        _showSearchResults.value = true

                        // 🔽 Récupérer les utilisateurs bloqués et amis d'abord
                        CoroutineScope(Dispatchers.IO).launch {
                                try {
                                        val userRepository = UserRepository(FirebaseFirestore.getInstance())
                                        val blockedUserIds = userRepository.getBlockedUserIds(currentUserId)
                                        val friendIds = userRepository.getFriendIds(currentUserId)


                                        FirebaseFirestore.getInstance().collection("users")
                                                .get()
                                                .addOnSuccessListener { result ->
                                                        Log.d(TAG, "📥 Documents reçus: ${result.size()}")

                                                        val users = result.documents.mapNotNull { doc ->
                                                                val userId = doc.id

                                                                // ✅ Ignorer soi-même, amis et bloqués
                                                                if (
                                                                        userId == currentUserId ||
                                                                        blockedUserIds.contains(userId) ||
                                                                        friendIds.contains(userId)
                                                                ) {
                                                                        Log.d(TAG, "⛔ Utilisateur ignoré: $userId (bloqué ou ami)")
                                                                        return@mapNotNull null
                                                                }

                                                                try {
                                                                        val user = doc.toObject(User::class.java)
                                                                        val ageCalc = calculateAge(doc.getString("birthday"))
                                                                        val countryFromDoc = doc.getString("country")
                                                                        val languageFromDoc = doc.getString("selectedLanguage")  // champ singulier

                                                                        Log.d(TAG, "✅ Document ID=${doc.id} - user=${user?.firstName} ${user?.lastName}, age=$ageCalc, country=$countryFromDoc, language=$languageFromDoc")

                                                                        user?.copy(age = ageCalc, country = countryFromDoc, selectedLanguage = languageFromDoc)
                                                                } catch (e: Exception) {
                                                                        Log.e(TAG, "❌ Erreur conversion document en User: ${e.message}", e)
                                                                        null
                                                                }
                                                        }.filter { user ->
                                                                val matchesCountry = selectedCountry.isNullOrBlank() || user.country == selectedCountry
                                                                val matchesGender = selectedgender.isNullOrBlank() || user.gender == selectedgender
                                                                val matchesAge = (selectedMinAge == null || (user.age ?: 0) >= selectedMinAge) &&
                                                                        (selectedMaxAge == null || (user.age ?: 0) <= selectedMaxAge)
                                                                val matchesLanguage = selectedLanguage.isNullOrBlank() ||
                                                                        (user.selectedLanguage == selectedLanguage)

                                                                val match = matchesCountry && matchesGender && matchesAge && matchesLanguage
                                                                Log.d(TAG, "🔍 Filtrage utilisateur ${user.firstName} -> match=$match (pays=$matchesCountry, genre=$matchesGender, âge=$matchesAge, langue=$matchesLanguage)")
                                                                match
                                                        }

                                                        Log.d(TAG, "✅ Utilisateurs après filtrage: ${users.size}")
                                                        _searchResults.value = users
                                                        onResult(users)
                                                }
                                                .addOnFailureListener { e ->
                                                        Log.e(TAG, "❌ Erreur récupération utilisateurs: ${e.message}", e)
                                                        onError(e.message ?: "Erreur lors de la récupération des utilisateurs.")
                                                }

                                } catch (e: Exception) {
                                        Log.e(TAG, "❌ Erreur récupération amis ou utilisateurs bloqués: ${e.message}", e)
                                        onError("Erreur récupération amis ou bloqués.")
                                }
                        }
                }

                fun ignoreUser(userId: String) {
                        _searchResults.value = _searchResults.value.filter { it.userId != userId }
                }

                fun removeFriendSuggestion(email: String) {
                        _friendSuggestions.value = _friendSuggestions.value.filter { it.email != email }
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











