package com.example.testapp.features.homescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testapp.R
import com.google.firebase.auth.FirebaseAuth

private val LightestGray = Color(0xFFF8F8F8)
private val LightGray = Color(0xFFE0E0E0)
private val PrimaryBlue = Color(0xFF1565C0)
private val MediumGray = Color(0xFFBDBDBD)
private val DarkGray = Color(0xFF424242)
private val LightestBlue = Color.White
private val LightBlue = Color(0xFFE8F0FE)
private val coulaire = Color(0xFF8DBAEC)
private val DarkBlue = Color(0xFF255A9D)
private val White = Color.White

class Home {
    companion object {
        const val homeScreenRoute = "homeScreen"

        @Composable
        fun homeScreen(navController: NavController, viewModel: HomeScreenViewModel = viewModel()) {
            val state by viewModel.state.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(LightestBlue, LightBlue)
                        )
                    )
            ) {
                UserButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    navController = navController
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 70.dp, start = 30.dp, end = 30.dp)
                ) {
                    var noteText by remember { mutableStateOf("") }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = LightBlue),
                        modifier = Modifier
                            .width(250.dp)
                            .height(80.dp)
                            .padding(8.dp)
                            .align(Alignment.TopStart),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp),
                                color = LightBlue,
                                shadowElevation = 4.dp
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.user),
                                    contentDescription = "User Avatar",
                                    tint = DarkBlue,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            TextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                placeholder = { Text("Écris quelque chose...") },
                                singleLine = false,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    cursorColor = DarkBlue,
                                    focusedIndicatorColor = DarkBlue,
                                    unfocusedIndicatorColor = MediumGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 60.dp)
                                    .padding(8.dp),
                                maxLines = 5,
                                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HomeButton(Modifier.weight(1f), navController)
                    MessageButton(Modifier.weight(1f), navController)
                }
            }
        }
    }
}

@Composable
fun UserButton(modifier: Modifier, navController: NavController) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(color = White, shape = RoundedCornerShape(16.dp))
            .border(1.dp, LightGray, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    navController.navigate("ProfileUserScreen")
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "Profil",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(35.dp)
                )
            }
            IconButton(
                onClick = {
                    expanded = !expanded
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = "Expand",
                    tint = DarkBlue
                )

            }
        }

        Text(
            text = "Profil",
            fontSize = 15.sp,
            color = PrimaryBlue,
            fontWeight = FontWeight.Medium
        )

        AnimatedVisibility(visible = expanded) {
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("SplashScreen") {
                        popUpTo("HomeScreen") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.4f)
                    .height(50.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HomeButton(modifier: Modifier, navController: NavController) {
    BottomNavButton(modifier, R.drawable.home, "Home", DarkBlue) {
        navController.navigate("homeRoute")
    }
}

@Composable
fun MessageButton(modifier: Modifier, navController: NavController) {
    BottomNavButton(modifier, R.drawable.message, "Message", DarkBlue) {
        navController.navigate("Chs")
    }
}

@Composable
fun BottomNavButton(
    modifier: Modifier,
    iconRes: Int,
    contentDesc: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = coulaire,
            contentColor = color
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDesc,
            modifier = Modifier.size(28.dp),
            tint = DarkBlue
        )
    }
}

@Preview(showBackground = true)
@Composable
fun homeScreenPreview() {
    val navController = rememberNavController()
    Home.homeScreen(navController)
}
