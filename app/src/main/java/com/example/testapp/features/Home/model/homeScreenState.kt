sealed class HomeState {
    object Loading : HomeState()
    data class Success(val users: List<User>) : HomeState()
    data class Error(val message: String) : HomeState()
    data class  Filtering(val filteredUsers: List<User>) : HomeState()
    data class Note(
        val id: String = "",
        val content: String = "",
        val authorId: String = "",
        val ownerName: String = "", // 💡 on ajoute ça !
        val timestamp: Long = System.currentTimeMillis()
    )

// Nouvel état pour filtrer
}
