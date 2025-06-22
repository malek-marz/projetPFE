    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Close
    import androidx.compose.material.icons.filled.PersonAdd
    import androidx.compose.material3.*
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.text.font.FontWeight
    import android.util.Log
    import androidx.compose.foundation.clickable
    import com.example.testapp.models.FriendSuggestion

    fun getInitials(firstName: String, lastName: String): String {
        return "${firstName.firstOrNull()?.uppercase() ?: ""}${lastName.firstOrNull()?.uppercase() ?: ""}"
    }

    @Composable
    fun FriendSuggestionCard(
        friend: FriendSuggestion,
        onAddFriend: (String) -> Unit,
        onProfileClick: () -> Unit, // <-- Ajouté ici
        onDismiss: (() -> Unit)? = null,
        modifier: Modifier = Modifier

    ) {
        val TAG = "FriendSuggestionCard"
        val matchProgress = (friend.matchPercentage.toFloat() / 100f).coerceIn(0f, 1f)

        Log.d(TAG, "Affichage de la suggestion pour: ${friend.firstName} ${friend.lastName}")

        Card(
            modifier = modifier
                .width(300.dp)
                .height(160.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable { onProfileClick() }, // <-- Ajout du clic ici

        shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                // Initiales dans un cercle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF90CAF9)) // Bleu ciel
                ) {
                    Text(
                        text = getInitials(friend.firstName, friend.lastName),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = "${friend.firstName} ${friend.lastName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = friend.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    if (friend.country.isNotBlank()) {
                        Text(
                            text = "Pays: ${friend.country}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = matchProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF2196F3)
                    )

                    Text(
                        text = "Match: ${friend.matchPercentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2196F3),
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = {
                        Log.d(TAG, "Ajout de l'ami: ${friend.email}")
                        onAddFriend(friend.email)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Ajouter ami"
                    )
                }

                onDismiss?.let {
                    IconButton(
                        onClick = {
                            Log.d(TAG, "Suggestion ignorée pour: ${friend.email}")
                            onDismiss()
                        },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer suggestion"
                        )
                    }
                }
            }
        }
    }
