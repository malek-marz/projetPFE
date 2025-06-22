import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@Composable
fun BlockedUsersScreen(viewModel: BlockedUsersViewModel = viewModel()) {
    val blockedUsers by viewModel.blockedUsers.collectAsState()
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var lastUnblockedUserId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadBlockedUsers()
        delay(500) // simulate loading time
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Utilisateurs bloqués",
            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Divider(color = Color.LightGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (blockedUsers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = "No blocked users",
                        tint = Color.Gray,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Aucun utilisateur bloqué pour l’instant.",
                        style = MaterialTheme.typography.body1.copy(color = Color.Gray)
                    )
                }
            }
        } else {
            blockedUsers.forEach { user ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    elevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {

                                // Optional: maybe show details or unblock confirmation
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circle placeholder with icon
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = "User icon",
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = user.username,
                                style = MaterialTheme.typography.subtitle1.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "UID: ${user.uid.take(8)}...", // Show partial uid as subtitle
                                style = MaterialTheme.typography.caption.copy(
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            )
                        }

                        TextButton(
                            onClick = {
                                viewModel.unblockUser(user.uid) {
                                    lastUnblockedUserId = user.uid
                                    Toast.makeText(context, "Utilisateur débloqué", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colors.error
                            )
                        ) {
                            Text(
                                "Débloquer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Animated feedback (optional)
                AnimatedVisibility(
                    visible = lastUnblockedUserId == user.uid,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        "Utilisateur débloqué",
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }
}
