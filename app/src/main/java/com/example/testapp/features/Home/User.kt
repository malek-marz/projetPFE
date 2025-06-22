import org.intellij.lang.annotations.Language

data class User(
    val userId: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profileImageUrl: String = "",
    val note: String = "",
    val country: String? = null,
    val selectedLanguage: String? = null,  // champ simple
    val age: Int? = null,
    val gender: String? = null,
    val criteria: List<String> = emptyList(),
    val birthday: String? = null  // ajouté pour pouvoir calculer l'âge
)
